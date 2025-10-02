/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package quickstart;

import java.net.HttpURLConnection;

import org.jboss.jbossts.star.util.TxSupport;

public class MultipleParticipants {
    // construct the endpoint for the example web service that will take part in a transaction
    private static final int SERVICE_PORT = 58082;
    private static final String SERVICE_URL =  "http://localhost:" + SERVICE_PORT + '/' + TransactionAwareResource.PSEGMENT;

    public static void main(String[] args) {
        String authority = "localhost:8080";  //"184.72.71.236", "jbossapp1-mmusgrov1.dev.rhcloud.com"

        if (args.length > 1 && "-h".equals(args[0]))
            authority = args[1];

//        authority = "eapp-mmusgrov.dev.rhcloud.com";
        // the example uses an embedded JAX-RS server for running the service that will take part in a transaction
        JaxrsServer.startServer("localhost", SERVICE_PORT);

        // get a helper for using REST Atomic Transactions, passing in the well know resource endpoint for the transaction coordinator
        TxSupport txn = new TxSupport("http://" + authority + "/rest-tx/tx/transaction-manager");

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

        // shutdown the embedded JAX-RS server
        JaxrsServer.stopServer();

        // check that the service has been asked to commit twice
        if ("2".equals(cnt))
            System.out.println("SUCCESS: Both service work loads received commit requests");
        else
            throw new RuntimeException("FAILURE: At least one server work load did not receive a commit request: " + cnt);
    }
}
