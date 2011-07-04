import org.jboss.jbossts.star.util.TxSupport;

public class MultipleTransactions {
    private static final String[] hosts = {"localhost:8080", "184.72.71.236", "jbossapp1-mmusgrov1.dev.rhcloud.com"};
    private static final String TXN_MGR_URL = "http://" + hosts[1] + "/rest-tx/tx/transaction-manager";

    public static void main(String args[]) throws Exception {
        // create a helper with the desired transaction manager resource endpoint
        TxSupport[] txns = { new TxSupport(TXN_MGR_URL), new TxSupport(TXN_MGR_URL)};

        // start transactions
        for (TxSupport txn: txns)
            txn.startTx();

        // verify that all the transactions are active
        for (TxSupport txn: txns)
            if (!txn.txStatus().equals(TxSupport.TX_ACTIVE))
                throw new RuntimeException("A transaction should be active: " + txn.txStatus());

        // see how many RESTful transactions are running (there should be at least one)
        int txnCount = txns[0].txCount();

        if (txns[0].txCount() != 2)
            throw new RuntimeException("At least one transaction did not start");

        // end the transaction
        for (TxSupport txn: txns)
            txn.commitTx();

        // there should now be one fewer transactions
        if (txns[0].txCount() >= txnCount)
            throw new RuntimeException("At least one transaction did not complete");

        System.out.println("Success");
    }
}
