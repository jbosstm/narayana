# Distributed ObjectStore (Experimental)

**Status:** Experimental  
**Sponsor:** Michael Musgrove (mmusgrov@redhat.com)  
**Tracking:** [JBTM-845](https://issues.redhat.com/browse/JBTM-845)  
**Since:** Narayana 7.0.0

## ⚠️ Warning

This is an **EXPERIMENTAL** feature. It is **NOT** recommended for production use.
The API and behavior may change or be removed in future releases without notice.

## Overview

This module provides an Distributed implementation of Narayana's `SlotStore` ObjectStore. It enables distributed, in-memory transaction log storage across a cluster of Infinispan nodes, offering an alternative to traditional file-based ObjectStores.

### Key Features

- **Distributed Storage**: Transaction logs stored in Infinispan distributed or replicated caches
- **Cluster-Aware**: Multiple Narayana instances can share transaction state across a cluster
- **In-Memory Performance**: Faster than disk-based stores for high-throughput scenarios
- **Configurable Consistency**: Support for different Infinispan cache modes (LOCAL, REPL_SYNC, DIST_SYNC)
- **Optional Persistence**: Can be configured with Infinispan cache stores for durability

### Architecture

The implementation consists of:

- **`InfinispanSlots`**: Main `BackingSlots` implementation using Infinispan cache
- **`InfinispanStoreEnvironmentBean`**: Configuration bean for Infinispan settings
- **`InfinispanSlotKeyGenerator`**: Interface for generating unique cache entry keys

The store integrates with Narayana's standard `SlotStoreAdaptor` and requires no changes to transaction management code.

## Requirements

- Java 11 or later
- Maven 3.6+
- Narayana 7.0.0 or later
- Infinispan 16.2.2 or later

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

### WildFly Integration

See [INTEGRATION.md](INTEGRATION.md) for WildFly module and subsystem configuration.

## Important Considerations

### Consistency Requirements

When using this store in a clustered environment with recovery management:

1. **Configure appropriate cache modes**: Use `REPL_SYNC` or `DIST_SYNC` for clustered setups
2. **Recovery leader election**: Ensure only one recovery manager acts as the leader
4. **Split-brain handling**: Configure Infinispan partition handling appropriately
5. **Network reliability**: Ensure stable network connectivity between cluster nodes

### Performance Characteristics

- **Faster than disk**: In-memory storage provides lower latency for transaction log writes
- **Network overhead**: Replication adds network round-trip latency
- **Memory usage**: Transaction logs consume JVM heap memory
- **GC pressure**: High transaction rates can increase garbage collection overhead

### Limitations

- **Experimental status**: Not recommended for production use
- **Recovery complexity**: Cluster-wide recovery requires careful coordination
- **Memory bounds**: Limited by available heap space, unlike filesystem stores
- **Configuration complexity**: Requires understanding of both Narayana and Infinispan

## Testing

Run the test suite:

```bash
cd experimental/arjuna-objectstore-distributed-experimental
mvn clean test
```

Key test classes:

- `InfinispanClusterTest`: Multi-node cluster scenarios
- `InfinispanRecoveryScanTest`: Recovery manager functionality
- `InfinispanReplicatedAndPersistentTest`: Persistence and replication

## Known Issues

- **JBTM-845**: Initial experimental implementation - see JIRA for open sub-tasks
- Configuration via JNDI requires application server environment
- Embedded cache manager usage in standalone apps requires manual lifecycle management

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

If multiple nodes attempt recovery simultaneously:


## Future Plans

### Path to Tech Preview

Criteria for promotion to Tech Preview status:

- [ ] Complete recovery manager integration with leader election
- [ ] Performance benchmarking vs. filesystem stores
- [ ] WildFly Galleon feature pack layer
- [ ] Quarkus extension with dev services
- [ ] Documentation in main Narayana user guide
- [ ] >85% test coverage
- [ ] Integration testing in CI

### Path to Stable

Additional requirements for stable release:

- [ ] Production deployment validation
- [ ] Long-running stability testing
- [ ] Performance tuning and optimization
- [ ] Backward compatibility guarantees
- [ ] Support for upgrade scenarios

## Related Documentation

- [INTEGRATION.md](INTEGRATION.md) - WildFly and Quarkus integration guide
- [JBTM-845](https://issues.redhat.com/browse/JBTM-845) - Original feature request
- [Narayana Documentation](https://narayana.io/docs/project/index.html) - Main documentation
- [Infinispan Documentation](https://infinispan.org/docs/stable/titles/overview/overview.html) - Infinispan guide

## Contributing

This experimental feature welcomes contributions. Please:

1. Discuss changes on [Narayana Zulip](https://narayana.zulipchat.com) or GitHub issue first
2. Follow Narayana coding standards
3. Add tests for new functionality
4. Update documentation as needed

## License

Same as Narayana: Apache License 2.0

---

**Remember**: This is an experimental feature. Always test thoroughly in your specific environment before any use beyond development and testing scenarios.
