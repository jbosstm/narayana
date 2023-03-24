
# Introduction

This is a demonstration of an object store based on [https://github.com/RedisLabs/redisraft](RedisRaft). RedisRaft is a Redis module that implements the Raft Consensus Algorithm, making it possible to create strongly-consistent clusters of Redis servers - standard Redis cannot be used because shared object stores for use with Narayana must provide this strong consistency guarantee.

This object store is strictly a PoC because, as stated on the redisraft github page, "RedisRaft is still being developed and is not yet ready for any real production use. Please do not use it for any mission critical purpose at this time".

The demo presents the use case of migrating logs between LRA coordinators. To run the demo you will need to:

1. Clone and build a narayana git branch with support for a Redis back store.
2. Start a 3-node cluster of Redis nodes running RedisRaft.
3. Build and start two LRA coordinators with distinct node id's, node1 and node2.
4. Start an LRA on the first coordinator and then halt it to simulate a failure.
5. View the redis keys using the Redis CLI noticing that the keys embed the node id of the owning coordinator.
7. Ask the second coordinator to migrate the keys from node1 to node2.
8. Coordinators maintain a cache of LRAs, but since this is just a PoC I haven't implemented refreshing the cache so you will need to simulate it by restarting the second coordinator.
9. When the first periodic recovery cycle runs (the default is every 2 minutes) the migrated LRAs will be detected which you can verify (`curl http://localhost:50001/lra-coordinator/|jq`).

In detail:

# build the branch

git clone https://github.com/jbosstm/narayana.git
cd narayana
git checkout JBTM-3762
./build.sh clean install -Prelease,community -DskipTests
cd redis-demo

# start a redisraft cluster of three nodes (the minimum cluster size that provides strong consistency in the presence of network partitions)

To use the demo you will need RedisRaft which must be built from an 'unstable' branch using the following [instructions](../redisraft-module/README.md) but the demo already includes a [prebuilt loadable shared library](../redisraft-module/redisraft.so) which may work on your particular architecture. You will also need to [install the Redis CLI](https://redis.io/docs/latest/operate/oss_and_stack/install/install-redis/) for managing clusers of Redis nodes. The demo includes a shell script to start a cluser of 3 redis-server instances:

```
./start-redisraft.sh
```

The quickist way of stopping these servers is to kill the processes: `killall redis-server`

# build and start two LRA coordinators:

cd lra-coordinator/
mvn clean package

Remark: I built this coordinator using the quarkus-maven-plugin (`mvn io.quarkus:quarkus-maven-plugin:3.3.1:create -DprojectGroupId=org.acme -DprojectArtifactId=narayana-lra-coordinator -Dextensions="rest-jackson,rest-client` and manually edited the generated pom to add a dependency on the coordinator (maven artifact `org.jboss.narayana.rts:lra-coordinator-jar:7.0.2.Final-SNAPSHOT). I didn't need the generated src directory so I deleted that.

The coordinator needs to be configured to use the redis backed object store and the lra-coordinator directory contains a jbossts-properties.xml properties file for this purpose - if you run the coordinator from the same directlry it will use these properties.

The build will place a coordinator in the maven target directory. Start two instances on different JAX-RS endpoints and different node identifiers:

```
$ java -Dquarkus.http.port=50000 -DCoreEnvironmentBean.nodeIdentifier=node1 -jar target/quarkus-app/quarkus-run.jar &
$ java -Dquarkus.http.port=50001 -DCoreEnvironmentBean.nodeIdentifier=node2 -jar target/quarkus-app/quarkus-run.jar &
```

Both coordinators will now be using the redis backed store for its transaction logs. Their logs will be isolated from each other because you started them with different nodeIdentifiers.

# Start a Long Running Action at node1 and finish it at node 2

Create an LRA managed by node 1:

```
COORDINATOR1_URL=http://localhost:50000/lra-coordinator
COORDINATOR2_URL=http://localhost:50001/lra-coordinator
LRA_ID=$(curl -X POST $COORDINATOR1_URL/start | sed "s#.*/##")
# sed "s/\"//g" would give the full LRA url
```

You should find an LRA, identified by the env variable `$LRA_ID`, active in the first coordinator:

```
curl $COORDINATOR1_URL
```
but not in the second coordinator:

```
curl $COORDINATOR2_URL
```

If you want to see how the keys space used for the second coordinator differs from the first coordinator, try creating an LRA at the second coordinator:

```
curl -X POST $COORDINATOR2_URL/start
```

Now check that the LRA is known to the redis store by using the Redis CLI to inspect the keys:

```
$ redis-cli -p 30001 # start the redis cli
127.0.0.1:30001> keys *
1) "{0}:node2:0:ffffc0a801b7:98c1:666eb5d3:1:0"
2) "{0}:node1:0:ffffc0a801b7:a735:666eb3a7:1:0"
127.0.0.1:30001> get "{0}:node2:0:ffffc0a801b7:98c1:666eb5d3:1:0"
"#BE\x01\x10 @\x00\x00\x00\x00\x1c\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\xff\xff\xc0\xa8\x01\xb7\x00\x00\x98\xc1fn\xb5\xd3\x00\x00\x01\x06\x00\x00\x00,/StateManager/BasicAction/LongRunningAction\x00\x00\x00\x00\x01\x00\x00\x00\x00\x00\x00\x00\x1c\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\xff\xff\xc0\xa8\x01\xb7\x00\x00\x98\xc1fn\xb5\xd3\x00\x00\x01\a\x00\x00\x00\xf0#BE\x01\x10 @\x00\x00\x00\x00\t#ARJUNA#\x00\x00\x00\x00\x00\x00\x00\x1c\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\xff\xff\xc0\xa8\x01\xb7\x00\x00\x98\xc1fn\xb5\xd3\x00\x00\x01\x06\x00\x00\x00\x1c\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\xff\xff\xc0\xa8\x01\xb7\x00\x00\x98\xc1fn\xb5\xd3\x00\x00\x01\b\x00\x00\x01\x90 z\xb5q\x00\x00\x00\x00\x00\x00\x01\xcf\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x01\xcf\x00\x00\x00Hhttp://localhost:50001/lra-coordinator/0_ffffc0a801b7_98c1_666eb5d3_106\x00\x00\x00\x00\x00\x00\x00\x00\x01\x00\x00\x00\x00\x00\x00\x00\x01\x00\x00\x01\x90 z\xb5t\x00\x00\x00\x00\x00\x00\x00\aActive\x00\x00"
127.0.0.1:30001>
```

so that's good because it shows the coordinator is using the new redis store which contains the active LRA.

Now simulate a failure by stopping the first coordinator running as node1 (ctrl-C or kill %1 etc) and migrate the logs by sending an HTTP POST request to the second recovery coordinator resource path telling it to take ownership of the logs created by node1:

```
curl -X POST $COORDINATOR2_URL/recovery/migrate/node1/node2
```

If you look at the keys type the keys command at the redis-cli prompt (`127.0.0.1:30001> keys *`) you should notice that the key is now owned by node2. Also notice that if you did not stop the original owning coordinator you will see warnings because the coordinator at node1 doesn't refresh its cache in this version and isn't aware that the record has been moved.

As pointed out above, in step 8, LRA cache refresh isn't implemented so to simulate the refresh simply restart the second coordinator. After the restart wait for the periodic recovery system to run (by default every 2 minutes) after which the second coordinator will have detected the migrated LRA:

```
curl $COORDINATOR2_URL # ensure you have restarted the second coordinator to force a "cache refresh"
```

pipe it into jq to see a nicely formated display of the active LRAs. You should expect to see the LRA in the list whereas previously it only appeared in the output from the first coordinator (`curl $COORDINATOR1_URL`).

Finally clean up by closing the migrated LRA at node2:

```
http://localhost:50001/lra-coordinator/id-of-LRA/close

# or use the env variables:

curl -X PUT "$COORDINATOR2_URL/$LRA_ID/close"
```
