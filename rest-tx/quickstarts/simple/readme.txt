OVERVIEW
--------
An example of how to start and end a transaction using REST style semantics.


USAGE
-----
Deploy the rest-tx war (rest-tx-web-5.0.0.M1-SNAPSHOT.war) into a running AS7 (or AS6) application server listening
for http requests on localhost (if you use a different host/port change the url in RestTransactionExample.TXN_MGR_URK).

mvn compile exec:exec

or use the run script

EXPECTED OUTPUT
---------------
[The examples run under the control of maven so you will need to filter maven output from example output.]

	transaction running: txStatus=TransactionActive
	Success


WHAT JUST HAPPENED?
-------------------
We deployed a JAX-RS servlet that implements a RESTful interface to the Narayana transaction manager (TM).
The example started a transaction by POSTing to a well known url that the JAX-RS servlet was listening on.
The servlet looked up the TM running in the container it was deployed to and started a transaction.
The servlet returned a URL (the transaction URL) that the example can use to control the termination of the active transaction.
The example performed an HTTP GET request on the transaction URL to check that the transaction status was active.
The example counted the number of running transactions (in order to check that at least one was active).
The example performed an HTTP PUT request to the transaction URL thus terminating the transaction.
The example counted the number of running transactions in order to verify that there was one less transaction.
