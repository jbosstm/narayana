/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests JGroups object store cluster formation and basic replication.
 */
public class JGroupsClusterTest extends JGroupsTestBase {

    private static final String AA_TYPE_NAME = "StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction";

    private JGroupsTestBase.Store store1;
    private JGroupsTestBase.Store store2;

    @AfterEach
    public void tearDown() {
        if (store1 != null) {
            store1.stop();
        }

        if (store2 != null) {
            store2.stop();
        }

        StoreManager.shutdown();

        removeDirectory(STORE_DIR);
    }

    /**
     * Test that a JGroups-backed store can start and perform basic write/read operations
     */
    @Test
    public void testBasicStoreOperations() throws Throwable {
        String clusterName = "basic-test-" + System.currentTimeMillis();
        store1 = createStore("node1", clusterName, STORE_DIR + "/basic/node1");

        // Add listener before start
        ViewChangeListener listener = new ViewChangeListener(1);
        store1.config().getCache().addReceiver(listener);

        store1.start();
        assertTrue(listener.await(10, TimeUnit.SECONDS), "Store didn't start and join cluster");

        int clusterSize = store1.config().getCache().getClusterSize();
        assertEquals(1, clusterSize, "Expected to see 1 node in cluster");

        // Get the recovery store and perform basic operations
        resetAtomicActionRecoveryModule();
        RecoveryStore recoveryStore = startRecoveryStore(store1.config());

        var uid = new Uid();
        OutputObjectState data = new OutputObjectState();
        String VALUE = "hello1";
        data.packString(VALUE);

        // write and then read back the data
        assertTrue(recoveryStore.write_committed(uid, AA_TYPE_NAME, data), "Write failed");
        var result = recoveryStore.read_committed(uid, AA_TYPE_NAME);
        assertNotNull(result, "Should be able to read back written data");
        assertEquals(VALUE, result.unpackString(), "Data mismatch");

        // clean up
        assertTrue(recoveryStore.remove_committed(uid, AA_TYPE_NAME), "Remove failed");

        try {
            assertNull(recoveryStore.read_committed(uid, AA_TYPE_NAME), "Removed record still exists");
        } catch (ObjectStoreException ignore) {
        }
    }

    /**
     * Form a cluster of two nodes and test that data written to one node is replicated to other node
     * <p>
     * This test works around the RecoveryStore singleton limitation as follows:
     * 1. Create two JGroups caches and wait for them to form a cluster
     * 2. Start the RecoveryStore singleton with cache1 and write a record to the store
     * 3. Shut down the RecoveryStore
     * 4. Start the RecoveryStore with cache2 and read data written during step 2
     * 5. Verify that the read succeeds, thereby proving that object store writes are replicated
     */
    @Test
    public void testTwoNodeReplication() throws Throwable {
        String clusterName = "replication-test-" + System.currentTimeMillis(); // unique cluster names

        // Create two stores in the same cluster
        store1 = createStore("node1", clusterName, STORE_DIR + "/replication/node1");
        store2 = createStore("node2", clusterName, STORE_DIR + "/replication/node2");

        // Configure and add listener to cache1
        ViewChangeListener listener1 = new ViewChangeListener(1);
        store1.config().getCache().addReceiver(listener1);

        // Start cache1 and wait for it to see itself
        store1.config().getCache().start();
        assertTrue(listener1.await(10, TimeUnit.SECONDS), "node1 should start");
        assertEquals(1, store1.config().getCache().getClusterSize(), "node1 should see cluster size 1");

        // Configure cache2 and add listener expecting size 2
        ViewChangeListener listener2 = new ViewChangeListener(2);
        store2.config().getCache().addReceiver(listener2);

        // Start cache2 and wait for cluster to form
        store2.config().getCache().start();
        assertTrue(listener2.await(10, TimeUnit.SECONDS), "Cluster should form with 2 nodes");

        // now wait for both caches to see each other
        waitFor(REPLICATION_TIMEOUT_MS, "both nodes see cluster size 2",
            () -> store1.config().getCache().getClusterSize() == 2
               && store2.config().getCache().getClusterSize() == 2);

        // now the cluster is ready simulate writing to one store and reading from the other:

        // Step 1: write data using a RecoveryStore backed by cache1
        resetAtomicActionRecoveryModule();
        RecoveryStore recoveryStore1 = startRecoveryStore(store1.config());

        Uid uid = new Uid();
        OutputObjectState writeData = new OutputObjectState();
        String DATA = "hello-from-node1";
        writeData.packString(DATA);

        assertTrue(recoveryStore1.write_committed(uid, AA_TYPE_NAME, writeData), "Write to node1 failed");

        // wait a while for the data to replicate to the other node
        assertDoesNotThrow(() -> TimeUnit.MILLISECONDS.sleep(100), "interrupted");

        // Step 2: shutdown the RecoveryStore and recreate it with cache2
        StoreManager.shutdown();
        resetAtomicActionRecoveryModule();

        RecoveryStore recoveryStore2 = startRecoveryStore(store2.config());

        // Step 3: Read from RecoveryStore backed by cache2
        InputObjectState readData = recoveryStore2.read_committed(uid, AA_TYPE_NAME);
        assertNotNull(readData, "Data should be replicated to node2");
        assertEquals(DATA, readData.unpackString(), "Replicated data mismatch");
    }
}
