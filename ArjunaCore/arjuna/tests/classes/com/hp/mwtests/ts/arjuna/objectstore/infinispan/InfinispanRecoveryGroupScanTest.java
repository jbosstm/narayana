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
import org.infinispan.commons.marshall.WrappedByteArray;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.distribution.group.Grouper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.fail;

/*
 * tests isolation of records when running recovery
 */
public class InfinispanRecoveryGroupScanTest extends InfinispanRecoveryTestBase {

    private static RecoveryGrouper recoveryGrouper;

    @BeforeAll
    public static void beforeAll() {
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

            return null;
        }

        @Override
        public Class<WrappedByteArray> getKeyType() {
            return WrappedByteArray.class; //byte[].class;
        }
    }

    private void restartRecoverySystem(String nodeName, String groupName)  {
        stopRecoveryStore();
        store = new Store(
                createCacheManager(nodeName, CacheMode.REPL_SYNC, -1, recoveryGrouper, true, false),
                groupName, nodeName);
        startRecoverySystem(store);
    }

    @Test
    public void testRecovery() {
        // start a recovery store on node1 filtering by group1
        restartRecoverySystem("node1", "group1");
        store.manager().getCache(CLUSTER_NAME).clear(); // make sure it's clean
        // verify that the cache that backs the recovery store is empty
        Assertions.assertEquals(0, store.manager().getCache(CLUSTER_NAME).size());
        // create an in-doubt action (causes an entry to be added to the store)
        AtomicAction aa1 = createAnInDoubtAction();
        Assertions.assertTrue(containsAtomicAction(recoveryStore, aa1));
        // verify that the cache that backs the recovery store increased by one entry
        Assertions.assertEquals(1, store.manager().getCache(CLUSTER_NAME).size());
        stopRecoveryStore();

        // start a recovery store on node1 filtering by group2
        restartRecoverySystem("node1", "group2");
        // verify that the cache that backs the recovery store has 1 entry
        Assertions.assertEquals(1, store.manager().getCache(CLUSTER_NAME).size());
        // create another in-doubt action (causes an entry to be added to the store)
        AtomicAction aa2 = createAnInDoubtAction();
        Assertions.assertTrue(containsAtomicAction(recoveryStore, aa2));
        // the store should not report actions created by the store that filters by group1
        Assertions.assertFalse(containsAtomicAction(recoveryStore, aa1));
        // verify that the cache that backs the recovery store contains a second entry
        Assertions.assertEquals(2, store.manager().getCache(CLUSTER_NAME).size());
        stopRecoveryStore();

        // recover aa1
        restartRecoverySystem("node1", "group1");
        manager.startRecoveryManagerThread(); // start periodic recovery
        manager.scan();
        Assertions.assertFalse(containsAtomicAction(recoveryStore, aa1));
        // verify that cache size was reduced by 1 after recovery ran for the first config
        Assertions.assertEquals(1, store.manager().getCache(CLUSTER_NAME).size());
        stopRecoveryStore();

        // recover aa2
        restartRecoverySystem("node1", "group2");
        manager.startRecoveryManagerThread(); // start periodic recovery
        manager.scan();
        Assertions.assertFalse(containsAtomicAction(recoveryStore, aa2));
        // verify that cache is empty after recovery was run for both store configs
        Assertions.assertEquals(0, store.manager().getCache(CLUSTER_NAME).size());
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