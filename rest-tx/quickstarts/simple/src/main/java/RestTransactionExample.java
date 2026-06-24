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
import org.jboss.jbossts.star.util.TxSupport;

public class RestTransactionExample {
    private static final String[] authorities = {"172.17.130.188:8080", "localhost:8080"};
    private static final String TXN_MGR_URL = "http://" + authorities[1]  + "/rest-tx/tx/transaction-manager";

    //-Dhttp.proxyHost=file.rdu.redhat.com -Dhttp.proxyPort=3128
    public static void main(String args[]) throws Exception {
        // create a helper with thin(e desired transaction manager resource endpoint
        TxSupport txn = new TxSupport(TXN_MGR_URL);

        // start a transaction
        txn.startTx();

        // verify that there is an active transaction
        if (!txn.txStatus().equals(TxSupport.TX_ACTIVE))
            throw new RuntimeException("A transaction should be active: " + txn.txStatus());

        System.out.println("transaction running: " + txn.txStatus());

        // see how many RESTful transactions are running (there should be at least one)
        int txnCount = txn.txCount();

        if (txn.txCount() == 0)
            throw new RuntimeException("The transaction did not start");

        // end the transaction
        txn.commitTx();

        // there should now be one fewer transactions
        if (txn.txCount() >= txnCount)
            throw new RuntimeException("The transaction did not complete");

        System.out.println("Success");
    }
}
