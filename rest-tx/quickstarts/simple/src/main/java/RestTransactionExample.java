import org.jboss.jbossts.star.util.TxSupport;

public class RestTransactionExample {
    private static final String[] hosts = {"localhost:8080"};
    private static final String TXN_MGR_URL = "http://" + hosts[0] + "/rest-tx/tx/transaction-manager";

    public static void main(String args[]) throws Exception {
        // create a helper with the desired transaction manager resource endpoint
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
