# Distributed ObjectStore (Experimental)

**Status:** Experimental  
**Sponsor:** Michael Musgrove (mmusgrov@redhat.com)  
**Tracking:** [JBTM-845](https://issues.redhat.com/browse/JBTM-845)  
**Since:** Narayana 7.0.0

## ⚠️ Warning

This is an **EXPERIMENTAL** feature. It is **NOT** recommended for production use.
The API and behavior may change or be removed in future releases without notice.

## Overview

This module provides a distributed implementation of Narayana's `SlotStore` ObjectStore backed by Infinispan distributed caches. It enables in-memory transaction log storage across a cluster of nodes, offering an alternative to traditional file-based ObjectStores for clustered environments.

Transaction logs are stored in Infinispan distributed or replicated caches, allowing multiple Narayana instances to share transaction state across a cluster. The implementation supports different Infinispan cache modes (LOCAL, REPL_SYNC, DIST_SYNC) for configurable consistency guarantees and can be configured with Infinispan cache stores for optional persistence.

The implementation consists of three main components. `InfinispanSlots` provides the main `BackingSlots` implementation using an Infinispan cache as the backing store. `InfinispanStoreEnvironmentBean` supplies configuration settings for the Infinispan store including cache references and JNDI names. `InfinispanSlotKeyGenerator` defines an interface for generating unique cache entry keys for slot entries.

The store integrates with Narayana's standard `SlotStoreAdaptor` and requires no changes to transaction management code.

## Requirements

This experimental feature requires Java 11 or later, Maven 3.6 or higher, Narayana 7.0.0 or later, and Infinispan 16.2.2 or later.

## Installation

### Maven Dependency

Add the experimental module to your project:

```xml
<dependency>
    <groupId>org.jboss.narayana.experimental</groupId>
    <artifactId>arjuna-objectstore-distributed-experimental</artifactId>
    <version>${narayana.version}</version>
</dependency>
```

**Important**: This dependency is **not** included in Narayana's default build. It must be built explicitly using:

```bash
cd narayana
mvn clean install -Pexperimental
```

## Configuration

### Basic Configuration

Configure the ObjectStore to use Infinispan via properties or programmatic configuration:

#### Properties-Based Configuration

Create or modify `jbossts-properties.xml`:

```xml
<properties>
    <entry key="CoordinatorEnvironmentBean.commitOnePhase">true</entry>
    <entry key="ObjectStoreEnvironmentBean.objectStoreType">
        com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor
    </entry>
    <entry key="SlotStoreEnvironmentBean.backingSlots">
        com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanSlots
    </entry>
    
    <!-- Infinispan-specific configuration -->
    <entry key="InfinispanStoreEnvironmentBean.cacheManagerJndiName">
        java:jboss/infinispan/container/narayana
    </entry>
    <entry key="InfinispanStoreEnvironmentBean.cacheJndiName">
        java:jboss/infinispan/cache/narayana/transaction-log
    </entry>
</properties>
```

#### Programmatic Configuration

```java
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.internal.arjuna.objectstore.slot.*;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.*;

// Configure SlotStore to use Infinispan
SlotStoreEnvironmentBean slotStoreEnvBean = 
    BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);
slotStoreEnvBean.setBackingSlots(InfinispanSlots.class.getName());

// Configure Infinispan connection
InfinispanStoreEnvironmentBean infinispanEnvBean = 
    BeanPopulator.getDefaultInstance(InfinispanStoreEnvironmentBean.class);
infinispanEnvBean.setCacheManagerJndiName("java:jboss/infinispan/container/narayana");
infinispanEnvBean.setCacheJndiName("java:jboss/infinispan/cache/narayana/transaction-log");
```

### Infinispan Cache Configuration

The Infinispan cache should be configured appropriately for your consistency requirements:

#### Example: Replicated Cache with Synchronous Replication

```xml
<infinispan>
    <cache-container name="narayana" default-cache="transaction-log">
        <transport cluster="narayana-cluster" stack="tcp"/>
        
        <replicated-cache name="transaction-log" mode="SYNC">
            <transaction mode="BATCH"/>
            <locking isolation="REPEATABLE_READ"/>
            <state-transfer enabled="true" timeout="60000"/>
            
            <!-- Optional: persistence for durability -->
            <file-store shared="false" preload="true" purge="false">
                <write-behind modification-queue-size="1024"/>
            </file-store>
        </replicated-cache>
    </cache-container>
</infinispan>
```

### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `InfinispanStoreEnvironmentBean.cacheManagerJndiName` | `null` | JNDI name for Infinispan CacheManager |
| `InfinispanStoreEnvironmentBean.cacheJndiName` | `null` | JNDI name for Infinispan Cache |
| `InfinispanStoreEnvironmentBean.keyGeneratorClassName` | `null` | Custom `InfinispanSlotKeyGenerator` implementation |
| `InfinispanStoreEnvironmentBean.numberOfSlots` | `1024` | Number of slots in the store |

## Usage Example

### Standalone Application

```java
import com.arjuna.ats.arjuna.AtomicAction;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.configuration.cache.*;

// 1. Create Infinispan cache manager
ConfigurationBuilder cacheConfig = new ConfigurationBuilder();
cacheConfig.clustering()
    .cacheMode(CacheMode.REPL_SYNC)
    .transaction()
    .transactionMode(TransactionMode.BATCH);

EmbeddedCacheManager cacheManager = 
    new DefaultCacheManager(new GlobalConfigurationBuilder()
        .clusteredDefault()
        .transport().defaultTransport()
        .build());
        
cacheManager.defineConfiguration("transaction-log", cacheConfig.build());

// 2. Configure Narayana to use Infinispan
// (see Configuration section above)

// 3. Use transactions normally
AtomicAction tx = new AtomicAction();
tx.begin();
try {
    // Your transactional work here
    tx.commit();
} catch (Exception e) {
    tx.abort();
}
```

## Important Considerations

When using this store in a clustered environment with recovery management, several aspects require careful configuration. Configure Infinispan cache modes appropriately, using `REPL_SYNC` or `DIST_SYNC` for clustered setups to ensure consistency across nodes. Ensure only one recovery manager acts as the leader at any given time, and configure Infinispan partition handling to prevent split-brain scenarios. Stable network connectivity between cluster nodes is essential for reliable operation.

Performance characteristics of this distributed implementation differ from traditional file-based stores. In-memory storage may provide different latency characteristics for transaction log writes compared to disk-based stores, though comprehensive benchmarking has not been performed. Replication modes add network round-trip latency to write operations. Transaction logs consume JVM heap memory rather than filesystem space, and high transaction rates may increase garbage collection pressure.

This experimental implementation has several important limitations. It is not recommended for production use due to its experimental status. Cluster-wide recovery requires careful coordination and understanding of distributed systems concepts. Memory usage is bounded by available heap space unlike filesystem stores which can grow as needed. Proper configuration requires understanding of both Narayana transaction management and Infinispan distributed caching.

## Testing

The test suite can be run from the experimental module directory using `mvn clean test`. Key test classes include `InfinispanClusterTest` for multi-node cluster scenarios, `InfinispanRecoveryScanTest` for recovery manager functionality, and `InfinispanReplicatedAndPersistentTest` for persistence and replication behavior.

## Known Issues

This is the initial experimental implementation tracked under JBTM-845 - see the JIRA issue for open sub-tasks and known limitations. Configuration via JNDI requires an application server environment. Embedded cache manager usage in standalone applications requires manual lifecycle management.

## Troubleshooting

### Cache Not Found

```
ERROR: Cache not found: java:jboss/infinispan/cache/narayana/transaction-log
```

**Solution**: Verify the Infinispan cache is defined and the JNDI name matches configuration.

### ClassNotFoundException for InfinispanSlots

```
ERROR: Could not instantiate backing slots class
```

**Solution**: Ensure the experimental module JAR is on the classpath. In WildFly, add module dependency.

### Recovery Leader Split-Brain

If multiple nodes attempt recovery simultaneously, ensure your cluster coordination mechanism prevents conflicts.

## Related Documentation

Additional documentation and context for this experimental feature can be found in the following resources. The original feature request and design discussion is tracked in [JBTM-845](https://issues.redhat.com/browse/JBTM-845). Detailed information about ObjectStore implementations and architecture is available in the [Narayana ObjectStore documentation](../../docs/src/main/asciidoc/project/appendix/object_store_implementations.adoc). General Narayana documentation is at [narayana.io](https://narayana.io/docs/project/index.html), and Infinispan documentation can be found at [infinispan.org](https://infinispan.org/docs/stable/titles/overview/overview.html).

## Contributing

This experimental feature welcomes contributions. Please discuss changes on [Narayana Zulip](https://narayana.zulipchat.com) or in a GitHub issue first, follow Narayana coding standards, add tests for new functionality, and update documentation as needed.

## License

Same as Narayana: Apache License 2.0

---

**Remember**: This is an experimental feature. Always test thoroughly in your specific environment before any use beyond development and testing scenarios.
