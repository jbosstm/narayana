package com.arjuna.mwlabs.wst.at.remote;

import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.mwlabs.wst.at.context.TxContextImple;
import com.arjuna.mw.wst.UserTransaction;

/**
 * Implementation of class used to create a subordinate AT transaction
 *
 * This class normally redirects calls to call the corresponding method of the singleton instance
 * which implements UserTransaction. In the case of a begin call it redirects to a beginSubordinate call
 * on the UserTransaction singleton. In the case of a commit or rollback it throws a WrongStateException,
 * irrespective of whether the current transaction is top-level or subordinate since these operations
 * should only be attempted via the UserTransactionImple singleton obtained by calling
 * TransactionFactory.userTransaction()
 */

public class UserSubordinateTransactionImple extends UserTransaction
{
    public UserTransaction getUserSubordinateTransaction() {
        return this;
    }

    public void begin() throws WrongStateException, SystemException {
        ((UserTransactionImple)UserTransactionImple.getUserTransaction()).beginSubordinate(0);
    }

    /**
     * Start a new subordinate transaction with the specified timeout as its lifetime.
     * If an AT transaction is not currently associated with this thread then the
     * WrongStateException will be thrown.
     */
    public void begin (int timeout) throws WrongStateException, SystemException
    {
        ((UserTransactionImple)UserTransactionImple.getUserTransaction()).beginSubordinate(timeout);
    }

    /**
     * it is inappropriate to call this even if the current transaction is a top level AT
     * transaction so we always throw a WrongStateException.
     */
    public void commit() throws TransactionRolledBackException, UnknownTransactionException, SecurityException, SystemException, WrongStateException {
        throw new WrongStateException();
    }

    /**
     * it is inappropriate to call this even if the current transaction is a top level AT
     * transaction so we always throw a WrongStateException.
     */
    public void rollback() throws UnknownTransactionException, SecurityException, SystemException, WrongStateException {
        throw new WrongStateException();
    }

    public String transactionIdentifier() {
        return UserTransactionImple.getUserTransaction().transactionIdentifier();
    }
}
