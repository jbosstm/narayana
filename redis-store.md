# branches

JBTM-3762 redis store

- implementation is: https://github.com/mmusgrov/narayana/tree/JBTM-3762/ArjunaCore/arjuna/classes/com/arjuna/ats/internal/arjuna/objectstore/slot/redis
- tests are: https://github.com/mmusgrov/narayana/blob/JBTM-3762/ArjunaCore/arjuna/tests/classes/com/hp/mwtests/ts/arjuna/objectstore/RedisStoreTest.java
- [the demo](./redis-store-demo.md) is work in progress (but was written for LRA but migration isn't possible since ids encode host names so I'm in the process of changing it to show AtomicActions instead)
- tests are: `mvn test -Predis-store -Dtest=RedisStoreTest#test1 -f ArjunaCore/arjuna/pom.xml`
- redis raft implementaon: `git clone https://github.com/RedisLabs/redisraft.git` and build it with: cmake and make

= Documentation/publicity

== Transaction stores 

A while ago I prototyped a Redis backed implementation of the [SlotStore](https://www.narayana.io/docs/api/com/arjuna/ats/internal/arjuna/objectstore/slot/SlotStore.html)
 backend suitable for installations where nodes hosting the storage can come and go. This makes it well suited for cloud based deployments of the Recovery Manager.

In the context of the [CAP theorem](https://en.wikipedia.org/wiki/CAP_theorem) of distributed computing, the recovery store needs to behave as a CP system,
ie it needs to be able to tolerate network Partitions and yet continue to provide Strong Consistency.
Redis can provide the strong consistency guarantee if the [RedisRaft module](https://redis.com/blog/redisraft-new-strong-consistency-deployment-option/) is used with Redis running as a
cluster. RedisRaft achieves consistency and partition tolerance by ensuring that:

- acknowledged writes are guaranteed to be committed and never lost,
- reads will always return the most up-to-date committed write,
- the cluster is sized correctly: a RedisRaft cluster of 3 nodes can tolerate a single node failure and a cluster
  of 5 can tolerate 2 node failures, ... ie if the cluster is to tolerate losing N nodes then the cluster size must be at least 2*N+1, thus the minimum
  cluster size is 3 and the reason having an odd number of nodes in the cluster is to avoid "split brain" scenarios during network partitions; an odd number guarantees that one side of the split will be in the majority.

During network splits the cluster will become unavailable for a [second or two](https://redis.io/docs/reference/cluster-spec/), ie the cluster is designed to survive failures of a few nodes in the cluster, but it is not a suitable solution for applications that require availability in the event of large net splits, however transaction systems favour Consistency over Availability.

A key motivator for this new SlotStore backend is to address a common problem with using the Narayana transaction stores on cloud platforms is that scaling down a node that has in doubt transactions can leave them unmanaged. Most cloud platforms can detect crashed nodes and restart them but this must be carefully managed to ensure that the restarted node is identically configured (same node identifier, same transaction store and same resource adapters).
The current solution, when running on Openshift, is to use a ReplicatSet and to veto scale down until all transactions are completed which can take an indeterminate amount of time, but if we can ask another member of the deployment to finish these in doubt transactions then all but the last node can be safely shutdown even with in doubt transactions. The resulting increase in availability in the presence of failure of nodes or the network is a significant benefit for transactional applications which, after all, is key reason why businesses are embracing cloud based deployments.

Remark: Redis is offered as a managed service on a majority of cloud platforms making it easier for customers to get started with this solution.

== A Redis backed store

Redis is a key value store. Keys are stored in hash slots and hash slot are shared evenly amongst the shards (keys -> hash slots -> shards), the [redis cluster specication](https://redis.io/docs/reference/cluster-spec/) has the detail.
Re-sharding involves moving hash slots to other nodes, impacting performance. Thus, if we can control which hash slots the keys map onto then we can improve performance under both normal and failure conditions. This periodic rebalancing of the cluster can be optimised if keys belonging to the same recovery manager are stored in the same hash slot, furthermore having the keys, for a particular recovery node, colocated on a single cluster node is good for the performance of transaction store.

Also noteworthy is that the keys mapped to a particular hash slot can operated upon [transactionally](https://redis.io/docs/manual/transactions/) which is not the case for keys in different slots meaning that no inter-node hand-shaking is required. This feature opens up the possibility, perhaps, of allowing concurrent access to recovery logs by different recovery managers: but that's something for a future iteration of the design, but if the logs in a store are shared then be aware that some Narayana recovery modules cache records so those implementations would need to re-evaluated, noting in particular that Redis has support for optimistic concurrency using the watch API which clients can use to observe updates to key values by other recovery managers.

== Key space design

A recovery manager has a unique node identifier. We'd like to be able to form "recovery groups" such that any recovery manager in the group can manage transactions created by the others, but not at the same time. To this end we assign a "failoverGroupId" to each recovery manager and use that as the Redis key prefix. This will force all keys created by members of the failover group into the same hash slot, a cloud example of this idea is that the pods in a deployment would all share the same failoverGroupId so any pod in the deployement can take over when the deployment is scaled down.

== Failover

Failover involves detecting when a member of the "recovery group" is removed from the cluster and to then migrate the keys to another member of the group. I added an example to the [LRA recovery coordinator](https://github.com/mmusgrov/narayana/blob/JBTM-3762/rts/lra/coordinator/src/main/java/io/narayana/lra/coordinator/api/RecoveryCoordinator.java#L228) and used the [jedis redis API](https://javadoc.io/doc/redis.clients/jedis/latest/redis/clients/jedis/UnifiedJedis.html#rename-java.lang.String-java.lang.String-) rename command to "migrate" the keys which is an atomic operation.

== Issues

The performance of Redis Raft in my implementation of the SlotStore backend is poor (4 times slower that the default store); I have not invested any effort on improving it but I will follow up with another post to discuss throughput, for example [pipelining redis commands](https://redis.io/docs/manual/pipelining/), similar to how we batch writes to the Journal Store](https://redis.io/docs/manual/pipelining/), or using (Virtual Threads)[https://openjdk.org/jeps/444] etc. 

This design for the key space may not be suitable for transactions with subordinates or nested transactions or for ones that require participant logs to be distinct from the transaction log, such as JTS or XTS. I say may since a modification to the design should accomodate these models.

== Assumptions

The cloud platform administrator is responsible for:
- detecting and restarting failed nodes;
- issuing the migrate command on one of the remaining nodes
- for detecting when the deployment is scaled down to zero with pending transactions (including orphans) and emitting a warning accordingly

== Example of how to migrate logs

[demonstrator instructions](redis-store-demo.md)

Remark: there are issues with LRA support so I will provide a demo using AtomicAcions (unless I find time to fix the implemention to wwork with LRA)









The solution groups the scale set into a group and any member of the group can manager any transaction provided that they don't do so at the same time.

The design uses the notion of a `failover group` to address this requirement. Keys in the same failover group support migrate semantics within the group. The pair <failoverGroupId>:<nodeId> must be unique in a give Redis cluster.


The documentation at the RedisRaft github repository includes
<a href="https://github.com/RedisLabs/redisraft/blob/master/docs/Deployment.md#deploying-redisraft">
    instructions on setting up clusters</a>.

The performance cost/benefit comparison between a standard redis cluster and redis raft cluster shows that
the complexities of guaranteeing strong consistency adds a 4-fold performance cost.

This performance cost is just a baseline measure and optimisation work should improve upon it,
for example batching writes, in the manor of hornetq store perhaps using
(<a href="https://redis.io/docs/manual/pipelining/">redis pipelines</a>) or otherwise.

==== end

other notes:

raft:
/home/mmusgrov/.../backup/start.sh # start redisraft cluster

clustered:
/home/mmusgrov/src/misc/redis/redis-7.2-rc1/utils/create-cluster/create-cluster start
  - to set up a cluster use: https://www.mortensi.com/2021/12/understand-setup-and-test-a-redis-cluster-in-less-than-10-minutes/
  - https://redis.com/blog/redis-clustering-best-practices-with-keys/

  redis-cli --cluster call --cluster-only-masters 127.0.0.1:30001 FLUSHALL

  redis-cli --cluster call --cluster-only-masters <one-of-the-nodes-address>:<its-port> FLUSHALL 

  redis-cli --cluster call 127.0.0.1:30003 KEYS "*"

https://github.com/RedisLabs/redisraft

sysctl vm.overcommit_memory=1

git clone https://github.com/redis/redis
cd redis
sudo make && sudo make install # installs in /usr/local/bin/redis-server

vi /etc/redis/redis.conf
daemonize yes

redis-cli -p 30001 raft.cluster init

