package org.jboss.narayana.txframework.impl.handlers.restat.client;

import org.jboss.jbossts.star.util.TxSupport;

/**
 * @author paul.robinson@redhat.com, 2012-04-12
 */
//todo: this is a quick and dirty naive implementation. Belongs in REST-TX
public class UserTransaction {

    private static final String coordinatorUrl = "http://localhost:8080/rest-tx/tx/transaction-manager";

    private static ThreadLocal<TxSupport> threadTX = new ThreadLocal<TxSupport>();

    public void begin() throws IllegalStateException
    {
        if (threadTX.get() != null)
        {
            throw new IllegalStateException("Transaction already running");
        }
        TxSupport txSupport = new TxSupport();
        txSupport.startTx();
        threadTX.set(txSupport);
    }

    public void commit() throws TransactionRolledBackException
    {
        if (threadTX.get() == null)
        {
            throw new IllegalStateException("Transaction not running");
        }
        TxSupport txSupport = threadTX.get();
        txSupport.commitTx();

        //todo: check if rolled back and throw TransactionRolledBackException

        threadTX.remove();
    }

    public void rollback()
    {
        if (threadTX.get() == null)
        {
            throw new IllegalStateException("Transaction not running");
        }
        TxSupport txSupport = threadTX.get();
        txSupport.rollbackTx();
        threadTX.remove();
    }

    public static TxSupport getTXSupport()
    {
        return threadTX.get();
    }
}
