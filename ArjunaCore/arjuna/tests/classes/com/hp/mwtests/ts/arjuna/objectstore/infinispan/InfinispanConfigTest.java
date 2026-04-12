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

/*
 * test configuring an infinispan store using the standard infinispan xml config
 * whereas the other tests all use programmatic config
 */
public class InfinispanConfigTest extends InfinispanTestBase {
    private static InfinispanTestBase.Store store;
    private final static String STORE_CONFIG_FILE = "/infinispan-config.xml";

    @BeforeAll
    public static void beforeAll() throws Exception {
        // configure the cache manager using an infinispan config file
        DefaultCacheManager cacheManager = new DefaultCacheManager(
                InfinispanConfigTest.class.getResourceAsStream(STORE_CONFIG_FILE));
        store = new Store(cacheManager, null, "node1", STORE_DIR);
        store.start();
    }

    @AfterAll
    public static void afterClass() {
        store.stop();
    }

    @Test
    public void test() {
        // the location of the index and data files are defined in STORE_CONFIG_FILE
        // ie they should be kept in sync for the test to pass
        String INDEX_LOCATION = "infinispan-caches/index";
        String DATA_LOCATION = "infinispan-caches/data";

        Configuration cacheConfig = store.cache().getCacheConfiguration();
        List<StoreConfiguration> stores = cacheConfig.persistence().stores();
        assertEquals(1, stores.size());
        StoreConfiguration storeConfig = stores.get(0);
        ClusteringConfiguration clustering = cacheConfig.clustering();

        assertEquals(MergePolicy.PREFERRED_ALWAYS, clustering.partitionHandling().mergePolicy());
        assertEquals(PartitionHandling.DENY_READ_WRITES, clustering.partitionHandling().whenSplit());
        assertEquals(CacheMode.DIST_SYNC, clustering.cacheMode());
        Assertions.assertFalse(storeConfig.shared());
        assertEquals(INDEX_LOCATION, ((SoftIndexFileStoreConfiguration) storeConfig).indexLocation());
        assertEquals(DATA_LOCATION, ((SoftIndexFileStoreConfiguration) storeConfig).dataLocation());
        // remark: this would be a good place to check for the experimental feature (message id ARJUNA012419)
        // but jboss logging does appear to support programmatically adding an appender which we could have used
        // to check the log output
    }
}