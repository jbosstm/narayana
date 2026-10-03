package com.arjuna.mwlabs.wst11.ba.remote;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

/**
 * Implementation of class used to create a subordinate BA transaction
 *
 * This class normally redirects all calls to call the corresponding method of the singleton instance
 * which implements UserBusinessActivity. In the case of a begin call it redirects to a beginSubordinate call
 * on the UserBusinessActivity singleton. In the case of a complete, cancel or compensate it throws a
 * WrongStateException, irrespective of whether the current transaction is top-level or subordinate since
 * these operations should only be attempted via the UserTransactionImple singleton obtained by calling
 * TransactionFactory.userTransaction()
 */
public class UserSubordinateBusinessActivityImple extends UserBusinessActivity
{
    public UserBusinessActivity getUserSubordinateBusinessActivity() {
        return this;
    }

    public void begin() throws WrongStateException, SystemException {
        ((UserBusinessActivityImple)UserBusinessActivityImple.getUserBusinessActivity()).beginSubordinate(0);
    }

    public void begin(int timeout) throws WrongStateException, SystemException {
        ((UserBusinessActivityImple)UserBusinessActivityImple.getUserBusinessActivity()).beginSubordinate(timeout);
    }

    public void close() throws TransactionRolledBackException, UnknownTransactionException, SystemException, WrongStateException {
        throw new WrongStateException();
    }

    public void cancel() throws UnknownTransactionException, SystemException, WrongStateException {
        throw new WrongStateException();
    }

    public void complete() throws UnknownTransactionException, SystemException, WrongStateException {
        throw new WrongStateException();
    }

    public String transactionIdentifier() {
        return ((UserBusinessActivityImple)UserBusinessActivityImple.getUserBusinessActivity()).transactionIdentifier();
    }
}
