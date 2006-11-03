/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BaseTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore;

import com.arjuna.ats.jta.common.*;
import com.arjuna.ats.jta.logging.*;

import com.arjuna.common.util.logging.*;

import java.util.Hashtable;

import javax.transaction.NotSupportedException;

import java.lang.IllegalStateException;

/**
 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.notx
 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.notx] no
 *          transaction!
 */

public class BaseTransaction
{

	public void begin() throws javax.transaction.NotSupportedException,
			javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"BaseTransaction.begin");
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
			catch (Exception e2)
			{
				throw new javax.transaction.SystemException(e2.toString());
			}
		}

        final String threadId = Integer.toHexString(System.identityHashCode(Thread.currentThread())) ;
		Integer value = (Integer) _timeouts.get(threadId);
		int v = 0; // if not set then assume 0. What else can we do?

		if (value != null)
		{
			v = value.intValue();
		}

		// TODO set default timeout

		TransactionImple.putTransaction(new TransactionImple(v));
	}

	/**
	 * We will never throw a HeuristicRollbackException because if we get a
	 * HeuristicRollback from a resource, and can successfully rollback the
	 * other resources, this is then the same as having simply been forced to
	 * rollback the transaction during phase 1. The OTS interfaces do not allow
	 * a differentiation.
	 * 
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.cmfailunknownstatus
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.cmfailunknownstatus]
	 *          commit failed with status:
	 */

	public void commit() throws javax.transaction.RollbackException,
			javax.transaction.HeuristicMixedException,
			javax.transaction.HeuristicRollbackException,
			java.lang.SecurityException, java.lang.IllegalStateException,
			javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"BaseTransaction.commit");
		}

		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			throw new IllegalStateException(
					"BaseTransaction.commit - "
							+ jtaLogger.logMesg
									.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.notx"));

		theTransaction.commitAndDisassociate();
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.abfailunknownstatus
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.abfailunknownstatus]
	 *          rollback failed with status:
	 */

	public void rollback() throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"BaseTransaction.rollback");
		}

		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			throw new IllegalStateException(
					"BaseTransaction.rollback - "
							+ jtaLogger.logMesg
									.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.notx"));

		theTransaction.rollbackAndDisassociate();
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.nosuchtx
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.nosuchtx] No
	 *          such transaction!
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.rbofail
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.rbofail]
	 *          Could not mark transaction as rollback only.
	 */

	public void setRollbackOnly() throws java.lang.IllegalStateException,
			javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"BaseTransaction.setRollbackOnly");
		}

		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			throw new IllegalStateException(
					jtaLogger.logMesg
							.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.nosuchtx"));

		theTransaction.setRollbackOnly();
	}

	public int getStatus() throws javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"BaseTransaction.getStatus");
		}

		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			return javax.transaction.Status.STATUS_NO_TRANSACTION;
		else
			return theTransaction.getStatus();
	}

	public void setTransactionTimeout(int seconds)
			throws javax.transaction.SystemException
	{
		if (seconds >= 0)
		{
	        final String threadId = Integer.toHexString(System.identityHashCode(Thread.currentThread())) ;
			_timeouts.put(threadId, new Integer(seconds));
		}
	}

	public int getTimeout() throws javax.transaction.SystemException
	{
        final String threadId = Integer.toHexString(System.identityHashCode(Thread.currentThread())) ;
		Integer value = (Integer) _timeouts.get(threadId);

		if (value != null)
		{
			return value.intValue();
		}
		else
			return 0;
	}

	public String toString()
	{
		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			return "Transaction: unknown";
		else
			return "Transaction: " + theTransaction;
	}

	public TransactionImple createSubordinate () throws javax.transaction.NotSupportedException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"BaseTransaction.createSubordinate");
		}
		
		try
		{
			checkTransactionState();
		}
		catch (IllegalStateException e1)
		{
			throw new NotSupportedException();
		}
		catch (Exception e2)
		{
			throw new javax.transaction.SystemException(e2.toString());
		}
		
        final String threadId = Integer.toHexString(System.identityHashCode(Thread.currentThread())) ;
		Integer value = (Integer) _timeouts.get(threadId);
		int v = 0; // if not set then assume 0. What else can we do?
		
		if (value != null)
		{
			v = value.intValue();
		}
		
		// TODO set default timeout
		
		return new com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.TransactionImple(v);
	}

	protected BaseTransaction()
	{
	}

	/**
	 * Called when we want to make sure this thread does not already have a
	 * transaction associated with it.
	 * 
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.alreadyassociated
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.alreadyassociated]
	 *          thread is already associated with a transaction!
	 */

	final void checkTransactionState() throws IllegalStateException,
			javax.transaction.SystemException
	{
		// ok, no transaction currently associated with thread.

		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			return;
		else
		{
			if ((theTransaction.getStatus() != javax.transaction.Status.STATUS_NO_TRANSACTION)
					&& !_supportSubtransactions)
			{
				throw new IllegalStateException(
						"BaseTransaction.checkTransactionState - "
								+ jtaLogger.logMesg.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.alreadyassociated"));
			}
		}
	}

	private static boolean _supportSubtransactions = false;

	private static Hashtable _timeouts = new Hashtable();

	static
	{
		String subtran = jtaPropertyManager.propertyManager
				.getProperty(com.arjuna.ats.jta.common.Environment.SUPPORT_SUBTRANSACTIONS);

		if ((subtran != null) && (subtran.equals("YES")))
			_supportSubtransactions = true;
	}

}
