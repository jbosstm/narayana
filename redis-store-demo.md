
build the branch:
build a coordinator:
```
$ mvn io.quarkus:quarkus-maven-plugin:3.1.1.Final:create -DprojectGroupId=org.acme -DprojectArtifactId=narayana-lra-coordinator -Dextensions="resteasy-jackson,rest-client"
$ cd narayana-lra-coordinator/
$ rm -rf src
$ vi pom.xml # add a dependency on the coordinator artifact: org.jboss.narayana.rts:lra-coordinator-jar:6.0.2.Final-SNAPSHOT
```
copy the jbossts properties file in the current directory with the standard name:
```
$ cp src/main/resources/redis-jbossts-properties.xml jbossts-properties.xml
```

start redis
```
$ /home/mmusgrov/vimwiki/backup/start.sh # start redisraft cluster
```
start the coordinator 
```
$ mvn clean package
```
# start two coordinators
```
$ java -Dquarkus.http.port=50000 -DCoreEnvironmentBean.nodeIdentifier=node1 -jar target/quarkus-app/quarkus-run.jar &
$ java -Dquarkus.http.port=50001 -DCoreEnvironmentBean.nodeIdentifier=node2 -jar target/quarkus-app/quarkus-run.jar &
```

Be sure to give them different node identifiers.

Both coordinators will now be using redis backed store for its transaction logs. Their logs will be isolated from each other because you started them with different nodeIdentifiers.
Create an LRA managed by node 1:

```
COORDINATOR1_URL=http://localhost:50000/lra-coordinator
COORDINATOR2_URL=http://localhost:50001/lra-coordinator
LRA_URL=$(curl -X POST $COORDINATOR1_URL/start | sed "s/\"//g")
```
# see /home/mmusgrov/vimwiki/narayana/notes.lra.md for more

You should find LRA `$LRA` active in the first coordinator:
```
curl $COORDINATOR1_URL
```
but not in the second one:
```
curl $COORDINATOR2_URL
```

If you want to see how the keys space used for the second coordinator differs from the first, try creating an LRA at the second coordinator:
```
LRA_URL=$(curl -X POST $COORDINATOR2_URL/start | sed "s/\"//g")
```

```
$ redis-cli -p 30001 # start the redis cli
127.0.0.1:30001> keys *
1) "{0}:node2:0"
2) "{0}:node1:0"
127.0.0.1:30001> get "{0}:node1:0"
"#BE\x01\x10 @\x00\x00\x00\x00\x1c\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\xff\xff\xc0\xa8\x00\x0e\x00\x00\x81Ud\xa1i\b\x00\x00\x00\x03\x00\x00\x00,/StateManager/BasicAction/LongRunningAction\x00\x00\x00\x00\x01\x00\x00\x00\x00\x00\x00\x00\x1c\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\xff\xff\xc0\xa8\x00\x0e\x00\x00\x81Ud\xa1i\b\x00\x00\x00\x04\x00\x00\x00\xf0#BE\x01\x10 @\x00\x00\x00\x00\t#ARJUNA#\x00\x00\x00\x00\x00\x00\x00\x1c\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\xff\xff\xc0\xa8\x00\x0e\x00\x00\x81Ud\xa1i\b\x00\x00\x00\x03\x00\x00\x00\x1c\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\xff\xff\xc0\xa8\x00\x0e\x00\x00\x81Ud\xa1i\b\x00\x00\x00\x05\x00\x00\x01\x89\x16\x82\xd1\x01\x00\x00\x00\x00\x00\x00\x01\xcf\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x01\xcf\x00\x00\x00Fhttp://localhost:50000/lra-coordinator/0_ffffc0a8000e_8155_64a16908_3\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x01\x00\x00\x00\x00\x00\x00\x00\x01\x00\x00\x01\x89\x16\x82\xd1\x00\x00\x00\x00\x00\x00\x00\x00\aActive\x00\x00"
```

so that's good since it shows the coordinator is using the new redis store which contains the active LRA.

Now stop the first coordinator (ctrl-C or kill %1 etc).

Now migrate the logs by sending an HTTP POST request to the second recocery coordinator resource path telling it to take ownership of the logs created by node1:
```
curl -X POST $COORDINATOR2_URL/recovery/migrate/node1
```

You will need to wait for the recovery manager to detect the new logs, or trigger a synchronous scan:
```
curl -X GET $COORDINATOR2_URL/recovery
```

The response should now contain the migrated LRA or you can list them directly with:
```
curl $COORDINATOR2_URL
```
Notice that previously this last call did not contain the LRA created at node1 but this time it does because of the migrate request.


if you migrate via node 2 then node 1 still has it in its index: see SlotStore#getMatchingKeys

