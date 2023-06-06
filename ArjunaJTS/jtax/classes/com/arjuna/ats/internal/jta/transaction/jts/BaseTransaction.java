/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.jts;

import jakarta.transaction.NotSupportedException;

import org.omg.CORBA.TRANSACTION_UNAVAILABLE;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;

import com.arjuna.ats.internal.jta.utils.jtaxLogger;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jts.OTSManager;

/**
 * Some common methods for UserTransaction and TransactionManager.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: BaseTransaction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public class BaseTransaction
{

	public void begin () throws jakarta.transaction.NotSupportedException,
			jakarta.transaction.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("BaseTransaction.begin");
        }

		/*
		 * We can supported subtransactions, so should have the option to let
		 * programmer use them. Strict conformance will always say no.
		 */

		if (!_supportSubtransactions)
		{
			try
			{
				checkTransactionState();
			}
			catch (IllegalStateException e1)
			{
                NotSupportedException notSupportedException = new NotSupportedException(e1.getMessage());
                notSupportedException.initCause(e1);
				throw notSupportedException;
			}
			catch (org.omg.CORBA.SystemException e2)
			{
                jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e2.toString());
                systemException.initCause(e2);
				throw systemException;
			}
		}

		try
		{
			TransactionImple.putTransaction(new TransactionImple());
		}
		catch (org.omg.CosTransactions.SubtransactionsUnavailable e3)
		{
			// shouldn't happen if we get here from the previous checks!

            NotSupportedException notSupportedException = new NotSupportedException(e3.getMessage());
            notSupportedException.initCause(e3);
            throw notSupportedException;
		}
		catch (org.omg.CORBA.SystemException e4)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e4.toString());
            systemException.initCause(e4);
            throw systemException;
		}
	}

	/**
	 * We will never throw a HeuristicRollbackException because if we get a
	 * HeuristicRollback from a resource, and can successfully rollback the
	 * other resources, this is then the same as having simply been forced to
	 * rollback the transaction during phase 1. The OTS interfaces do not allow
	 * a differentiation.
	 */

	public void commit () throws jakarta.transaction.RollbackException,
			jakarta.transaction.HeuristicMixedException,
			jakarta.transaction.HeuristicRollbackException,
			java.lang.SecurityException, java.lang.IllegalStateException,
			jakarta.transaction.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("BaseTransaction.commit");
        }

		TransactionImple theTransaction = TransactionImple.getTransaction();

		try
		{
			theTransaction.commitAndDisassociate();
		}
		catch (NullPointerException ex)
		{
			ex.printStackTrace();

			throw new IllegalStateException(
                    "BaseTransaction.commit - "
                            + jtaxLogger.i18NLogger.get_jtax_transaction_jts_notxe()
							+ ex, ex);
		}

		checkTransactionState();
	}

	public void rollback () throws java.lang.IllegalStateException,
			java.lang.SecurityException, jakarta.transaction.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("BaseTransaction.rollback");
        }

		TransactionImple theTransaction = TransactionImple.getTransaction();

		try
		{
			theTransaction.rollbackAndDisassociate();
		}
		catch (NullPointerException ex)
		{
			throw new IllegalStateException(ex);
		}

		checkTransactionState();
	}

	public void setRollbackOnly () throws java.lang.IllegalStateException,
			jakarta.transaction.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("BaseTransaction.setRollbackOnly");
        }

		TransactionImple theTransaction = TransactionImple.getTransaction();

		try
		{
			theTransaction.setRollbackOnly();
		}
		catch (NullPointerException ex)
		{
			throw new IllegalStateException(
                    jtaxLogger.i18NLogger.get_jtax_transaction_jts_nosuchtx(), ex);
		}
	}

	public int getStatus () throws jakarta.transaction.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("BaseTransaction.getStatus");
        }

		TransactionImple theTransaction = null;
	
		try {
		theTransaction = TransactionImple.getTransaction();
		} catch (TRANSACTION_UNAVAILABLE e) {
		    if (e.minor == 1) {
	            return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
		    }
		}
		
		if (theTransaction == null) {
		    return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
		}

		try
		{
			return theTransaction.getStatus();
		}
		catch (NullPointerException ex)
		{
			return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
		}
		catch (Exception e)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e.toString());
            systemException.initCause(e);
            throw systemException;
		}
	}

	public void setTransactionTimeout (int seconds)
			throws jakarta.transaction.SystemException
	{
		try
		{
			OTSImpleManager.current().set_timeout(seconds);
		}
		catch (Exception e)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e.toString());
            systemException.initCause(e);
            throw systemException;
		}
	}

	public int getTimeout () throws jakarta.transaction.SystemException
	{
		try
		{
			return OTSImpleManager.current().get_timeout();
		}
		catch (Exception e)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e.toString());
            systemException.initCause(e);
            throw systemException;
		}
	}

	protected BaseTransaction ()
	{
	}

	/**
	 * Called when we want to make sure this thread does not already have a
	 * transaction associated with it.
	 */

	final void checkTransactionState () throws IllegalStateException,
			jakarta.transaction.SystemException
	{
		try
		{
			Control cont = OTSManager.get_current().get_control();

			/*
			 * Control may not be null, but its coordinator may be.
			 */

			if (cont != null)
			{
				Coordinator coord = cont.get_coordinator();

				if (coord != null)
				{
					if ((coord.get_status() == org.omg.CosTransactions.Status.StatusActive)
							&& (!_supportSubtransactions))
					{
						throw new IllegalStateException(
                                "BaseTransaction.checkTransactionState - "
                                        + jtaxLogger.i18NLogger.get_jtax_transaction_jts_alreadyassociated());
					}
				}

				cont = null;
			}
		}
		catch (org.omg.CORBA.SystemException e1)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e1.toString());
            systemException.initCause(e1);
            throw systemException;
		}
		catch (org.omg.CosTransactions.Unavailable e2)
		{
			// ok, no transaction currently associated with thread.
		}
		catch (NullPointerException ex)
		{
			// ok, no transaction currently associated with thread.
		}
	}

	private static boolean _supportSubtransactions = jtaPropertyManager.getJTAEnvironmentBean()
            .isSupportSubtransactions();
}