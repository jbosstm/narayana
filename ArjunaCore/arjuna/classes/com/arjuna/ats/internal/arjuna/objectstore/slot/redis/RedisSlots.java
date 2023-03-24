/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.arjuna.ats.internal.arjuna.objectstore.slot.redis;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputBuffer;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreKey;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Connection;
import redis.clients.jedis.ConnectionPool;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.io.SyncFailedException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Redis backed implementation of the SlotStore backend suitable for installations where nodes hosting the store
 * can come and go so is well suited for cloud based deployments of the Recovery Manager.
 *
 * In the context of the CAP theorem of distributed computing, the recovery store needs to behave as a CP system -
 * ie it needs to be able to tolerate network Partitions and yet continue to provide Strong Consistency.
 * Redis can provide the strong consistency guarantee if the RedisRaft module is used with Redis running as a
 * cluster. RedisRaft achieves consistency and partition tolerance by ensuring that:
 *
 * - acknowledged writes are guaranteed to be committed and never lost,
 * - reads will always return the most up-to-date committed write,
 * - the cluster is sized correctly (a RedisRaft cluster of 3 nodes can tolerate a single node failure and a cluster
 *   of 5 can tolerate 2 node failures, ... ie 2*N+1, where N is number of nodes the cluster can lose so the minimum
 *   cluster size should be 3). An odd number of nodes should be used in order to avoid "split brain" scenarios.
 *
 * The documentation at the RedisRaft github repository includes
 * <a href="https://github.com/RedisLabs/redisraft/blob/master/docs/Deployment.md#deploying-redisraft">
 *     instructions on setting up clusters</a>.
 *
 * The performance cost/benefit comparison between a standard redis cluster and redis raft cluster shows that
 * the complexities of guaranteeing strong consistency adds a 4-fold performance cost.
 *
 * This performance cost is just a baseline measure and optimisation work should improve upon it,
 * for example batching writes, in the manor of hornetq store perhaps using
 * (<a href="https://redis.io/docs/manual/pipelining/">redis pipelines</a>) or otherwise.
 */
public class RedisSlots implements BackingSlots, SharedSlots {
    private CloudId cloudId;
    private byte[][] slots = null;
    private boolean clustered;
    private JedisPool jedisPool;
    private JedisCluster jedisCluster;
    private HostAndPort hostAndPort;

    @Override
    public void init(SlotStoreEnvironmentBean slotStoreConfig) throws IOException {
        if (slots != null) {
            if (tsLogger.logger.isInfoEnabled()) {
                tsLogger.logger.info("RedisSlots.init(): already initialised");
            }

            throw new IllegalStateException("already initialized");
        }

        RedisStoreEnvironmentBean env = BeanPopulator.getDefaultInstance(RedisStoreEnvironmentBean.class);
        String nodeId = BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class).getNodeIdentifier();

        slots = new byte[slotStoreConfig.getNumberOfSlots()][];
        cloudId = new CloudId(nodeId, env.getFailoverId());
        hostAndPort = new HostAndPort(env.getRedisHost(), env.getRedisPort());
        clustered = env.isClustered();

        Set<String> keys;

        if (clustered) {
            // provide one of the master instances (the others will be auto discovered)
            jedisCluster = new JedisCluster(hostAndPort);
            keys = loadClustered();
            // to enable pooling one needs to build the cluster by hand:
/*            Set<HostAndPort> jedisClusterNodes = new HashSet<> ();

            jedisClusterNodes.add(new HostAndPort("127.0.0.1", 30001));
            jedisClusterNodes.add(new HostAndPort("127.0.0.1", 30002));
            jedisClusterNodes.add(new HostAndPort("127.0.0.1", 30003));

            GenericObjectPoolConfig<Connection> jedisPoolConfig = new GenericObjectPoolConfig<> ();
            JedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder().timeoutMillis(50).build();

            jedisCluster = new JedisCluster(jedisClusterNodes, jedisClientConfig,2, jedisPoolConfig);*/
        } else {
            // nb jedis instances are single threaded
            jedisPool = new JedisPool(env.getRedisURI()); // pass in JedisClientConfig() based on the env bean
            keys = loadSingle();
        }

        load(keys);
    }

    private void initJedis2() {
        Set<HostAndPort> jedisClusterNodes = new HashSet<> ();

        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 30001));
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 30002));
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 30003));

        GenericObjectPoolConfig<Connection> jedisPoolConfig = new GenericObjectPoolConfig<> ();
        JedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder().timeoutMillis(50).build();

        jedisCluster = new JedisCluster(jedisClusterNodes, jedisClientConfig,2, jedisPoolConfig);
    }

    public void fini() {
        slots = null;

        if (clustered) {
            jedisCluster.close();
        } else {
            jedisPool.close(); // probably needs to be synchronised unless we can control when close gets called
        }
    }

    private Set<String> loadClustered() {
        Set<String> keys = new HashSet<>();

        for (ConnectionPool node : jedisCluster.getClusterNodes().values()) {
            try (Jedis j = new Jedis(node.getResource())) {
                // load keys matching this recovery manager
                Set<String> candidates = j.keys(cloudId.allKeysPattern());
                // filter out candidates that don't match this managers node id
                // Collection actuals = candidates.stream().filter(s -> s.matches(pattern)).collect(Collectors.toList());
                keys.addAll(candidates);
            }
        }

        return keys;
    }

    private Set<String> loadSingle() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(cloudId.allKeysPattern());
        }
    }

    private void load(Set<String> keys) {
        int i = 0;

        for (String key : keys) {
            if (i < slots.length) {
                slots[i] = key.getBytes(StandardCharsets.UTF_8);
                i += 1;
            } else {
                tsLogger.logger.infof("Too many redis keys: ignoring remaining keys from slot %d (key=%s)", i, key);
                break;
            }
        }

        // initialise the remaining slots
        while (i < slots.length) {
            // prefix the slot key with the cloudId and force keys for nodeId + failoverId into the same hash slot
            // (using the curly brace notation) so that they will be stored on the same redis node
            // In this way we can perform multikey operations on a slot
            // see https://redis.io/docs/reference/cluster-spec/ section "Key distribution model" for more info
//            slots[i] = String.format("{%s}:%s:%s:%d", cloudId.failoverGroupId, cloudId.nodeId, new Uid().stringForm(), i)
//                    .getBytes(StandardCharsets.UTF_8);
            slots[i] = cloudId.generateUniqueKey(i);
            i += 1;
        }
    }

    @Override
    public void write(int slot, byte[] data, boolean sync) throws IOException {
        if (!clustered) {
            try (Jedis jedis = jedisPool.getResource()) { // or use JedisPooled to avoid the try with resources
                if (!"OK".equals(jedis.set(slots[slot], data))) {
                    if (tsLogger.logger.isInfoEnabled()) {
                        tsLogger.logger.info("RedisSlots.write(): write failed");
                    }
                    throw new IOException("redis write failed for slot " + slot);
                }
            }
        } else {
            // note that the slot number is based ultimately based on uid , typeName and the status of the state
            if (!"OK".equals(jedisCluster.set(slots[slot], data))) {
                if (tsLogger.logger.isInfoEnabled()) {
                    tsLogger.logger.info("RedisSlots.write(): write failed");
                }
                throw new IOException("redis write failed for slot " + slot);
            }
        }
    }

    @Override
    public byte[] read(int slot) throws IOException {
        if (clustered) {
            return jedisCluster.get(slots[slot]);
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                return jedis.get(slots[slot]);
            }
        }
    }

    @Override
    public void clear(int slot, boolean sync) throws IOException {
        if (clustered) {
            jedisCluster.del(slots[slot]);
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.del(slots[slot]);
            }
        }
    }

    @Override
    public String getNodeId() {
        return cloudId.nodeId;
    }

    @Override
    public boolean migrate(CloudId from) {
        return migrate(from, cloudId);
    }

    @Override
    public boolean migrate(CloudId from, CloudId to) {
        if (!clustered) {
            // in Cluster mode, only keys in the same hash slot (ie they have the same hashtag) can be reliably renamed
            if (tsLogger.logger.isInfoEnabled()) {
                tsLogger.logger.info("RedisSlots.migrate(): not supported");
            }

            throw new UnsupportedOperationException("migrating logs is only supported by Redis Cluster");
        }

        if (from.failoverGroupId != to.failoverGroupId) {
            if (tsLogger.logger.isInfoEnabled()) {
                tsLogger.logger.info("RedisSlots.migrate(): target node is in a different failover group");
            }

            throw new UnsupportedOperationException("migrating logs is only supported if they belong to the same failover group");
        }

        for (String key : jedisCluster.keys(from.allKeysPattern())) {
            byte[] bytes = jedisCluster.get(key.getBytes());
//            String data = jedisCluster.get(key);

            String newKey = key.replace(from.nodeId, to.nodeId);

            try {
                if (!"OK".equals(jedisCluster.rename(key, newKey))) {
                    if (tsLogger.logger.isInfoEnabled()) {
                        tsLogger.logger.info("RedisSlots.migrate(): rename failed");
                    }
                }// else {
//                    recoveryStore.remove_committed()
//                    recoveryStore.write_committed()
//                }
            } catch (JedisException e) {
                if (tsLogger.logger.isInfoEnabled()) {
                    tsLogger.logger.infof("RedisSlots.migrate(): %s", e.getMessage());
                }
                return false;
            }
        }

/*        String keyPattern = from.allKeysPattern();

        try (JedisCluster jedis = new JedisCluster(hostAndPort)) {
            for (String key : getKeys(keyPattern)) {
                String newKey = key.replace(from.nodeId, to.nodeId);

                try {
                    if (!"OK".equals(jedis.rename(key, newKey))) {
                        if (tsLogger.logger.isInfoEnabled()) {
                            tsLogger.logger.info("RedisSlots.migrate(): rename failed");
                        }
                    }
                } catch (JedisException e) {
                    if (tsLogger.logger.isInfoEnabled()) {
                        tsLogger.logger.infof("RedisSlots.migrate(): %s", e.getMessage());
                    }
                    return false;
                }
            }
        }*/

        return true;
    }

    private Transaction getTransaction() {
        Collection<ConnectionPool> nodes = jedisCluster.getClusterNodes().values();

        for (ConnectionPool node : jedisCluster.getClusterNodes().values()) {
            try (Jedis jedis = new Jedis(node.getResource())) {
                return jedis.multi();
            }
        }

        return null;
    }

    private Set<String> getKeys(String keyPattern) {
        try (JedisCluster jedisCluster = new JedisCluster(hostAndPort)) {
            Set<String> keySet = new HashSet<>();

            for (ConnectionPool node : jedisCluster.getClusterNodes().values()) {
                try (Jedis jedis = new Jedis(node.getResource())) {
                    keySet.addAll(jedis.keys(keyPattern));
                }
            }

            return keySet;
        }
    }
}
