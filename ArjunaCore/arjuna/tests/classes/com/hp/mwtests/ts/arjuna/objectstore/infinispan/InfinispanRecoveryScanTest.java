/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.objectstore.infinispan;

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
import org.infinispan.configuration.cache.CacheMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class InfinispanRecoveryScanTest extends InfinispanTestBase {

    private static RecoveryManager manager;
    private static InfinispanTestBase.Store store;
    private static RecoveryStore recoveryStore;

    @BeforeAll
    public static void beforeAll() throws Exception {
        RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager.getRecoveryEnvironmentBean();
        store = new InfinispanTestBase.Store(
                createCacheManager("node1", CacheMode.REPL_SYNC, -1, null, false, false),
                null, "node1");

        store.start();
        recoveryStore = startRecoveryStore(store.config());

        recoveryEnvironmentBean.setRecoveryBackoffPeriod(1); // use a short interval between passes
        recoveryEnvironmentBean.setRecoveryListener(false); // configure the RecoveryMonitor

        manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);

        try {
            resetAtomicActionRecoveryModule();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        AtomicActionRecoveryModule aaRecoveryModule = new AtomicActionRecoveryModule();
        manager.addModule(aaRecoveryModule); // we only need to test the XARecoveryModule
    }

    @AfterAll
    public static void afterClass() throws Exception {
        recoveryStore.stop();
        store.stop();
        manager.terminate();
    }

    public static class RecoverableCrashRecord extends CrashRecord {

        // needs a default constructor which gets called during recovery via AbstractRecord.create
        public RecoverableCrashRecord() {
            super();
        }

        public RecoverableCrashRecord(CrashLocation crashLocation, CrashType crashType) {
            super(crashLocation, crashType);
        }

        public boolean shouldAdd(AbstractRecord a)  {
            return true;
        }
    }
    static class RecoverableCrashRecordTypeMap implements RecordTypeMap {
        public Class<RecoverableCrashRecord> getRecordClass ()
        {
            return RecoverableCrashRecord.class;
        }

        public int getType() {
            // the type must be registered in the RecordType registry which it will be if RecoverableCrashRecord
            // uses RecordType.USER_DEF_FIRST0 which it does
            int recordType = new RecoverableCrashRecord().typeIs();

            Assertions.assertEquals(RecordType.USER_DEF_FIRST0, recordType);

            return recordType;
        }
    }

    @Test
    public void testRecovery() {
        Uid uid = new Uid();
        AtomicAction aa = new AtomicAction(uid);

        aa.begin();

        RecordTypeManager.manager().add(new RecoverableCrashRecordTypeMap());

        aa.add(new RecoverableCrashRecord(CrashRecord.CrashLocation.NoCrash, CrashRecord.CrashType.Normal));
        aa.add(new RecoverableCrashRecord(CrashRecord.CrashLocation.CrashInCommit, CrashRecord.CrashType.Normal));

        Assertions.assertEquals(ActionStatus.COMMITTED, aa.commit(true)); // normal crash in commit

        // verify that the cache has the record
        Assertions.assertEquals(1, store.manager().getCache(CLUSTER_NAME).size());
        // and that there is something in the store
        try {
            recoveryStore.read_committed(aa.getSavingUid(), aa.type());
        } catch (ObjectStoreException e) {
            fail(e); // record should be available in the recovery store
        }

        /*
         * the recovery scan should find the atomic action with one prepared record which will be recovered via the
         * AtomicActionRecoveryModule.doRecoverTransaction logic which recreates the second RecoverableCrashRecord
         * using the empty constructor (via AbstractRecord.create) which does not set any CrashRecord.CrashLocation
         * so the commit will be replayed successfully:
         */
        manager.startRecoveryManagerThread(); // start periodic recovery
        manager.scan();

        // verify that the atomic action was recovered:
        try {
            recoveryStore.read_committed(aa.getSavingUid(), aa.type());
            fail("the recovery scan should have committed the action");
        } catch (ObjectStoreException ignore) {
        }
    }
}