/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.arjunacore;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.Transaction;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.jta.logging.jtaLogger;

public class TransactionManagerImple extends BaseTransaction implements
		jakarta.transaction.TransactionManager, javax.naming.spi.ObjectFactory
{

	public TransactionManagerImple()
	{
	}

	public Transaction getTransaction()
			throws jakarta.transaction.SystemException
	{
		return TransactionImple.getTransaction();
	}

	/**
	 * @return the suspended transaction.
	 */

	public Transaction suspend() throws jakarta.transaction.SystemException
	{
		if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("TransactionImpleManager.suspend");
        }

		try
		{
			TransactionImple tx = TransactionImple.getTransaction();

			if (tx != null)
			{
				tx.getAtomicAction().suspend();
			}

			return tx;
		}
		catch (Exception e)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e.toString());
            systemException.initCause(e);
            throw systemException;
		}
	}

	/**
	 * Unlike the OTS, if we are already associated with a transaction then we
	 * cannot call resume.
	 */

	public void resume(Transaction which) throws InvalidTransactionException,
			java.lang.IllegalStateException, jakarta.transaction.SystemException
	{
		if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("TransactionImpleManager.resume");
        }

		super.checkTransactionState();

		/*
		 * If we are here then there is no transaction associated with the
		 * thread.
		 */

		if ((which == null) || (which instanceof TransactionImple))
		{
		    TransactionImple theTransaction = (TransactionImple) which;

		    try
		    {
		        AtomicAction act = ((theTransaction == null) ? null : theTransaction.getAtomicAction());

		        if (!AtomicAction.resume(act))
		            throw new InvalidTransactionException();

		        theTransaction = null;
		    }
		    catch (final Exception e2)
		    {
		        jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException();
                systemException.initCause(e2);
                throw systemException;
		    }
		}
		else
		    throw new InvalidTransactionException("Illegal type is: "
		            + which);
	}

	/**
	 * Creates a TransactionManageImple from the given information.
	 *
	 * @param obj
	 * @param name
	 * @param nameCtx
	 * @param environment
	 * @return the instance of the transaction manager
	 * @throws Exception
	 */
	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
			Hashtable environment) throws Exception
	{
		return this;
	}
}