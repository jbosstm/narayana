/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BaseTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.jts;

import com.arjuna.ats.internal.jta.utils.jtaxLogger;
import com.arjuna.ats.jta.common.jtaPropertyManager;

import org.omg.CosTransactions.*;

import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.OTSImpleManager;

import com.arjuna.common.util.logging.*;

import javax.transaction.NotSupportedException;
import java.lang.IllegalStateException;
import java.lang.NullPointerException;

/**
 * Some common methods for UserTransaction and TransactionManager.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: BaseTransaction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 *
 * @message com.arjuna.ats.internal.jta.transaction.jts.notx
 *          [com.arjuna.ats.internal.jta.transaction.jts.notx] - no transaction!
 * @message com.arjuna.ats.internal.jta.transaction.jts.invalidtx
 *          [com.arjuna.ats.internal.jta.transaction.jts.invalidtx] - invalid
 *          transaction!
 * @message com.arjuna.ats.internal.jta.transaction.jts.notxe
 *          [com.arjuna.ats.internal.jta.transaction.jts.notxe] - no
 *          transaction! Caught:
 * @message com.arjuna.ats.internal.jta.transaction.jts.nosuchtx
 *          [com.arjuna.ats.internal.jta.transaction.jts.nosuchtx] No such
 *          transaction!
 */

public class BaseTransaction
{

	public void begin () throws javax.transaction.NotSupportedException,
			javax.transaction.SystemException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "BaseTransaction.begin");
		}

		/*
		 * We can supported subtransactions, so should have the option to let
		 * programmer use them. Strict conformance will always say no.
		 */

		if (!BaseTransaction._supportSubtransactions)
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
                javax.transaction.SystemException systemException = new javax.transaction.SystemException(e2.toString());
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
            javax.transaction.SystemException systemException = new javax.transaction.SystemException(e4.toString());
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

	public void commit () throws javax.transaction.RollbackException,
			javax.transaction.HeuristicMixedException,
			javax.transaction.HeuristicRollbackException,
			java.lang.SecurityException, java.lang.IllegalStateException,
			javax.transaction.SystemException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "BaseTransaction.commit");
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
							+ jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notxe")
							+ ex, ex);
		}

		checkTransactionState();
	}

	public void rollback () throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "BaseTransaction.rollback");
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
			javax.transaction.SystemException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "BaseTransaction.setRollbackOnly");
		}

		TransactionImple theTransaction = TransactionImple.getTransaction();

		try
		{
			theTransaction.setRollbackOnly();
		}
		catch (NullPointerException ex)
		{
			throw new IllegalStateException(
					jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.nosuchtx"), ex);
		}
	}

	public int getStatus () throws javax.transaction.SystemException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "BaseTransaction.getStatus");
		}

		TransactionImple theTransaction = TransactionImple.getTransaction();

		try
		{
			return theTransaction.getStatus();
		}
		catch (NullPointerException ex)
		{
			return javax.transaction.Status.STATUS_NO_TRANSACTION;
		}
		catch (Exception e)
		{
            javax.transaction.SystemException systemException = new javax.transaction.SystemException(e.toString());
            systemException.initCause(e);
            throw systemException;
		}
	}

	public void setTransactionTimeout (int seconds)
			throws javax.transaction.SystemException
	{
		try
		{
			OTSImpleManager.current().set_timeout(seconds);
		}
		catch (Exception e)
		{
            javax.transaction.SystemException systemException = new javax.transaction.SystemException(e.toString());
            systemException.initCause(e);
            throw systemException;
		}
	}

	public int getTimeout () throws javax.transaction.SystemException
	{
		try
		{
			return OTSImpleManager.current().get_timeout();
		}
		catch (Exception e)
		{
            javax.transaction.SystemException systemException = new javax.transaction.SystemException(e.toString());
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
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.jts.alreadyassociated
	 *          [com.arjuna.ats.internal.jta.transaction.jts.alreadyassociated]
	 *          thread is already associated with a transaction and
	 *          subtransaction support is not enabled!
	 */

	final void checkTransactionState () throws IllegalStateException,
			javax.transaction.SystemException
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
										+ jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.alreadyassociated"));
					}
				}

				cont = null;
			}
		}
		catch (org.omg.CORBA.SystemException e1)
		{
            javax.transaction.SystemException systemException = new javax.transaction.SystemException(e1.toString());
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

	private static boolean _supportSubtransactions = false;

	static
	{
        _supportSubtransactions = jtaPropertyManager.getJTAEnvironmentBean().isSupportSubtransactions();
    }
}
