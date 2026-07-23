/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups;

import com.arjuna.ats.arjuna.logging.tsLogger;
import org.apache.activemq.artemis.core.journal.Journal;
import org.apache.activemq.artemis.core.journal.JournalLoadInformation;
import org.apache.activemq.artemis.core.journal.PreparedTransactionInfo;
import org.apache.activemq.artemis.core.journal.RecordInfo;
import org.apache.activemq.artemis.core.journal.TransactionFailureCallback;
import org.apache.activemq.artemis.core.journal.impl.JournalImpl;
import org.apache.activemq.artemis.core.io.SequentialFileFactory;
import org.apache.activemq.artemis.core.io.aio.AIOSequentialFileFactory;
import org.apache.activemq.artemis.core.io.nio.NIOSequentialFileFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Write-Ahead Log for JGroupsSlots using Artemis Journal.
 * Provides crash recovery for slot data with efficient append-only logging.
 *
 * <p>Features:
 * <ul>
 *   <li>Append-only journal for fast writes</li>
 *   <li>Automatic compaction of old entries</li>
 *   <li>Crash recovery on restart</li>
 *   <li>Optional fsync for durability</li>
 * </ul>
 *
 * <p>Journal Format:
 * Each record contains: [slotId (4 bytes)] [data length (4 bytes)] [data (N bytes)]
 */
public class SlotJournal {

    private final Journal journal;
    private final boolean syncWrites;
    private final boolean syncDeletes;
    private final AtomicLong nextRecordId = new AtomicLong(0);

    // Maps slotId to journal record ID for fast lookups
    private final Map<Integer, Long> slotToRecordId = new ConcurrentHashMap<>();

    // Maps slotId to data (in-memory cache loaded from journal)
    private final Map<Integer, byte[]> slotData = new ConcurrentHashMap<>();

    private static final byte RECORD_TYPE = 0x01;

    /**
     * Create a new SlotJournal using configuration from JGroupsStoreEnvironmentBean.
     * This constructor mirrors HornetqJournalStore's approach to journal configuration.
     *
     * @param config Environment bean with write-ahead log journal configuration
     * @throws IOException if journal cannot be created
     */
    public SlotJournal(JGroupsStoreEnvironmentBean config) throws IOException {
        this.syncWrites = config.isWalSyncWrites();
        this.syncDeletes = config.isWalSyncDeletes();

        String storeDir = config.getStoreDir();
        if (storeDir == null || storeDir.isEmpty()) {
            throw new IllegalArgumentException("storeDir must be set when using the SlotJournal");
        }

        File storeDirFile = new File(storeDir);
        if (!storeDirFile.exists() && !storeDirFile.mkdirs()) {
            throw new IOException(tsLogger.i18NLogger.get_dir_create_failed(storeDirFile.getCanonicalPath()));
        }

        // Buffer timeout calculation matches HornetqJournalStore
        int flushesPerSecond = config.getWalBufferFlushesPerSecond();
        if (flushesPerSecond <= 0) {
            throw new IllegalArgumentException("walBufferFlushesPerSecond must be > 0");
        }
        // Buffer timeout calculation matches HornetqJournalStore
        int bufferTimeoutNanos = (int)(1000000000d / flushesPerSecond);

        SequentialFileFactory fileFactory;
        if (config.isWalAsyncIO() && AIOSequentialFileFactory.isSupported()) {
            fileFactory = new AIOSequentialFileFactory(
                storeDirFile,
                config.getWalBufferSize(),
                bufferTimeoutNanos,
                config.getWalMaxIO(),
                false  // don't output write and flush rate debug metrics
            );
        } else {
            if (config.isWalAsyncIO()) {
                tsLogger.i18NLogger.warn_not_asyncIO();
            }
            fileFactory = new NIOSequentialFileFactory(
                storeDirFile,
                true,  // buffered - enables TimedBuffer for write batching
                config.getWalBufferSize(),
                bufferTimeoutNanos,
                1,  // maxIO has no effect in NIO mode
                false // don't output write and flush rate debug metrics
            );
        }

        // Create journal with configuration from environment bean
        journal = new JournalImpl(
            config.getWalFileSize(),
            config.getWalMinFiles(),
            config.getWalPoolSize(),
            config.getWalCompactMinFiles(),
            config.getWalCompactPercentage(),
            fileFactory,
            config.getWalFilePrefix(),
            config.getWalFileExtension(),
            config.getWalMaxIO()
        );

        // optimize record updates (in-place replace instead of append+delete) otherwise every slot overwrite results in
        // faster journal growth and more compaction overhead.
        journal.replaceableRecord(RECORD_TYPE);
        journal.setRemoveExtraFilesOnLoad(true);
    }

    /**
     * Start the journal and load existing records.
     *
     * @throws Exception if journal cannot be started
     */
    public void start() throws Exception {
        journal.start();

        List<RecordInfo> committedRecords = new LinkedList<>();
        List<PreparedTransactionInfo> preparedTransactions = new LinkedList<>();
        TransactionFailureCallback failureCallback = (txId, records, recordsToDelete) -> {
            tsLogger.logger.warn("SlotJournal: Transaction " + txId + " failed to load");
        };

        // Load journal and replay records
        JournalLoadInformation loadInfo = journal.load(committedRecords, preparedTransactions, failureCallback);
        nextRecordId.set(loadInfo.getMaxID() + 1);

        if (!preparedTransactions.isEmpty()) {
            tsLogger.logger.warn("SlotJournal: Found " + preparedTransactions.size() +
                " prepared transactions, ignoring (slots don't use tx)");
        }

        // Replay records into memory
        for (RecordInfo record : committedRecords) {
            try {
                ByteBuffer buffer = ByteBuffer.wrap(record.data);
                int slotId = buffer.getInt();
                int dataLength = buffer.getInt();
                byte[] data = new byte[dataLength];
                buffer.get(data);

                slotData.put(slotId, data);
                slotToRecordId.put(slotId, record.id);
            } catch (Exception e) {
                tsLogger.logger.warn("SlotJournal: Failed to load record " + record.id + ": " + e.getMessage());
            }
        }

        tsLogger.logger.infof("SlotJournal: Loaded %d slots from journal", slotData.size());
    }

    /**
     * Stop the journal, flushing all pending writes.
     *
     * @throws Exception if journal cannot be stopped cleanly
     */
    public void stop() throws Exception {
        // Compact journal to apply deletes before stopping
        // This ensures deleted records are truly removed from the journal files
        try {
            journal.scheduleCompactAndBlock(5000); // 5 second timeout
        } catch (Exception e) {
            tsLogger.logger.warn("SlotJournal: Compaction failed during stop: " + e.getMessage());
        }
        journal.stop();
    }

    /**
     * Write slot data to journal.
     * If slot already exists, replaces it (deletes old record, adds new one).
     *
     * @param slotId the slot ID (0 to numberOfSlots-1)
     * @param data the data to write
     * @throws Exception if write fails
     */
    public void write(int slotId, byte[] data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Cannot write null data for slot " + slotId);
        }

        // Pack data: [slotId (4 bytes)] [length (4 bytes)] [data (N bytes)]
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + data.length);
        buffer.putInt(slotId);
        buffer.putInt(data.length);
        buffer.put(data);
        byte[] encoded = buffer.array();

        Long oldRecordId = slotToRecordId.get(slotId);

        if (oldRecordId != null) {
            journal.appendUpdateRecord(oldRecordId, RECORD_TYPE, encoded, syncWrites);
        } else {
            long newRecordId = nextRecordId.getAndIncrement();
            journal.appendAddRecord(newRecordId, RECORD_TYPE, encoded, syncWrites);
            slotToRecordId.put(slotId, newRecordId);
        }

        slotData.put(slotId, data);
    }

    /**
     * Read slot data from memory (already loaded from journal on start).
     *
     * @param slotId the slot ID
     * @return the slot data, or null if slot is empty
     */
    public byte[] read(int slotId) {
        return slotData.get(slotId);
    }

    /**
     * Delete slot data from journal.
     *
     * @param slotId the slot ID
     * @throws Exception if delete fails
     */
    public void delete(int slotId) throws Exception {
        Long recordId = slotToRecordId.remove(slotId);
        slotData.remove(slotId);

        if (recordId != null) {
            if (syncDeletes) {
                journal.appendDeleteRecord(recordId, true);
            } else {
                journal.tryAppendDeleteRecord(recordId, false, null, null);
            }
        }
    }

    /**
     * Get all slot IDs that have data in the journal.
     *
     * @return set of slot IDs
     */
    public java.util.Set<Integer> getSlotIds() {
        return new java.util.HashSet<>(slotData.keySet());
    }

    /**
     * Compact the journal, removing old records.
     * Call periodically to prevent journal from growing indefinitely.
     *
     * @throws Exception if compaction fails
     */
    public void compact() throws Exception {
        journal.scheduleCompactAndBlock(5000); // 5 second timeout
        tsLogger.logger.debug("SlotJournal: Compaction completed");
    }

    /**
     * Get the number of slots with data.
     *
     * @return count of non-empty slots
     */
    public int size() {
        return slotData.size();
    }
}
