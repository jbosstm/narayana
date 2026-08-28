OVERVIEW
--------
An example of how to start and end a transaction using REST style semantics and how services
can recover from failures during the commitment protocol.

The demo requires ruby.


USAGE
-----

If you wish to run the demo interactively then use the notes in demo.txt. Ignore Part 2 of demo.txt
unless you wish to migrate part of the demo to the OpenShift Express infrastructure.

Alternatively:

Install ruby (for example on centos type yum install ruby).

Deploy the rest-tx war to an AS7 application server listening for http requests on
localhost:8080 (if you use a different host/port change the relevant url in test.sh).

Eg deploying to AS7:
cp ../../webservice/target/rest-tx-web-<version>.war <AS7>/standalone/deployments/) into a running AS7

and then execute the run script:

./run.sh

EXPECTED OUTPUT
---------------
The test generates a lot of output. The highligths are:

Running recovery demo
... lots of maven output
JAX-RS container waiting for requests on 127.0.0.1:8081 (for 1000 seconds) ...
... more output
Service: PUT request to terminate url: wId=xx, status:=txStatus=TransactionCommitted
Service: Halting VM during commit of work unit wId=xx
Recovering failed service - this could take up to 2 minutes
... more maven output
Service: PUT request to terminate url: wId=xx, status:=txStatus=TransactionCommitted
SUCCESS: Transaction was recovered


WHAT JUST HAPPENED?
-------------------

1. run.sh starts an embedded JAX-RS web service using the mvn compile exec:java maven target.
The web service listens for requests on localhost port 8081.

2. A ruby client starts a transaction (using the REST-AT interface to JBoss Transactions). 

3. The same client interacts with the web service (passing the 'transaction url')
The web service enlists itself into the transaction so that it will be notified by the
transaction coordinator (runing in the standalone JBoss AS container) when the transaction commits.

4. Step 3 is repeated but on a different web service. This ensures that there are two
services enlisted into the transaction (and therefore the transaction cooordinator will send
both prepare and commit requests to each service).

5. The client arranges things such that the second service will halt its JBM when asked to commit.
(for details please refer to the script test.sh and the command:
  ruby client.rb -p $proxies -v Get -a "$service?failWid=${wid2}"
  this request is handled by the JAX-RS service quickstart.TransactionAwareResource in the method
  incrementCounters)

6. The client commits the transaction.

7. The second web service halts its JVM when it receives the commit request.

8. The controlling script run.sh restarts the web service and waits for the transaction
coordinator to recover the transaction.
