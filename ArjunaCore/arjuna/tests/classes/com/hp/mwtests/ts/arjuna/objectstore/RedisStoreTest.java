/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.slot.redis.CloudId;
import com.arjuna.ats.internal.arjuna.objectstore.slot.redis.RedisSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.redis.RedisStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import redis.clients.jedis.ConnectionPool;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedisStoreTest {
    private static final Logger log = Logger.getLogger(RedisStoreTest.class);
    private final static String typeName = "/StateManager/junit";
    private final static String nodeId = "test-node";
    private static RedisStoreEnvironmentBean redisConfig;
    private static String redisHost;
    private static int redisPort;
    private static HostAndPort hostAndPort;
    private static RedisSlots redisSlots;
    private static RecoveryStore recoveryStore;
    private static boolean clustered;
    private static Boolean redisAvailable = null;

    private static boolean isRedisRunning() {
        if (redisAvailable != null) {
            return redisAvailable;
        }

        if (clustered) {
            try (JedisCluster jedisCluster = new JedisCluster(hostAndPort)) {
                for (ConnectionPool node : jedisCluster.getClusterNodes().values()) {
                    try (Jedis ignore = new Jedis(node.getResource())) {
                        redisAvailable = true;
                        break;
                    }
                }
            } catch (Exception e) {
                redisAvailable = false;
                log.warnf("Skipping RedisStoreTests because Redis is not running on the configured endpoint %s:%d",
                        redisHost, redisPort);
            }
        } else {
            try (JedisPool jedisPool = new JedisPool(redisHost, redisPort)) {
                try (Jedis ignore = jedisPool.getResource()) {
                    redisAvailable = true;
                }
            }
        }

        try (Jedis ignored = new Jedis(redisConfig.getRedisHost(), redisConfig.getRedisPort())) {
            redisAvailable = true;
        } catch (Exception e) {
            redisAvailable = false;
            log.warnf("Skipping RedisStoreTests because Redis is not running on the configured endpoint %s:%d",
                    redisHost, redisPort);
        }

        return redisAvailable;
    }

    @BeforeAll
    public static void before() throws CoreEnvironmentBeanException {
        redisConfig = BeanPopulator.getDefaultInstance(RedisStoreEnvironmentBean.class);
        redisSlots = new RedisSlots();

        hostAndPort = new HostAndPort(redisConfig.getRedisHost(), redisConfig.getRedisPort());

        redisConfig.setBackingSlots(redisSlots);
        redisConfig.setClustered(true);

        clustered = redisConfig.isClustered();
        redisHost = redisConfig.getRedisHost();
        redisPort = redisConfig.getRedisPort();

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).
                setObjectStoreType(SlotStoreAdaptor.class.getName());
        BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class).
                setNodeIdentifier(nodeId);

        recoveryStore = StoreManager.getRecoveryStore();
    }

    private static class RecordHolder {
        String typeName;
        Uid uid;
        OutputObjectState outputObjectState;
        String data;
        boolean typeFound;
        boolean uidFound;

        public RecordHolder(String typeName, String data) throws IOException {
            this.typeName = typeName;
            this.uid = new Uid();
            this.outputObjectState = new OutputObjectState();
            this.data = data;
            this.typeFound = false;
            this.uidFound = false;

            this.outputObjectState.packString(data);
        }
    }

    // verify that a value written with different type names have the same value when read back
    @Test
    public void test1 () throws Exception {
        if (!isRedisRunning()) {
            return;
        }

        RecordHolder[] records = {
            new RecordHolder("/StateManager/junit1", "hello1"),
            new RecordHolder("/StateManager/junit2", "hello2")
        };

        for (RecordHolder record: records) {
            // add the record and read it back again
            Assertions.assertTrue(recoveryStore.write_committed(record.uid, record.typeName, record.outputObjectState));
            InputObjectState inputData = recoveryStore.read_committed(record.uid, record.typeName);

            String s = inputData.unpackString();
            assertEquals(record.data, s);//inputData.unpackString());
        }

        // clean up
        for (RecordHolder record: records) {
            recoveryStore.remove_committed(record.uid, record.typeName);
        }
    }

    // verify that writing a record increments the number of values in the store correctly
    @Test
    public void test2 () throws Exception {
        if (!isRedisRunning()) {
            return;
        }

        RecordHolder[] records = {
            new RecordHolder(typeName, "hello1"),
            new RecordHolder(typeName, "hello2")
        };

        int count = countUids(recoveryStore, typeName);

        for (RecordHolder record: records) {
            // add the record and read it back again
            Assertions.assertTrue(recoveryStore.write_committed(record.uid, record.typeName, record.outputObjectState));
            InputObjectState inputData = recoveryStore.read_committed(record.uid, record.typeName);

            assertEquals(record.data, inputData.unpackString());
        }

        // verify that the record count increased by
        assertEquals(count + records.length, countUids(recoveryStore, typeName));

        // clean up
        for (RecordHolder record: records) {
            recoveryStore.remove_committed(record.uid, record.typeName);
        }
    }

    @Test
    public void testAllTypes () throws Exception {
        if (!isRedisRunning()) {
            return;
        }

        RecordHolder[] records = {
            new RecordHolder("/StateManager/junit1", "hello1"),
            new RecordHolder("/StateManager/junit2", "hello2")
        };

        for (RecordHolder record : records) {
            addUid(recoveryStore, record.typeName, record.uid, record.data);
        }

        InputObjectState types = new InputObjectState();

        Assertions.assertTrue(recoveryStore.allTypes(types));

        // find all types and all uids and verify the ones that we just added are present
        while (true) {
            String tn = types.unpackString(); // extract a type

            Assertions.assertNotNull(tn);

            if ( tn.compareTo("") == 0 ) {
                break; // end of returned types
            }

            // see if tn is one of the ones we just created
            for (RecordHolder record : records) {
                if (record.typeName.endsWith(tn)) { // the leading slash is stripped off the returned types
                    record.typeFound = true;
                }
            }

            InputObjectState uids = new InputObjectState();

            Assertions.assertTrue(recoveryStore.allObjUids(tn, uids));

            // unpack uids
            while (true) {
                Uid uid = UidHelper.unpackFrom(uids); // extract a uid

                if (uid.equals( Uid.nullUid() )) {
                    break;
                }

                for (RecordHolder record : records) {
                    if (record.uid.equals(uid)) {
                        record.uidFound = true;
                    }
                }
            }
        }

        // verify that the store contained all the expected types and all the expected uids of each type
        for (RecordHolder record : records) {
            recoveryStore.remove_committed(record.uid, record.typeName); // clean up

            Assertions.assertTrue(record.typeFound);
            Assertions.assertTrue(record.uidFound);
        }
    }

    @Test
    public void testAllObjUids() throws ObjectStoreException {
        if (!isRedisRunning()) {
            return;
        }

        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        InputObjectState ios = new InputObjectState();
        Assertions.assertTrue(recoveryStore.allObjUids(typeName, ios));
    }

    @Test
    public void testMove() throws ObjectStoreException, IOException {
        if (!isRedisRunning()) {
            return;
        }

        /*
         * test that records "owned" by nodeId can be moved to a different recovery manager
         */
        SlotStoreEnvironmentBean env = BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);
        RedisSlots impl = (RedisSlots) env.getBackingSlots();
        Uid uid = new Uid();
        OutputObjectState obuff = new OutputObjectState();
        String toNodeId = "migration-node";
        String value = "value";
        Set<String> keysBefore, keysAfter;
        String keyPattern = nodeId + ":*";

        obuff.packString(value);

        // write two records
        Assertions.assertTrue(recoveryStore.write_committed(uid, typeName, obuff));
        Assertions.assertTrue(recoveryStore.write_committed(new Uid(), typeName, obuff));
        // check that the uid is in the store - after the move the read will fail
        Assertions.assertNotNull(recoveryStore.read_committed(uid, typeName));

        keysBefore = getKeys(keyPattern);
//////
/*        try (JedisPool jedisPool = new JedisPool("localhost", redisPort)) {
            try (Jedis jedis = jedisPool.getResource()) {
                keysBefore = jedis.keys(keyPattern);
            }
        }*/

//////
        CloudId from = new CloudId(nodeId);
        CloudId to = new CloudId(toNodeId);
        Assertions.assertTrue(impl.migrate(from, to));

        // the slot store needs re-initialising in order to regenerate its keys
        shutdownStoreManager(); // restartRecoveryManager();
        recoveryStore = StoreManager.getRecoveryStore();

        // verify that the record is no longer present with the original key (remark: RedisSlots.init()
        // only loads keys corresponding to this nodeId, ignoring ones corresponding to toNodeId)
        RecoveryStore finalRecoveryStore = recoveryStore;
        Assertions.assertThrows(ObjectStoreException.class, () -> {
            InputObjectState buff = finalRecoveryStore.read_committed(uid, typeName); // should throw an exception
            assertEquals(value, buff.unpackString()); // should not be reached
        });

        keysAfter = delKeys(toNodeId + ":*"); // clean up

/*        try (JedisPool jedisPool = new JedisPool("localhost", redisPort)) {
            try (Jedis jedis = jedisPool.getResource()) {
                keyPattern = toNodeId + ":*";

                keysAfter = jedis.keys(keyPattern);

                // clean up
                for (String key : keysAfter) {
                    jedis.del(key);
                }
            }
        }*/

        // compare keysBefore and keysAfter (all keys in the keysBefore set should have
        // been moved and have a key prefix corresponding to the new nodeId)
        Assertions.assertEquals(keysBefore.size(), keysAfter.size());
    }

    //    @Test
    public void testMovex() throws ObjectStoreException, IOException {
        if (!isRedisRunning()) {
            return;
        }

        /*
         * test that records "owned" by nodeId can be moved to a different recovery manager
         */
        SlotStoreEnvironmentBean env = BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);
        RedisSlots impl = (RedisSlots) env.getBackingSlots();
        Uid uid = new Uid();
        OutputObjectState obuff = new OutputObjectState();
        String toNodeId = "migration-node";
        String value = "value";
        Set<String> keysBefore, keysAfter;
        String keyPattern = nodeId + ":*";

        obuff.packString(value);

        // write two records
        Assertions.assertTrue(recoveryStore.write_committed(uid, typeName, obuff));
        Assertions.assertTrue(recoveryStore.write_committed(new Uid(), typeName, obuff));
        // check that the uid is in the store - after the move the read will fail
        Assertions.assertNotNull(recoveryStore.read_committed(uid, typeName));

        keysBefore = getKeys(keyPattern);
//////
/*        try (JedisPool jedisPool = new JedisPool("localhost", redisPort)) {
            try (Jedis jedis = jedisPool.getResource()) {
                keysBefore = jedis.keys(keyPattern);
            }
        }*/

//////
        CloudId from = new CloudId(nodeId);
        CloudId to = new CloudId(toNodeId);
        Assertions.assertTrue(impl.migrate(from, to));

        // the slot store needs re-initialising in order to regenerate its keys
        shutdownStoreManager(); // restartRecoveryManager();
        recoveryStore = StoreManager.getRecoveryStore();

        // verify that the record is no longer present with the original key (remark: RedisSlots.init()
        // only loads keys corresponding to this nodeId, ignoring ones corresponding to toNodeId)
        RecoveryStore finalRecoveryStore = recoveryStore;
        Assertions.assertThrows(ObjectStoreException.class, () -> {
            InputObjectState buff = finalRecoveryStore.read_committed(uid, typeName); // should throw an exception
            assertEquals(value, buff.unpackString()); // should not be reached
        });

        keysAfter = delKeys(toNodeId + ":*"); // clean up

/*        try (JedisPool jedisPool = new JedisPool("localhost", redisPort)) {
            try (Jedis jedis = jedisPool.getResource()) {
                keyPattern = toNodeId + ":*";

                keysAfter = jedis.keys(keyPattern);

                // clean up
                for (String key : keysAfter) {
                    jedis.del(key);
                }
            }
        }*/

        // compare keysBefore and keysAfter (all keys in the keysBefore set should have
        // been moved and have a key prefix corresponding to the new nodeId)
        Assertions.assertEquals(keysBefore.size(), keysAfter.size());
    }

    private Set<String> getKeys(String keyPattern) {
        Set<String> keySet = new HashSet<>();

        if (clustered) {
            try (JedisCluster jedisCluster = new JedisCluster(hostAndPort)) {

                for (ConnectionPool node : jedisCluster.getClusterNodes().values()) {
                    try (Jedis jedis = new Jedis(node.getResource())) {
                        keySet.addAll(jedis.keys(keyPattern));
                    }
                }
            }
        } else {
            try (JedisPool jedisPool = new JedisPool(redisHost, redisPort)) {
                try (Jedis jedis = jedisPool.getResource()) {

                    keySet.addAll(jedis.keys(keyPattern));
                }
            }
        }

        return keySet;
    }

    private Set<String> delKeys(String keyPattern) {
        Set<String> keySet = new HashSet<>();

        if (clustered) {
            try (JedisCluster jedisCluster = new JedisCluster(hostAndPort)) {

                for (ConnectionPool node : jedisCluster.getClusterNodes().values()) {
                    try (Jedis jedis = new Jedis(node.getResource())) {
                        keySet.addAll(jedis.keys(keyPattern));
                        jedis.keys(keyPattern).forEach(jedis::del);
                    }
                }
            }
        } else {
            try (JedisPool jedisPool = new JedisPool(redisHost, redisPort)) {
                try (Jedis jedis = jedisPool.getResource()) {

                    keySet = jedis.keys(keyPattern);

                    // clean up
                    for (String key : keySet) {
                        jedis.del(key);
                    }
                }
            }
        }

        return keySet;
    }

    private void addUid(RecoveryStore store, String type, Uid uid, String data) throws Exception {
        OutputObjectState state = new OutputObjectState();

        state.packBytes(data.getBytes());

        Assertions.assertTrue(store.write_committed(uid, type, state));
    }

    private int countUids(RecoveryStore rs, String ... typeNames) throws ObjectStoreException, IOException {
        int i = 0;

        for (String tn : typeNames) {
            ObjectStoreIterator iter = new ObjectStoreIterator(rs, tn);

            while (true) {
                Uid u = iter.iterate();

                if (Uid.nullUid().equals(u))
                    break;

                i += 1;
            }
        }

        return i;
    }

    private void shutdownStoreManager() {
        StoreManager.shutdown();

        redisSlots.fini();
    }
}
