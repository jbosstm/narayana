package quickstart;

import org.jboss.jbossts.star.util.TxSupport;
import java.net.HttpURLConnection;

public class MultipleParticipants {
    private static final int host = 0;
    private static final String[] hosts = {"localhost:8080", "184.72.71.236", "jbossapp1-mmusgrov1.dev.rhcloud.com"};
    private static final String TXN_MGR_URL = "http://" + hosts[host] + "/rest-tx/tx/transaction-manager";

    private static final int SERVICE_PORT = 58082;
    private static final String SERVICE_URL =  "http://localhost:" + SERVICE_PORT + '/' + TransactionAwareResource.PSEGMENT;

    public static void main(String[] args) {
        // the example uses an embedded JAX-RS server for running the service that will take part in a transaction
        JaxrsServer.startServer("localhost", SERVICE_PORT);

        // get a helper for using RESTful transactions passing in the resource endpoint for the transaction manager
        TxSupport txn = new TxSupport(TXN_MGR_URL);

        // start a RESTful transaction
        txn.startTx();

        /*
         * Send two web service requests. Include the resource url for registering durable participation
         * in the transaction with the request
         */
        String serviceRequest = SERVICE_URL + "?enlistURL=" + txn.enlistUrl();

        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest, "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);
        txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest, "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);

        // commit the transaction
        System.out.println("committing transaction");
        txn.commitTx();

        // the web service should have received prepare and commit requests from the transaction manager (TXN_MGR_URL)

        // shutdown the embedded JAX-RS server
        JaxrsServer.stopServer();
    }
}
