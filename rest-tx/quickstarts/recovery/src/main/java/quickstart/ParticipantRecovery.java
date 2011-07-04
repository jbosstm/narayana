package quickstart;

import org.jboss.jbossts.star.util.TxSupport;
import java.net.HttpURLConnection;

public class ParticipantRecovery {
    private static final int host = 0;
    private static final String[] hosts = {"localhost:8080", "184.72.71.236", "jbossapp1-mmusgrov1.dev.rhcloud.com"};
    private static final String TXN_MGR_URL = "http://" + hosts[host] + "/rest-tx/tx/transaction-manager";

    private static final int SERVICE_PORT = 58082;
    private static final String SERVICE_URL =  "http://localhost:" + SERVICE_PORT + '/' + TransactionAwareResource.PSEGMENT;

    public static void main(String[] args) {
        String opt = (args.length == 0 ? "" : args[0]);

        // the example uses an embedded JAX-RS server for running the service that will take part in a transaction
        JaxrsServer.startServer("localhost", SERVICE_PORT);

        // get a helper for using RESTful transactions, passing in the well know resource endpoint for the transaction manager
        TxSupport txn = new TxSupport(TXN_MGR_URL);

        if ("-r".equals(opt)) {
            System.out.println("Client: waiting for recovery in 2 second intervals (for a max of 130 secs)");

            for (long i = 0l; i < 130l; i += 2) {
                try {
                    // ask the service how many transactions it has committed since the VM started

                    String cnt = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, SERVICE_URL + "/query", "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);

                    if (cnt != null && !"0".equals(cnt)) {
                        System.out.println("SUCCESS participant was recovered after " + i + " seconds. Number of commits: " + cnt);
                        System.exit(0);
                    }

                    Thread.sleep(i * 1000);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.println("FAILURE participant was not recovered");
            System.exit(1);
        }

        // start a REST Atomic transaction
        txn.startTx();

        /*
         * Send two web service requests. Include the resource url for registering durable participation
         * in the transaction with the request
         */
        String serviceRequest = SERVICE_URL + "?enlistURL=" + txn.enlistUrl();

        String wId1 = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest, "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);
        String wId2 = txn.httpRequest(new int[] {HttpURLConnection.HTTP_OK}, serviceRequest, "GET", TxSupport.PLAIN_MEDIA_TYPE, null, null);

        // commit the transaction
        if ("-f".equals(opt)) {
            System.out.println("Client: Failing work load " + wId2);
            TransactionAwareResource.FAIL_COMMIT = wId2;
        }

        System.out.println("Client: Committing transaction");
        txn.commitTx();

        // the web service should have received prepare and commit requests from the transaction manager (TXN_MGR_URL)

        // shutdown the embedded JAX-RS server
        JaxrsServer.stopServer();
    }
}
