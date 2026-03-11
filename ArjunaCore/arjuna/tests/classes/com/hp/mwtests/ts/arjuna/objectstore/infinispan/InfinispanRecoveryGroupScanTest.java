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
import org.infinispan.commons.marshall.WrappedByteArray;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.distribution.group.Grouper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.fail;

public class InfinispanRecoveryGroupScanTest extends InfinispanTestBase {

    private static RecoveryManager manager;
    private static Store store;
    private static RecoveryStore recoveryStore;
    private static RecoveryGrouper recoveryGrouper;

    @BeforeAll
    public static void beforeAll() {
        // the recovery system needs to know how to create instantiates of RecoverableCrashRecord
        RecordTypeManager.manager().add(new RecoverableCrashRecordTypeMap());
        // key grouping strategy
        recoveryGrouper = new RecoveryGrouper();
    }

    /*
     * Define a strategy for grouping keys, ie for co-locating a group of entries on the same nodes.
     * This means that keys managed by a particular recovery manager are co-located
     */
    static class RecoveryGrouper implements Grouper<WrappedByteArray> {

        @Override
        public Object computeGroup(WrappedByteArray key, Object group) {
            // group holds the group as currently computed, or null if no group has been determined yet
            String k = new String(key.getBytes());

            Matcher matcher = ClusterMemberId.CB_DELIMITER_REGEX.matcher(k);
            if (matcher.find()) {
                return matcher.group(1);
            }

            return "";
        }

        @Override
        public Class<WrappedByteArray> getKeyType() {
            return WrappedByteArray.class; //byte[].class;
        }
    }

    private RecoveryStore startRecoveryStore(String nodeName, String groupName) {
        stopRecoveryStore();
        RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager.getRecoveryEnvironmentBean();
        store = new Store(
                createCacheManager(nodeName, CacheMode.REPL_SYNC, -1, recoveryGrouper, true, false),
                groupName, nodeName);
        // store.start(); // startRecoveryStore initialise the store
        recoveryStore = startRecoveryStore(store.config());

        recoveryEnvironmentBean.setRecoveryBackoffPeriod(1); // use a short interval between passes
        recoveryEnvironmentBean.setRecoveryListener(true); // configure the RecoveryMonitor

        manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        try {
            resetAtomicActionRecoveryModule();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        try {
            resetAtomicActionRecoveryModule();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        AtomicActionRecoveryModule aaRecoveryModule = new AtomicActionRecoveryModule();
        manager.addModule(aaRecoveryModule); // we only need to test the XARecoveryModule

        return recoveryStore;
    }

    public static void stopRecoveryStore() {
        if (recoveryStore != null) {
            recoveryStore.stop();
            recoveryStore = null;
        }
        if (store != null) {
            store.stop();
            store = null;
        }
        if (manager != null) {
            manager.terminate();
            manager = null;
        }
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
    public void testRecovery() throws Exception {
        // start a recovery store on node1 filtering by group1
        startRecoveryStore("node1", "group1");
        store.manager().getCache(CLUSTER_NAME).clear(); // make sure it's clean
        // verify that the cache that backs the recovery store is empty
        Assertions.assertEquals(0, store.manager().getCache(CLUSTER_NAME).keySet().size());
        AtomicAction aa1 = createAnInDoubtAction();
        Assertions.assertTrue(containsAtomicAction(recoveryStore, aa1));
        // verify that the cache that backs the recovery store contain one entry
        Assertions.assertEquals(1, store.manager().getCache(CLUSTER_NAME).keySet().size());
        stopRecoveryStore();

        // start a recovery store on node1 filtering by group2
        startRecoveryStore("node1", "group2");
        // verify that the cache that backs the recovery store has 1 entry
        Assertions.assertEquals(1, store.manager().getCache(CLUSTER_NAME).keySet().size());
        AtomicAction aa2 = createAnInDoubtAction();
        Assertions.assertTrue(containsAtomicAction(recoveryStore, aa2));
        // the store should not report actions created by the store that filters by group2
        Assertions.assertFalse(containsAtomicAction(recoveryStore, aa1));
        // verify that the cache that backs the recovery store contain a second entry
        Assertions.assertEquals(2, store.manager().getCache(CLUSTER_NAME).keySet().size());
        stopRecoveryStore();

        // recover aa1
        startRecoveryStore("node1", "group1");
        manager.startRecoveryManagerThread(); // start periodic recovery
        manager.scan();
        Assertions.assertFalse(containsAtomicAction(recoveryStore, aa1));
        // verify that cache size was reduced by 1 after recovery was run for both stores
        Assertions.assertEquals(1, store.manager().getCache(CLUSTER_NAME).keySet().size());
        stopRecoveryStore();

        // recover aa2
        startRecoveryStore("node1", "group2");
        manager.startRecoveryManagerThread(); // start periodic recovery
        manager.scan();
        Assertions.assertFalse(containsAtomicAction(recoveryStore, aa2));
        // verify that cache is empty after recovery was run for both stores
        Assertions.assertEquals(0, store.manager().getCache(CLUSTER_NAME).keySet().size());
        stopRecoveryStore();
    }

    private AtomicAction createAnInDoubtAction() {
        int size = store.manager().getCache(CLUSTER_NAME).size();
        Uid uid = new Uid();
        AtomicAction aa = new AtomicAction(uid);

        aa.begin();

        aa.add(new RecoverableCrashRecord(CrashRecord.CrashLocation.NoCrash, CrashRecord.CrashType.Normal));
        aa.add(new RecoverableCrashRecord(CrashRecord.CrashLocation.CrashInCommit, CrashRecord.CrashType.Normal));

        Assertions.assertEquals(ActionStatus.COMMITTED, aa.commit(true)); // normal crash in commit

        // verify that the cache has the record
        Assertions.assertEquals(size + 1, store.manager().getCache(CLUSTER_NAME).size());
        // and that there is something in the store
        try {
            recoveryStore.read_committed(aa.getSavingUid(), aa.type());
        } catch (ObjectStoreException e) {
            fail(e); // record should be available in the recovery store
        }

        return aa;
    }
}