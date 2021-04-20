
# start a REST-AT coordinator on port 8080

cd <narayana-repo>/jboss-as/build/target/wildfly-23.0.0.Beta1-SNAPSHOT
cp docs/examples/configs/standalone-rts.xml standalone/configuration
./bin/standalone.sh -c standalone-rts.xml

# verify that it is running, eg:
curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager

# build and run the demo application on port 8081
cd <narayana-repo>/rts/sra
mvn clean install
java $JAVA_OPTS -Dquarkus.http.port=8081 -jar target/sra-coordinator-runner.jar &

# book a trip within a transaction (see sra/demo/api/TripController.java method bookTrip)
# this starts a transaction and you can verify it is running using
# curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager
# except that the delayCommit attribute of `@SRA(value = SRA.Type.REQUIRED, delayCommit = true)` is ignored
# either you or I can raise an issue for that

curl -XPOST http://localhost:8081/trip/book?hotelName=Rex

So you probably want to either change delayCommit = false (which is the default) or you would fix the bug.
If the delayCommit attribute was respected then querying the transaction coordinator (`curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager`) would report the in progress transaction.
