/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.internal.arjuna.objectstore.hornetq;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.OutputBuffer;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.ats.internal.arjuna.common.UidHelper;
import org.hornetq.core.journal.*;
import org.hornetq.core.journal.impl.JournalImpl;
import org.hornetq.core.journal.impl.AIOSequentialFileFactory;
import org.hornetq.core.journal.impl.NIOSequentialFileFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of the tx store backed by HornetQ's journal.
 * This is a bean suitable for hooking into the app server lifecycle.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-03
 */
public class HornetqJournalStore
{
    private final Journal journal;

    private final ConcurrentMap<String,Map<Uid, RecordInfo>> content = new ConcurrentHashMap<String, Map<Uid, RecordInfo>>();

    private final Object uidMappingLock = new Object();
    private final boolean syncWrites;
    private final boolean syncDeletes;
    private long maxID = 0;

    private final String storeDirCanonicalPath;
    
    private static final byte RECORD_TYPE = 0x00;

    public void stop() throws Exception {
        journal.stop();
    }

    public void start() throws Exception {

        journal.start();

        List<RecordInfo> committedRecords = new LinkedList<RecordInfo>();
        List<PreparedTransactionInfo> preparedTransactions = new LinkedList<PreparedTransactionInfo>();
        TransactionFailureCallback failureCallback = new TransactionFailureCallback() {
            public void failedTransaction(long l, java.util.List<org.hornetq.core.journal.RecordInfo> recordInfos, java.util.List<org.hornetq.core.journal.RecordInfo> recordInfos1) {
                System.err.println("This should never get called");
            }
        };

        JournalLoadInformation journalLoadInformation = journal.load(committedRecords, preparedTransactions, failureCallback);
        maxID = journalLoadInformation.getMaxID();

        if(!preparedTransactions.isEmpty()) {
            System.err.println("This should never happen");
        }

        for(RecordInfo record : committedRecords) {
            InputBuffer inputBuffer = new InputBuffer(record.data);
            Uid uid = UidHelper.unpackFrom(inputBuffer);
            String typeName = inputBuffer.unpackString();
            getContentForType(typeName).put(uid, record);
            // don't unpack the rest yet, we may never need it. read_committed does it on demand.
        }

        System.out.println("HornetqJournalStore started foo");
    }

    public HornetqJournalStore(HornetqJournalEnvironmentBean envBean) throws IOException {

        System.out.println("HornetqJournalStore ctor");

        syncWrites = envBean.isSyncWrites();
        syncDeletes = envBean.isSyncDeletes();

        File storeDir = new File(envBean.getStoreDir());
        if(!storeDir.exists() && !storeDir.mkdirs()) {
            throw new IOException("Failed to create store dir "+storeDir.getCanonicalPath()); // TODO i18n
        }
        storeDirCanonicalPath = storeDir.getCanonicalPath();

        SequentialFileFactory sequentialFileFactory;
        if(AIOSequentialFileFactory.isSupported()) {
            sequentialFileFactory = new AIOSequentialFileFactory(envBean.getStoreDir());
        } else {
            sequentialFileFactory = new NIOSequentialFileFactory(envBean.getStoreDir());
        }

        journal = new JournalImpl(envBean.getFileSize(), envBean.getMinFiles(), envBean.getCompactMinFiles(),
                        envBean.getCompactPercentage(), sequentialFileFactory, envBean.getFilePrefix(),
                        envBean.getFileExtension(), envBean.getMaxIO());
    }


    /**
     * Remove the object's committed state.
     *
     * @param uid  The object to work on.
     * @param typeName The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     * @throws ObjectStoreException if things go wrong.
     */
    public boolean remove_committed(Uid uid, String typeName) throws ObjectStoreException
    {
        try {
            long id = getId(uid, typeName); // look up the id *before* doing the remove from state, or it won't be there any more.
            getContentForType(typeName).remove(uid);
            journal.appendDeleteRecord(id, syncDeletes);
        } catch(Exception e) {
            throw new ObjectStoreException(e);
        }

        return true;
    }

    /**
     * Write a new copy of the object's committed state.
     *
     * @param uid    The object to work on.
     * @param typeName   The type of the object to work on.
     * @param txData The state to write.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     * @throws ObjectStoreException if things go wrong.
     */
    public boolean write_committed(Uid uid, String typeName, OutputObjectState txData) throws ObjectStoreException
    {
        try {
            OutputBuffer outputBuffer = new OutputBuffer();
            UidHelper.packInto(uid, outputBuffer);
            outputBuffer.packString(typeName);
            outputBuffer.packBytes(txData.buffer());
            long id = getId(uid, typeName);
            byte[] data = outputBuffer.buffer();

            // yup, there is a race condition here.
            if(getContentForType(typeName).containsKey(uid)) {
                journal.appendUpdateRecord(id, RECORD_TYPE, data, syncWrites);
            } else {
                journal.appendAddRecord(id, RECORD_TYPE, data, syncWrites);
            }

            RecordInfo record = new RecordInfo(id, RECORD_TYPE, data, false);
            getContentForType(typeName).put(uid, record);
        } catch(Exception e) {
            throw new ObjectStoreException(e);
        }

        return true;
    }

    /**
     * Read the object's committed state.
     *
     * @param uid  The object to work on.
     * @param typeName The type of the object to work on.
     * @return the state of the object.
     * @throws ObjectStoreException if things go wrong.
     */
    public InputObjectState read_committed(Uid uid, String typeName) throws ObjectStoreException
    {
        RecordInfo record = getContentForType(typeName).get(uid);
        if(record == null) {
            return null;
        }

        // this repeated unpacking is a little inefficient - subclass RecordInfo to hold unpacked form too?
        // not too much of an issue as log reads are done for recovery only.
        try {
            InputBuffer inputBuffer = new InputBuffer(record.data);
            Uid unpackedUid = UidHelper.unpackFrom(inputBuffer);
            String unpackedTypeName = inputBuffer.unpackString();
            InputObjectState inputObjectState = new InputObjectState(uid, typeName, inputBuffer.unpackBytes());
            return inputObjectState;
        } catch(Exception e) {
            throw new ObjectStoreException(e);
        }
    }

    public boolean contains(Uid uid, String typeName) {
        RecordInfo record = getContentForType(typeName).get(uid);
        return record != null;
    }

    /**
     * @return the "name" of the object store. Where in the hierarchy it appears, e.g., /ObjectStore/MyName/...
     */
    public String getStoreName()
    {
        return this.getClass().getSimpleName()+":"+storeDirCanonicalPath;
    }

    public String[] getKnownTypes() {
        return content.keySet().toArray(new String[content.size()]);
    }

    public Uid[] getUidsForType(String typeName) {
        Set<Uid> keySet = getContentForType(typeName).keySet();
        return keySet.toArray(new Uid[keySet.size()]);
    }

    /////////////////////////////////

    private Map<Uid, RecordInfo> getContentForType(String typeName) {
        Map<Uid, RecordInfo> result = content.get(typeName);
        if(result == null) {
            content.putIfAbsent(typeName, new ConcurrentHashMap<Uid, RecordInfo>());
            result = content.get(typeName);
        }
        return result;
    }

    private long getId(Uid uid, String typeName) {
        synchronized (uidMappingLock) {
            RecordInfo record = getContentForType(typeName).get(uid);
            if(record != null) {
                return record.id;
            } else {
                maxID++;
                return maxID;
            }
        }
    }
}
