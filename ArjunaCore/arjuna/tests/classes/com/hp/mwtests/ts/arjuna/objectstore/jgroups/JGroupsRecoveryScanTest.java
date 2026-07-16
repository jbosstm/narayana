/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.hp.mwtests.ts.arjuna.resources.CrashRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests showing that periodic recovery can recover in doubt actions in the JGroups slot store.
 * This test is analogous to InfinispanRecoveryScanTest.
 */
public class JGroupsRecoveryScanTest extends JGroupsTestBase {

    static RecoveryManager manager;
    static Store store;
    static RecoveryStore recoveryStore;

    @BeforeAll
    public static void beforeAll() throws Exception {
        // Register the record type for recovery
        RecordTypeManager.manager().add(new RecoverableCrashRecordTypeMap());

        // Create and start the JGroups store
        store = createStore("node1", CLUSTER_NAME, STORE_DIR);
        store.start();

        // Get the recovery store that was created during store.start()
        recoveryStore = startRecoveryStore(store.config());

        // Setup recovery system
        RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager.getRecoveryEnvironmentBean();
        recoveryEnvironmentBean.setRecoveryBackoffPeriod(1); // use a short interval between passes
        recoveryEnvironmentBean.setRecoveryListener(true); // configure the RecoveryMonitor

        manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);

        try {
            resetAtomicActionRecoveryModule();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        AtomicActionRecoveryModule aaRecoveryModule = new AtomicActionRecoveryModule();
        manager.addModule(aaRecoveryModule);
    }

    @AfterAll
    static void afterAll() {
        if (manager != null) {
            manager.terminate();
            manager = null;
        }
        if (recoveryStore != null) {
            recoveryStore.stop();
            recoveryStore = null;
        }
        if (store != null) {
            store.stop();
            store = null;
        }
    }

    @Test
    public void testRecovery() throws Exception {
        Uid uid = new Uid();
        AtomicAction aa = new AtomicAction(uid);

        aa.begin();

        aa.add(new RecoverableCrashRecord(CrashRecord.CrashLocation.NoCrash, CrashRecord.CrashType.Normal));
        aa.add(new RecoverableCrashRecord(CrashRecord.CrashLocation.CrashInCommit, CrashRecord.CrashType.Normal));

        assertEquals(ActionStatus.COMMITTED, aa.commit(true)); // normal crash in commit

        // Verify that there is something in the store (the in-doubt transaction)
        try {
            recoveryStore.read_committed(aa.getSavingUid(), aa.type());
        } catch (ObjectStoreException e) {
            fail("Record should be available in the recovery store: ", e);
        }

        /*
         * The recovery scan should find the atomic action with one prepared record which will be recovered via the
         * AtomicActionRecoveryModule.doRecoverTransaction logic which recreates the second RecoverableCrashRecord
         * using the empty constructor (via AbstractRecord.create) which does not set any CrashRecord.CrashLocation
         * so the commit will be replayed successfully.
         */
        manager.startRecoveryManagerThread(); // start periodic recovery
        manager.scan();

        // Wait for recovery to complete (give it some time to process)
        waitFor(RECOVERY_TIMEOUT_MS, "recovery to complete", () -> {
            try {
                recoveryStore.read_committed(aa.getSavingUid(), aa.type());
                return false; // Still present i.e. not recovered yet
            } catch (ObjectStoreException e) {
                return true; // Successfully recovered
            }
        });

        // Verify that the atomic action was recovered
        try {
            recoveryStore.read_committed(aa.getSavingUid(), aa.type());
            fail("The recovery scan should have committed the action");
        } catch (ObjectStoreException ignore) {
        }
    }

    /**
     * RecoverableCrashRecord that can be reconstructed during recovery.
     */
    public static class RecoverableCrashRecord extends CrashRecord {

        // Default constructor needed for recovery via AbstractRecord.create
        public RecoverableCrashRecord() {
            super();
        }

        public RecoverableCrashRecord(CrashLocation crashLocation, CrashType crashType) {
            super(crashLocation, crashType);
        }

        @Override
        public boolean shouldAdd(AbstractRecord a) {
            return true;
        }
    }

    /**
     * RecordTypeMap is the Arjuna mechanism that facilitates {@link AbstractRecord#create(int)}
     * to create specific record types during recovery.
     */
    static class RecoverableCrashRecordTypeMap implements RecordTypeMap {
        public Class<RecoverableCrashRecord> getRecordClass() {
            return RecoverableCrashRecord.class;
        }

        public int getType() {
            // The type must be registered in the RecordType registry
            int recordType = new RecoverableCrashRecord().typeIs();
            assertEquals(RecordType.USER_DEF_FIRST0, recordType);
            return recordType;
        }
    }
}
