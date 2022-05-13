
# start a REST-AT coordinator on port 8080

cd <narayana-repo>/jboss-as/build/target/wildfly-23.0.0.Beta1-SNAPSHOT
cp docs/examples/configs/standalone-rts.xml standalone/configuration
./bin/standalone.sh -c standalone-rts.xml

# verify that it is running, eg:
curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager

# build and run the demo application on port 8081
cd <narayana-repo>/rts/sra
mvn clean install
java $JAVA_OPTS -Dquarkus.http.port=8081 -jar target/sra-participant-runner.jar &

# book a trip within a transaction (see sra/demo/api/TripController.java method bookTrip)
# this starts a transaction and you can verify it is running using
# curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager
# except that the end attribute of `@SRA(value = SRA.Type.REQUIRED, end = false)` is ignored
# either you or I can raise an issue for that

curl -XPOST http://localhost:8081/trip/book?hotelName=Rex

So you probably want to either change end = true (which is the default) or you would fix the bug.
If the end attribute was respected then querying the transaction coordinator (`curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager`) would report the in progress transaction.


# examples of how to manually test that the coordinator is working using curl

curl -H "Content-Type: application/x-www-form-urlencoded" -X POST http://localhost:8080/rest-at-coordinator/tx/transaction-manager
curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager
curl -X PUT --data txstatus=TransactionCommitted http://localhost:8080/rest-at-coordinator/tx/transaction-manager/0_ffffc0a8000e_-60f54f29_60ad4e96_91/terminator
curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager

