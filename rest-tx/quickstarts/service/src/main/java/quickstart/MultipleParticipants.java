package quickstart;

import org.jboss.jbossts.star.util.TxSupport;
import java.net.HttpURLConnection;

public class MultipleParticipants {
    // construct the well known http endpoint for the REST Atomic Transactions coordinator
    private static final int host = 0;
    private static final String[] hosts = {"localhost:8080", "184.72.71.236", "jbossapp1-mmusgrov1.dev.rhcloud.com"};
    private static final String TXN_MGR_URL = "http://" + hosts[host] + "/rest-tx/tx/transaction-manager";

    // construct the endpoint for the example web service that will take part in a transaction
    private static final int SERVICE_PORT = 58082;
    private static final String SERVICE_URL =  "http://localhost:" + SERVICE_PORT + '/' + TransactionAwareResource.PSEGMENT;

    public static void main(String[] args) {
        // the example uses an embedded JAX-RS server for running the service that will take part in a transaction
        JaxrsServer.startServer("localhost", SERVICE_PORT);

        // get a helper for using REST Atomic Transactions, passing in the well know resource endpoint for the transaction coordinator
        TxSupport txn = new TxSupport(TXN_MGR_URL);

        // start a REST Atomic transaction
        txn.startTx();

        /*
         * Send two web service requests. Include the resource url for registering durable participation
         * in the transaction with the request (namely txn.enlistUrl())
         *
         * Each request should cause the service to enlist a unit of work within the transaction.
         */
        String serviceRequest = SERVICE_URL + "?enlistURL=" + txn.enlistUrl();

        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest, "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest, "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);

        /*
         * Commit the transaction (we expect the service to receive a prepare followed by a commit request for each work unit it enlists)
         * Note that if there was only one work unit then the implementation would skip the prepare step.
         */
        System.out.println("Client: Committing transaction");
        txn.commitTx();

        // the web service should have received prepare and commit requests from the transaction coordinator (TXN_MGR_URL) for each work unit
        String cnt = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, SERVICE_URL + "/query", "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);

        // check that the service has been asked to commit twice
        if ("2".equals(cnt))
            System.out.println("SUCCESS: Both service work loads received commit requests");
        else
            System.out.println("FAILURE: At leas one server work load did not receive a commit request: " + cnt);

        // shutdown the embedded JAX-RS server
        JaxrsServer.stopServer();
    }
}
