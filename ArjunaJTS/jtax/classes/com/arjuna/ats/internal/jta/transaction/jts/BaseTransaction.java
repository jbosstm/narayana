/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.*;

import com.arjuna.ats.internal.jta.utils.jts.StatusConverter;

import org.omg.CosTransactions.*;

import com.arjuna.ats.jts.*;
import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.OTSImpleManager;

import javax.transaction.*;

import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.common.*;

import com.arjuna.common.util.logging.*;

import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.SystemException;
import java.lang.SecurityException;
import java.lang.IllegalStateException;
import java.lang.NullPointerException;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.InvalidControl;
import org.omg.CosTransactions.WrongTransaction;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

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
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "BaseTransaction.begin");
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
				throw new NotSupportedException();
			}
			catch (org.omg.CORBA.SystemException e2)
			{
				throw new javax.transaction.SystemException(e2.toString());
			}
		}

		try
		{
			TransactionImple.putTransaction(new TransactionImple());
		}
		catch (org.omg.CosTransactions.SubtransactionsUnavailable e3)
		{
			// shouldn't happen if we get here from the previous checks!

			throw new NotSupportedException();
		}
		catch (org.omg.CORBA.SystemException e4)
		{
			throw new javax.transaction.SystemException(e4.toString());
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
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "BaseTransaction.commit");
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
							+ jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notxe")
							+ ex);
		}

		checkTransactionState();
	}

	public void rollback () throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "BaseTransaction.rollback");
		}

		TransactionImple theTransaction = TransactionImple.getTransaction();

		try
		{
			theTransaction.rollbackAndDisassociate();
		}
		catch (NullPointerException ex)
		{
			throw new IllegalStateException();
		}

		checkTransactionState();
	}

	public void setRollbackOnly () throws java.lang.IllegalStateException,
			javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "BaseTransaction.setRollbackOnly");
		}

		TransactionImple theTransaction = TransactionImple.getTransaction();

		try
		{
			theTransaction.setRollbackOnly();
		}
		catch (NullPointerException ex)
		{
			throw new IllegalStateException(
					jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.nosuchtx"));
		}
	}

	public int getStatus () throws javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "BaseTransaction.getStatus");
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
			throw new javax.transaction.SystemException(e.toString());
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
			throw new javax.transaction.SystemException(e.toString());
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
			throw new javax.transaction.SystemException(e.toString());
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
										+ jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.alreadyassociated"));
					}
				}

				cont = null;
			}
		}
		catch (org.omg.CORBA.SystemException e1)
		{
			throw new javax.transaction.SystemException(e1.toString());
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
		String subtran = jtaPropertyManager.propertyManager.getProperty(com.arjuna.ats.jta.common.Environment.SUPPORT_SUBTRANSACTIONS);

		if ((subtran != null) && (subtran.equals("YES")))
			_supportSubtransactions = true;
	}

}
