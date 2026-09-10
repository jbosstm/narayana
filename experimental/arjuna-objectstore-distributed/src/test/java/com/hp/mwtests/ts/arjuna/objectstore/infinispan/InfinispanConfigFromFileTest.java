/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.arjuna.objectstore.infinispan;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ClusteringConfiguration;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.conflict.MergePolicy;
import org.infinispan.partitionhandling.PartitionHandling;
import org.infinispan.persistence.sifs.configuration.SoftIndexFileStoreConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/*
 * test configuring an infinispan slot store using a standard infinispan xml config file
 * as opposed to the other tests which all use programmatic config
 */
public class InfinispanConfigFromFileTest extends InfinispanTestBase {
    @BeforeAll
    public static void setupStore() {
        // jbossts properties file containing the config for an infinispan slot store
        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", "infinispan-jbossts-properties.xml");
    }

    @Test
    public void test() {
        // look up the bean that is created from the jbossts properties file
        InfinispanStoreEnvironmentBean config = BeanPopulator.getDefaultInstance(InfinispanStoreEnvironmentBean.class);

        // check that the store works as a recovery store
        writeSomething();

        // and verify that the cache was created when recovery store was initialised
        Cache<byte[], byte[]> cache = config.getCache();
        assertNotNull(cache);

        // the location of the index and data files are defined in STORE_CONFIG_FILE
        // ie they should be kept in sync for the test to pass
        String INDEX_LOCATION = "infinispan-caches/index";
        String DATA_LOCATION = "infinispan-caches/data";

        Configuration cacheConfig = cache.getCacheConfiguration();
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

        // remark: this would be a good place to check for the Experimental feature (message id ARJUNA012419)
        // but jboss logging does not appear to support programmatically adding an appender which we could have used
        // to check the log output
    }

    private void writeSomething() {
        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        OutputObjectState oos = new OutputObjectState();

        try {
            oos.packString("junit1");
            Uid uid = new Uid();
            String typeName = "StateManager/junit1";
            Assertions.assertTrue(recoveryStore.write_committed(uid, typeName, oos));
            Assertions.assertTrue(recoveryStore.remove_committed(uid, typeName));
        } catch (IOException | ObjectStoreException e) {
            fail(e);
        }
    }
}
