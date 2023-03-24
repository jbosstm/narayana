
DBG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5001"

java -Dquarkus.http.port=50000 -DCoreEnvironmentBean.nodeIdentifier=node1 -jar target/quarkus-app/quarkus-run.jar &
java $DBG -Dquarkus.http.port=50001 -DCoreEnvironmentBean.nodeIdentifier=node2 -jar target/quarkus-app/quarkus-run.jar &

#COORDINATOR1_URL=http://localhost:50000/lra-coordinator
#COORDINATOR2_URL=http://localhost:50001/lra-coordinator
