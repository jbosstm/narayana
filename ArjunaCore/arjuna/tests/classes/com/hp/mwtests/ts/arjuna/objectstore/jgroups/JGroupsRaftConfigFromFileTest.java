/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests configuring a JGroupsRaftSlots store using standard jbossts xml config files
 * (as opposed to the other tests which mostly use programmatic config).
 *
 * <p>Verifies that {@link BeanPopulator#configureFromProperties} correctly populates both
 * {@link JGroupsRaftStoreEnvironmentBean} (Raft-specific fields) and
 * {@link SlotStoreEnvironmentBean} (base slot store fields). This exercises the flattened
 * inheritance hierarchy where {@code JGroupsRaftStoreEnvironmentBean} extends
 * {@code SlotStoreEnvironmentBean} directly, so {@code getDeclaredFields()} discovers
 * the Raft fields under the {@code JGroupsRaftStoreEnvironmentBean.*} property prefix.
 */
public class JGroupsRaftConfigFromFileTest extends JGroupsTestBase {
    private static final String JBOSSTS_CONFIG_FILE = "jgroups-raft-jbossts-properties.xml";
    private static final String EXPECTED_CLUSTER_NAME = "raftClusteredObjectStore";
    private static final String EXPECTED_CACHE_NAME = "raftClusteredCache";
    private static final String EXPECTED_NODE_ADDRESS = "node1";

    @BeforeAll
    public static void setupStore() {
        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", JBOSSTS_CONFIG_FILE);
    }

    @AfterAll
    public static void teardownStore() {
        StoreManager.shutdown();
        System.clearProperty("com.arjuna.ats.arjuna.common.propertiesFile");
        JGroupsRaftStoreEnvironmentBean config = BeanPopulator.getDefaultInstance(JGroupsRaftStoreEnvironmentBean.class);
        removeDirectory(config.getStoreDir());
    }

    @BeforeEach
    public void before() {
        JGroupsRaftStoreEnvironmentBean config = BeanPopulator.getDefaultInstance(JGroupsRaftStoreEnvironmentBean.class);

        removeDirectory(config.getStoreDir());

        // verify that the config used JBOSSTS_CONFIG_FILE
        assertEquals(EXPECTED_CLUSTER_NAME, config.getClusterName());
        assertEquals(EXPECTED_NODE_ADDRESS, config.getNodeAddress());
    }

    @Test
    public void test() {
        // verify JGroupsRaftStoreEnvironmentBean properties were loaded from config file
        JGroupsRaftStoreEnvironmentBean raftConfig = BeanPopulator.getDefaultInstance(JGroupsRaftStoreEnvironmentBean.class);

        assertEquals(EXPECTED_NODE_ADDRESS, raftConfig.getNodeAddress());
        assertEquals(EXPECTED_CLUSTER_NAME, raftConfig.getClusterName());
        assertEquals(EXPECTED_CACHE_NAME, raftConfig.getCacheName());
        assertEquals(EXPECTED_NODE_ADDRESS, raftConfig.getRaftMembers());
        assertFalse(raftConfig.isRaftLogFsync(), "raftLogFsync should be false per config");
        assertTrue(raftConfig.isAllowDirtyReads(), "allowDirtyReads should be true per config");
        assertEquals(10_000, raftConfig.getRaftTimeout());
        assertEquals(1000, raftConfig.getRaftElectionMaxInterval());

        assertEquals("jgroups-raft-alt-config.xml", raftConfig.getJGroupsConfigFileName(),
                "jGroupsConfigFileName should be set from properties file");

        // verify SlotStoreEnvironmentBean base properties were loaded (separate BeanPopulator instance,
        // configured with SlotStoreEnvironmentBean.* prefix keys from the same properties file)
        SlotStoreEnvironmentBean slotConfig = BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);
        assertEquals(256, slotConfig.getNumberOfSlots());
        assertEquals(4096, slotConfig.getBytesPerSlot());
        assertEquals(JGroupsRaftSlots.class.getName(), slotConfig.getBackingSlotsClassName());

        // write/read/remove round-trip through the recovery store
        String VALUE = "raft-hello";
        Uid uid = new Uid();
        String TYPE_NAME = "/StateManager/raftJunit1";

        writeSomething(uid, TYPE_NAME, VALUE);

        String data = readSomething(uid, TYPE_NAME);
        assertEquals(VALUE, data);

        assertTrue(removeSomething(uid, TYPE_NAME));

        try {
            StoreManager.getRecoveryStore().read_committed(uid, TYPE_NAME);
            fail("record should have been removed from the store");
        } catch (ObjectStoreException ignore) {
        }
    }

    private void writeSomething(Uid uid, String typeName, String data) {
        OutputObjectState oos = new OutputObjectState();

        try {
            oos.packString(data);

            assertTrue(StoreManager.getRecoveryStore().write_committed(uid, typeName, oos));
        } catch (IOException | ObjectStoreException e) {
            fail(e.getMessage());
        }
    }

    private String readSomething(Uid uid, String typeName) {
        try {
            return StoreManager.getRecoveryStore().read_committed(uid, typeName).unpackString();
        } catch (ObjectStoreException | IOException e) {
            fail(e.getMessage());
            return null;
        }
    }

    private boolean removeSomething(Uid uid, String typeName) {
        try {
            return StoreManager.getRecoveryStore().remove_committed(uid, typeName);
        } catch (ObjectStoreException e) {
            fail(e.getMessage());
            return false;
        }
    }
}
