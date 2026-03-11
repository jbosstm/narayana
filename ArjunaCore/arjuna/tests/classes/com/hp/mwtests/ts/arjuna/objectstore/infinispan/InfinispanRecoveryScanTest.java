/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.objectstore.infinispan;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.hp.mwtests.ts.arjuna.resources.CrashRecord;
import org.infinispan.configuration.cache.CacheMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/*
 * tests showing that periodic recovery can recover in doubt actions in the store
 */
public class InfinispanRecoveryScanTest extends InfinispanRecoveryTestBase {

    @BeforeAll
    public static void beforeAll() {
        store = new InfinispanTestBase.Store(
                createCacheManager("node1", CacheMode.REPL_SYNC, -1, null, false, false),
                null, "node1");

        startRecoverySystem(store);
    }

    @Test
    public void testRecovery() {
        Uid uid = new Uid();
        AtomicAction aa = new AtomicAction(uid);

        aa.begin();

//        RecordTypeManager.manager().add(new RecoverableCrashRecordTypeMap());

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