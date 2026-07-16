/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreKey;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.ByteArrayKey;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import org.jgroups.blocks.Cache;
import org.jgroups.blocks.ReplCache;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/*
 * Tests configuring a JGroupsSlots store using standard jbossts and jgroups xml config files
 * (as opposed to the other tests which all use programmatic config)
 */
public class JGroupsConfigFromFileTest extends JGroupsTestBase {
    private static final String JBOSSTS_CONFIG_FILE = "jgroups-jbossts-properties.xml";
    // the next three values MUST correspond to the values configured in JBOSSTS_CONFIG_FILE
    private static final String EXPECTED_CLUSTER_NAME_FROM_CONFIG = "jGroupsClusteredObjectStore";
    private static final String EXPECTED_SLOT_KEY_GENERATOR = SharedSlotKeyGenerator.class.getName();

    @BeforeAll
    public static void setupStore() {
        // define the jbossts properties file containing the config for a JGroupsSlot store
        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", JBOSSTS_CONFIG_FILE);
    }

    @BeforeEach
    public void before() {
        // look up the bean that is created from the jbossts properties file
        JGroupsStoreEnvironmentBean config = BeanPopulator.getDefaultInstance(JGroupsStoreEnvironmentBean.class);

        removeDirectory(config.getStoreDir());

        // verify that the config used JBOSSTS_CONFIG_FiLE
        assertEquals(JGROUPS_CONFIG_FILE, config.getJGroupsConfigFileName());
        assertEquals(EXPECTED_CLUSTER_NAME_FROM_CONFIG, config.getClusterName());
        assertEquals(EXPECTED_SLOT_KEY_GENERATOR, config.getSlotKeyGeneratorClassName());
    }

    @Test
    public void test() {
        // look up one of the beans that is created from the jbossts properties file
        JGroupsStoreEnvironmentBean config = BeanPopulator.getDefaultInstance(JGroupsStoreEnvironmentBean.class);
        String VALUE = "hello1";

        // check that the store works as a recovery store
        Uid uid = new Uid();
        String TYPE_NAME = "/StateManager/junit1";

        writeSomething(uid, TYPE_NAME, VALUE);

        String data = readSomething(uid, TYPE_NAME);
        assertEquals(VALUE, data);

        /*
         * for bonus points see if the raw JGroups cache has the data
         */
        try {
            ReplCache<ByteArrayKey, byte[]> cache = config.getCache();

            assertNotNull(cache);

            // check that the cache was configured with the expected config
            assertEquals(config.getJGroupsConfigFileName(), cache.getProps());

            int size = cache.getL2Cache().getSize();

            assertEquals(1, size); // writeSomething should only write one record

            // read the entries in the cache
            Set<Map.Entry<ByteArrayKey, Cache.Value<ReplCache.Value<byte[]>>>> entries = cache.getL2Cache().entrySet();
            assertEquals(size, entries.size());

            // find the record
            Optional<Map.Entry<ByteArrayKey, Cache.Value<ReplCache.Value<byte[]>>>> first = entries.stream().findFirst();
            ByteArrayKey key = first.map(Map.Entry::getKey).orElse(null);
            byte[] bytes = key != null ? cache.get(key) : null;

            if (bytes == null) {
                fail("missing payload");
            }

            try {
                // the value read from the cache should correspond to SlotStoreKey (Uid + typeName + status)
                // followed by the payload (the actual data):
                InputBuffer inputBuffer = new InputBuffer(bytes);
                // the initial part of the buffer contains the SlotStoreKey
                SlotStoreKey slotStoreKey = SlotStoreKey.unpackFrom(inputBuffer);

                assertEquals(uid, slotStoreKey.getUid());
                assertEquals(TYPE_NAME, slotStoreKey.getTypeName());

                InputObjectState payload = new InputObjectState();
                // the remaining part of the inputBuffer should hold the actual record data
                payload.unpackFrom(inputBuffer);
                String payloadData = payload.unpackString();
                assertEquals(VALUE, payloadData);
            } catch (IOException e) {
                fail(e.getMessage());
            }
        } catch (CoreEnvironmentBeanException e) {
            fail(e);
        } finally {
            assertTrue(removeSomething(uid, TYPE_NAME));
            try {
                StoreManager.getRecoveryStore().read_committed(uid, TYPE_NAME);
                fail("record should have been removed from the store");
            } catch (ObjectStoreException ignore) {
            }
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
