OVERVIEW
--------
This example shows how you can make your web services transactional. [The example uses Jersey and Grizzly
for deploying the participant web services but any conforming web container will do].


USAGE
-----
Deploy the rest-tx war (rest-tx-web-5.0.0.M1-SNAPSHOT.war) into a running AS7 (or AS6) application server listening
for http requests on localhost (if you use a different host/port change the url in MultipleParticipants.TXN_MGR_URK).

mvn compile exec:exec


EXPECTED OUTPUT
---------------
1. You will see 2 lines of output showing the service enlisting into the transaction.
2. A message showing the client committing transaction.
3. Two resouces will output two messages during the transaction committment protocol. One
saying that it has prepared and the other reporting that it has committed.
4. And finally the build will report success or failure:

	[INFO] BUILD SUCCESS

indicates a successful outcome.


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
beining involved in the transaction. Having more than one participant means we can demonstrate that either all
participants will commit or none them will.

6. The client commits the transaction using the resource url it got back when it created the transaction.

7. The transaction manager implementation knows that there are two resources involved and asks them both to
prepare their work (using the resource urls it got from the participants when they enlisted into the transaction).
If the particpant resources respond with the correct HTTP status code and body then the transaction manager
proceeds to request committment. Refer to the REST Atomic Transactions spec for full details.
