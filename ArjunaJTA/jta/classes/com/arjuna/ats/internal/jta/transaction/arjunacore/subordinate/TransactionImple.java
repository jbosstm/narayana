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
 * $Id: TransactionImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.internal.jta.transaction.arjunacore.AtomicAction;
import com.arjuna.ats.jta.exceptions.InvalidTerminationStateException;
import com.arjuna.ats.jta.exceptions.UnexpectedConditionException;
import com.arjuna.ats.jta.logging.*;

import java.lang.IllegalStateException;

import javax.transaction.HeuristicCommitException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.SystemException;

// https://jira.jboss.org/jira/browse/JBTM-384

/**
 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.invalidstate
 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.invalidstate]
 *          Not allowed to terminate subordinate transaction directly.
 */

public class TransactionImple extends
		com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple
{

	/**
	 * Create a new transaction with the specified timeout.
	 */

	public TransactionImple (int timeout)
	{
		this(new SubordinateAtomicAction(timeout));
	}
	
	// TODO use the timeout!
	
	public TransactionImple (AtomicAction act)
	{
		super(act);

		TransactionImple.putTransaction(this);
	}

	/**
	 * Overloads Object.equals()
	 */

	public boolean equals (Object obj)
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.equals");
		}

		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj instanceof TransactionImple)
		{
			return super.equals(obj);
		}

		return false;
	}

	/**
	 * This is a subordinate transaction, so any attempt to commit it or roll it
	 * back directly, should fail.
	 */

	public void commit () throws javax.transaction.RollbackException,
			javax.transaction.HeuristicMixedException,
			javax.transaction.HeuristicRollbackException,
			java.lang.SecurityException, javax.transaction.SystemException,
			java.lang.IllegalStateException
	{
		throw new IllegalStateException(
				jtaLogger.logMesg.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.invalidstate"));
	}

	/**
	 * This is a subordinate transaction, so any attempt to commit it or roll it
	 * back directly, should fail.
	 */

	public void rollback () throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		throw new InvalidTerminationStateException(
				jtaLogger.logMesg.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.invalidstate"));
	}

	// Should probably return XA status codes, c.f., XAResource.prepare
	
	public int doPrepare ()
	{
		try
		{
			SubordinateAtomicAction subAct = (SubordinateAtomicAction) super._theTransaction;

			if (!endSuspendedRMs())
				_theTransaction.preventCommit();
			
			int res = subAct.doPrepare();

			switch (res)
			{
			case TwoPhaseOutcome.PREPARE_READONLY:
			case TwoPhaseOutcome.PREPARE_NOTOK:
				TransactionImple.removeTransaction(this);
				break;
			}

			return res;
		}
		catch (ClassCastException ex)
		{
			ex.printStackTrace();

			return TwoPhaseOutcome.INVALID_TRANSACTION;
		}
	}

	public void doCommit () throws IllegalStateException,
			HeuristicMixedException, HeuristicRollbackException,
			javax.transaction.SystemException
	{
		try
		{
			SubordinateAtomicAction subAct = (SubordinateAtomicAction) super._theTransaction;

			int res = subAct.doCommit();

			switch (res)
			{
			case ActionStatus.COMMITTED:
			case ActionStatus.COMMITTING:
			case ActionStatus.H_COMMIT:				
				TransactionImple.removeTransaction(this);
				break;
			case ActionStatus.ABORTED:
			case ActionStatus.ABORTING:
				throw new HeuristicRollbackException();
			case ActionStatus.H_ROLLBACK:
				throw new HeuristicRollbackException();
			case ActionStatus.H_HAZARD:
			case ActionStatus.H_MIXED:
				throw new HeuristicMixedException();
			case ActionStatus.INVALID:
				TransactionImple.removeTransaction(this);
				
				throw new IllegalStateException();
			default:
				throw new HeuristicMixedException(); // not sure what
			// happened,
			// so err on the safe side!
			}
		}
		catch (ClassCastException ex)
		{
			ex.printStackTrace();

			throw new UnexpectedConditionException(ex.toString());
		}
	}

	public void doRollback () throws IllegalStateException,
			HeuristicMixedException, HeuristicCommitException, SystemException
	{
		try
		{
			SubordinateAtomicAction subAct = (SubordinateAtomicAction) super._theTransaction;

			if (!endSuspendedRMs())
			{
				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.arjunacore.endsuspendfailed1");
				}
			}
			
			int res = subAct.doRollback();

			switch (res)
			{
			case ActionStatus.ABORTED:
			case ActionStatus.ABORTING:
			case ActionStatus.H_ROLLBACK:
				TransactionImple.removeTransaction(this);
				
				break;
			case ActionStatus.H_COMMIT:
				throw new HeuristicCommitException();
			case ActionStatus.H_HAZARD:
			case ActionStatus.H_MIXED:
				throw new HeuristicMixedException();
			default:
				throw new HeuristicMixedException();
			}
		}
		catch (ClassCastException ex)
		{
			ex.printStackTrace();

			throw new UnexpectedConditionException(ex.toString());
		}
	}

	public void doForget () throws IllegalStateException
	{
		try
		{
			SubordinateAtomicAction subAct = (SubordinateAtomicAction) super._theTransaction;

			subAct.doForget();
		}
		catch (ClassCastException ex)
		{
			throw new IllegalStateException();
		}
		finally
		{
			TransactionImple.removeTransaction(this);
		}
	}
	
	public void doOnePhaseCommit () throws IllegalStateException,
			javax.transaction.HeuristicRollbackException, javax.transaction.SystemException
	{
		try
		{
			SubordinateAtomicAction subAct = (SubordinateAtomicAction) super._theTransaction;

			if (!endSuspendedRMs())
				_theTransaction.preventCommit();
			
			int status = subAct.doOnePhaseCommit();		

			switch (status)
			{
			case ActionStatus.COMMITTED:
			case ActionStatus.H_COMMIT:
				TransactionImple.removeTransaction(this);
				
				break;
			case ActionStatus.ABORTED:
			case ActionStatus.ABORTING:
			case ActionStatus.H_HAZARD:
			case ActionStatus.H_MIXED:
			default:
				throw new javax.transaction.HeuristicRollbackException();
			case ActionStatus.INVALID:
				throw new InvalidTerminationStateException();
			}
		}
		catch (ClassCastException ex)
		{
			ex.printStackTrace();

			throw new UnexpectedConditionException(ex.toString());
		}
	}

	public String toString ()
	{
		if (super._theTransaction == null)
			return "TransactionImple < ac-subordinate, NoTransaction >";
		else
		{
			return "TransactionImple < ac-subordinate, "
					+ super._theTransaction + " >";
		}
	}
	
	protected void commitAndDisassociate ()
			throws javax.transaction.RollbackException,
			javax.transaction.HeuristicMixedException,
			javax.transaction.HeuristicRollbackException,
			java.lang.SecurityException, javax.transaction.SystemException,
			java.lang.IllegalStateException
	{
		/*
		 * throw new IllegalStateException(
		 * jtaLogger.logMesg.getString("com.arjuna.ats.internal.jta.transaction.jts.subordinate.invalidstate"));
		 */

		throw new InvalidTerminationStateException();
	}

	protected void rollbackAndDisassociate ()
			throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		/*
		 * throw new IllegalStateException(
		 * jtaLogger.logMesg.getString("com.arjuna.ats.internal.jta.transaction.jts.subordinate.invalidstate"));
		 */

		throw new InvalidTerminationStateException();
	}

	/**
	 * Because of recovery, it is possible that a transaction may not be able to
	 * activate itself from the log initially, forcing us to retry later.
	 * 
	 * @return <code>true</code> if the transaction was activated, <code>false</code>
	 * otherwise.
	 */
	
    public boolean activated ()
    {
    	return true;
    }
}
