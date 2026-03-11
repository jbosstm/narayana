/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.arjuna.objectstore.infinispan;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ClusteringConfiguration;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.conflict.MergePolicy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.partitionhandling.PartitionHandling;
import org.infinispan.persistence.sifs.configuration.SoftIndexFileStoreConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InfinispanConfigTest extends InfinispanTestBase {
    private static InfinispanTestBase.Store store;
    private final static String STORE_CONFIG_FILE = "/infinispan-store-config.xml";

    @BeforeAll
    public static void beforeAll() throws Exception {
        DefaultCacheManager cacheManager = new DefaultCacheManager(
                InfinispanConfigTest.class.getResourceAsStream(STORE_CONFIG_FILE));
        store = new Store(cacheManager, null, "node1");
        store.start();
    }

    @AfterAll
    public static void afterClass() {
        store.stop();
    }

    @Test
    public void test() {
        String INDEX_LOCATION = "infinispan-caches/index"; // the location is defined in STORE_CONFIG_FILE
        String DATA_LOCATION = "infinispan-caches/data";

        Configuration cacheConfig = store.cache().getCacheConfiguration();
        List<StoreConfiguration> stores = cacheConfig.persistence().stores();
        assertEquals(1, stores.size());
        StoreConfiguration storeConfig = stores.getFirst();
        ClusteringConfiguration clustering = cacheConfig.clustering();

        assertEquals(MergePolicy.PREFERRED_ALWAYS, clustering.partitionHandling().mergePolicy());
        assertEquals(PartitionHandling.DENY_READ_WRITES, clustering.partitionHandling().whenSplit());
        assertEquals(CacheMode.DIST_SYNC, clustering.cacheMode());
        Assertions.assertFalse(storeConfig.shared());
        assertEquals(INDEX_LOCATION, ((SoftIndexFileStoreConfiguration) storeConfig).indexLocation());
        assertEquals(DATA_LOCATION, ((SoftIndexFileStoreConfiguration) storeConfig).dataLocation());
    }
}