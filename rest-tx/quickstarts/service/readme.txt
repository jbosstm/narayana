OVERVIEW
--------
This example shows how you can make your web services transactional. [It uses Jersey and Grizzly
for deploying the participant web services, but any conforming web container will do].


USAGE
-----
Deploy the rest-tx war (rest-tx-web-5.0.0.M1-SNAPSHOT.war) into a running AS7 (or AS6) application server
that is listening for http requests on localhost (if you use a different host/port change the url in
MultipleParticipants.TXN_MGR_URL).

mvn compile exec:exec

or use the run.sh or run.bat script.


EXPECTED OUTPUT
---------------
4. And finally the build will report success or failure:

	[INFO] BUILD SUCCESS

indicates a successful outcome.


1. You will see 2 lines of output showing the service enlisting into the transaction:
	Service: Enlisting terminator=http://localhost:58082/service/1/1/terminate;durableparticipant=http://localhost:58082/service/1/1/terminator
	Service: Enlisting terminator=http://localhost:58082/service/1/2/terminate;durableparticipant=http://localhost:58082/service/1/2/terminator

2. A message showing the client committing transaction:

	Client: Committing transaction

3. Two resources will output two messages during the transaction commitment protocol. One
saying that it has prepared and the other reporting that it has committed.

	Service: PUT request to terminate url: wId=2, status:=txStatus=TransactionPrepared
	Service: PUT request to terminate url: wId=1, status:=txStatus=TransactionPrepared
	Service: PUT request to terminate url: wId=1, status:=txStatus=TransactionCommitted
	Service: PUT request to terminate url: wId=2, status:=txStatus=TransactionCommitted

4. The client checks that the service got both commit requests:

	SUCCESS: Both service work loads received commit requests

WHAT JUST HAPPENED?
-------------------
1. We deployed a JAX-RS servlet that implements a RESTful interface to the Narayana transaction manager (TM)
(running in an AS7 or AS6 container).

2. The client (MultipleParticpants.java) started an embedded web server (JaxrsServer.java) for hosting web services.

3 The client then started a REST Atomic Transaction and got back two urls: one for completing the transaction
and one for use with enlisting durable participants (implemented by the class TransactionAwareResource.java)
into the transaction.

4. The client then made two HTTP requests to a web service and passed the participant enlistment url as part
of the context of the request (using a query parameter).

5. The participants used the enlistment url to join the transaction. In this naive example we assumes that
each request is for a separate unit of transactional work and in this way we end up with multiple participants
being involved in the transaction. Having more than one participant means we can demonstrate that either all
participants will commit or none them will.

6. The client commits the transaction using the resource url it got back when it created the transaction.

7. The transaction manager implementation knows that there are two resources involved and asks them both to
prepare their work (using the resource urls it got from the participants when they enlisted into the transaction).
If the participant resources respond with the correct HTTP status code and body then the transaction manager
proceeds to request commitment. Refer to the REST Atomic Transactions spec for full details.

8. The client sends a request to the service asking how many times it was asked to commit work units.
If it was twice then SUCCESS is printed otherwise FAILURE is printed.
