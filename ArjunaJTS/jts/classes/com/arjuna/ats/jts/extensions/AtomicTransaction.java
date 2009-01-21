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
 * $Id: AtomicTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.extensions;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.utils.Utility;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ControlWrapper;

import org.omg.CosTransactions.*;
import org.omg.CORBA.CompletionStatus;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.Unavailable;
import org.omg.CosTransactions.InvalidControl;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

/**
 * Similar to CosTransactions::Current. However, this class does transaction
 * scoping, so if an instance is garbage collected and the transaction is still
 * running, it will be rolled back automatically. It also adds some convenience
 * routines which Current does not do.
 * 
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: AtomicTransaction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class AtomicTransaction
{

	/**
	 * The types of transactions which can be created.
	 */

	public static final int TOP_LEVEL = 0;
	public static final int NESTED = 1;

	/**
	 * Create a new transaction. It is not running at this stage.
	 */

	public AtomicTransaction ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::AtomicTransaction ()");
		}

		_theAction = null;
		_theStatus = Status.StatusNoTransaction;
		_timeout = get_timeout();
	}

	/**
	 * @message com.arjuna.ats.jts.extensions.atscope {0} - running atomic
	 *          transaction going out of scope. Will roll back. {1}
	 * @message com.arjuna.ats.jts.extensions.namefail Cannot determine
	 *          transaction name!
	 * @message com.arjuna.ats.jts.extensions.abortfailnoexist Could not
	 *          rollback transaction {0} as it does not exist!
	 * @message com.arjuna.ats.jts.extensions.abortfail Could not rollback
	 *          transaction {0}
	 */

	public void finalize ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction.finalize ()");
		}

		if (_theAction != null)
		{
			if (getStatus() == Status.StatusActive)
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.atscope", new Object[]
					{ "AtomicTransaction.finalize", get_uid().toString() });
				}

				String name = null;

				try
				{
					name = get_transaction_name();
				}
				catch (SystemException ex)
				{
					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.namefail");
					}
				}

				try
				{
					rollback(); // tidies up for us.
				}
				catch (NoTransaction e)
				{
					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.abortfailnoexist", new Object[]
						{ name });
					}
				}
				catch (Exception e)
				{
					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.abortfail", new Object[]
						{ name });
					}
				}
			}
		}
	}

	/**
	 * @return the transaction name. Should only be used for debugging purposes.
	 * 
	 * @message com.arjuna.ats.jts.extensions.atunavailable {0} - transaction
	 *          unavailable.
	 */

	public String get_transaction_name () throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::get_transaction_name ()");
		}

		if (_theAction != null)
		{
			try
			{
				return _theAction.get_transaction_name();
			}
			catch (SystemException e)
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.atunavailable", new Object[]
					{ "AtomicTransaction.get_transaction_name" });
				}

				throw e;
			}
		}
		else
			throw new UNKNOWN(ExceptionCodes.UNKNOWN_EXCEPTION,
					CompletionStatus.COMPLETED_NO);
	}

	/**
	 * Start the transaction.
	 * 
	 * @exception org.omg.CosTransactions.SubtransactionsUnavailable
	 *                if subtransactions have been disabled, and the invoking
	 *                thread already has a transaction associated with it.
	 * 
	 * @exception org.omg.CORBA.INVALID_TRANSACTION
	 *                if the transaction has already begun or has completed.
	 */

	public void begin () throws SubtransactionsUnavailable, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::begin ()");
		}

		// already begun?

		CurrentImple current = OTSImpleManager.current();

		synchronized (_theStatus)
		{
			if (_theAction != null)
			{
				throw new INVALID_TRANSACTION(ExceptionCodes.ALREADY_BEGUN,
						CompletionStatus.COMPLETED_NO);
			}

		        current.begin();

			_theAction = current.getControlWrapper();
		}

		_theStatus = current.get_status();

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::begin create "
					+ _theAction);
		}
	}

	/*
	 * Commit the transaction. If the current transaction associated with the
	 * thread is not this transaction, then this transaction is rolled back and
	 * leave current alone.
	 * 
	 * @param report_heuristics indicates whether heuristic reporting is
	 * desired.
	 * 
	 * @exception org.omg.CosTransactions.NoTransaction if the transaction has
	 * already been terminated.
	 * 
	 * @exception org.omg.CORBA.TRANSACTION_ROLLEDBACK if the transaction rolls
	 * back.
	 * 
	 * @exception org.omg.CosTransactions.HeuristicMixed if some of the
	 * transaction participants committed, while some rolled back.
	 * 
	 * @exception org.omg.CosTransactions.HeuristicHazard if some of the
	 * transaction participants committed, some rolled back, and the outcome of
	 * others is indeterminate.
	 * 
	 * @exception org.omg.CORBA.WRONG_TRANSACTION if the current transaction is
	 * not this transaction.
	 */

	public void commit (boolean report_heuristics) throws NoTransaction,
			HeuristicMixed, HeuristicHazard, WrongTransaction, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::commit ( "
					+ report_heuristics + " ) for " + _theAction);
		}

		/*
		 * We shouldn't need to synchronize for the entire duration of the
		 * commit since we actually use Current to do the transaction
		 * termination and only look at _theAction to check that Current's
		 * notion of the transaction is the same. If we only synchronize when we
		 * do the check then it is still possible for multiple threads to get
		 * past this point and try to commit the transaction. However, then
		 * Current will do the checking for us.
		 */

		synchronized (_theStatus)
		{
			if (_theAction == null)
			{
				throw new NoTransaction();
			}
		}

		if (!validTransaction())
		{
			throw new WrongTransaction();
		}

		/*
		 * OK to use current since we have just guaranteed that the transaction
		 * is the same as current. Use current rather than saved control since
		 * it will do thread tracking for us.
		 */

		CurrentImple current = OTSImpleManager.current();

		/*
		 * Release our handle first, since current is about to destroy the
		 * action control.
		 */

		try
		{
			current.commit(report_heuristics);

			_theStatus = Status.StatusCommitted;
		}
		catch (NoTransaction e)
		{
			_theStatus = Status.StatusNoTransaction;

			throw e;
		}
		catch (HeuristicMixed e)
		{
			_theStatus = getStatus();

			throw e;
		}
		catch (HeuristicHazard e)
		{
			_theStatus = getStatus();

			throw e;
		}
		catch (TRANSACTION_ROLLEDBACK e)
		{
			_theStatus = Status.StatusRolledBack;

			throw e;
		}
		catch (SystemException e)
		{
			_theStatus = getStatus();

			throw e;
		}
	}

	/*
	 * Rollback the transaction. If the current transaction associated with the
	 * thread is not this transaction, then this transaction is rolled back and
	 * leave current alone.
	 * 
	 * @exception org.omg.CosTransactions.NoTransaction if the transaction has
	 * already been terminated.
	 * 
	 * @exception org.omg.CORBA.WRONG_TRANSACTION if the current transaction is
	 * not this transaction.
	 */

	public void rollback () throws NoTransaction, WrongTransaction,
			SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::rollback for "
					+ _theAction);
		}

		/*
		 * We shouldn't need to synchronize for the entire duration of the
		 * rollback since we actually use Current to do the transaction
		 * termination and only look at _theAction to check that Current's
		 * notion of the transaction is the same. If we only synchronize when we
		 * do the check then it is still possible for multiple threads to get
		 * past this point and try to rollback the transaction. However, then
		 * Current will do the checking for us.
		 */

		synchronized (_theStatus)
		{
			if (_theAction == null)
			{
				throw new NoTransaction();
			}
		}

		if (!validTransaction())
		{
			throw new WrongTransaction();
		}

		/*
		 * OK to use current since we have just guaranteed that the transaction
		 * is the same as current. Use current rather than saved control since
		 * it will do thread tracking for us.
		 */

		CurrentImple current = OTSImpleManager.current();

		try
		{
			current.rollback();

			_theStatus = Status.StatusRolledBack;
		}
		catch (NoTransaction e)
		{
			_theStatus = Status.StatusNoTransaction;

			throw e;
		}
		catch (TRANSACTION_ROLLEDBACK e)
		{
			_theStatus = Status.StatusRolledBack;

			throw e;
		}
		catch (SystemException e)
		{
			_theStatus = getStatus();

			throw e;
		}
	}

	/**
	 * Set the transaction timeout. This is the same as calling
	 * org.omg.CosTransactions.Current.set_timeout().
	 */

	public void set_timeout (int seconds) throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::set_timeout ( "
					+ seconds + " )");
		}

		CurrentImple current = OTSImpleManager.current();

		if (current != null)
			current.set_timeout(seconds);
		else
			throw new UNKNOWN();
	}

	/**
	 * @return the timeout associated with transactions created by the current
	 *         thread.
	 */

	public int get_timeout () throws SystemException
	{
		CurrentImple current = OTSImpleManager.current();

		if (current != null)
		{
			int val = current.get_timeout();

			if (jtsLogger.logger.isDebugEnabled())
			{
				jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::get_timeout returning "
						+ val);
			}

			return val;
		}
		else
			throw new UNKNOWN();
	}

	/**
	 * @return the timeout associated with this transaction.
	 */

	public int getTimeout () throws SystemException
	{
		return _timeout;
	}

	/**
	 * @return the propagation context for this transaction.
	 * 
	 * @since JTS 2.1.
	 * 
	 * @exception org.omg.CosTransactions.Inactive
	 *                if the current transaction is no longer in the active
	 *                phase.
	 */

	public PropagationContext get_txcontext () throws Inactive, SystemException
	{
		if (_theAction == null)
		{
			throw new Inactive();
		}
		else
		{
			try
			{
				return _theAction.get_coordinator().get_txcontext();
			}
			catch (NullPointerException ex)
			{
				throw new Inactive();
			}
			catch (Exception e)
			{
				throw new BAD_OPERATION(e.toString());
			}
		}
	}

	/**
	 * Register the specified resource with this transaction.
	 * 
	 * @return the org.omg.CosTransactions.RecoveryCoordinator reference that
	 *         can be used to later query the outcome of the transaction.
	 * 
	 * @exception org.omg.CosTransactions.Inactive
	 *                if the current transaction is no longer in the active
	 *                phase.
	 */

	public RecoveryCoordinator registerResource (Resource r) throws Inactive,
			SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::registerResource ( "
					+ r + " )");
		}

		RecoveryCoordinator rc = null;

		synchronized (_theStatus)
		{
			if (_theAction == null)
			{
				throw new Inactive();
			}
		}

		return _theAction.register_resource(r);
	}

	/**
	 * Register the specified subtransaction aware resource with this
	 * transaction. This transaction must be a subtransaction.
	 * 
	 * @exception org.omg.CosTransactions.Inactive
	 *                if this transaction is no longer in the active phase.
	 * 
	 * @exception org.omg.CosTransactions.NotSubtransaction
	 *                if this transaction is not a subtransaction.
	 */

	public void registerSubtranAware (SubtransactionAwareResource r)
			throws Inactive, NotSubtransaction, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::registerSubtranAware ( "
					+ r + " )");
		}

		synchronized (_theStatus)
		{
			if (_theAction == null)
			{
				throw new Inactive();
			}
		}

		_theAction.register_subtran_aware(r);
	}

	/**
	 * Register the specified synchronization with this transaction. This
	 * transaction must be a top-level transaction.
	 * 
	 * @exception org.omg.CosTransactions.Inactive
	 *                if this transaction is no longer in the active phase.
	 * 
	 * @exception org.omg.CosTransactions.SynchronizationUnavailable
	 *                if this transaction it not a top-level transaction.
	 */

	public void registerSynchronization (Synchronization sync) throws Inactive,
			SynchronizationUnavailable, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::registerSynchronization ( "
					+ sync + " )");
		}

		synchronized (_theStatus)
		{
			if (_theAction == null)
			{
				throw new Inactive();
			}
		}

		_theAction.register_synchronization(sync);
	}

	/*
	 * Should probably remove ability to get control as this allows a user to
	 * commit/abort outside the control of the AtomicTransaction.
	 */

	/**
	 * @return the org.omg.CosTransactions.Control reference to this
	 *         transaction.
	 */

	public Control control () throws NoTransaction, SystemException
	{
		if (_theAction == null)
		{
			throw new NoTransaction();
		}
		else
		{
			try
			{
				return _theAction.get_control();
			}
			catch (Unavailable ex)
			{
				throw new NoTransaction();
			}
		}
	}

	public boolean equals (Object obj)
	{
		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj instanceof AtomicTransaction)
		{
			/*
			 * If we can't get either coordinator to compare, then assume
			 * transactions are different.
			 */

			try
			{
				AtomicTransaction tx = (AtomicTransaction) obj;
				ControlWrapper txControl = tx._theAction;

				if ((_theAction == null) && (txControl == null))
					return true;
				else
					return _theAction.equals(txControl);
			}
			catch (Exception e)
			{
			}
		}

		return false;
	}

	/**
	 * Suspend this transaction from the current thread. This transaction must
	 * be active on the calling thread for this method to succeed.
	 * 
	 * @exception org.omg.CosTransactions.NoTransaction
	 *                if there is no current transaction.
	 * 
	 * @exception org.omg.CORBA.WRONG_TRANSACTION
	 *                if the transaction associated with the current thread is
	 *                different from this thread.
	 */

	public void suspend () throws NoTransaction, WrongTransaction,
			SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::suspend called for "
					+ _theAction);
		}

		synchronized (_theStatus)
		{
			if (_theAction != null)
				throw new NoTransaction();
		}

		if (!validTransaction())
		{
			throw new WrongTransaction();
		}

		synchronized (_theStatus)
		{
        		_theAction = OTSImpleManager.current().suspendWrapper();
        		
        		/*
        		 * Make sure to set the status in case we get called again.
        		 */
        		
        		if (_theAction == null)
        		    _theStatus = org.omg.CosTransactions.Status.StatusNoTransaction;
		}
	}

	/**
	 * Resume this transaction.
	 * 
	 * @exception org.omg.CosTransactions.InvalidControl
	 *                if this transaction is invalid.
	 */

	public void resume () throws InvalidControl, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::resume called for "
					+ _theAction);
		}

		synchronized (_theStatus)
		{
			if (_theAction == null)
				throw new InvalidControl();
		}

		OTSImpleManager.current().resumeWrapper(_theAction);
	}

	/**
	 * @return the status of this transaction.
	 */

	public org.omg.CosTransactions.Status get_status () throws SystemException
	{
		_theStatus = getStatus();

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::get_status called for "
					+ _theAction
					+ " returning "
					+ Utility.stringStatus(_theStatus));
		}

		return _theStatus;
	}

	/**
	 * Allow action commit to be supressed. Although the OTS would require an InactiveException
	 * if the transaction is not active, we ignore that via this route.
	 */

	public void rollbackOnly () throws SystemException, NoTransaction
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::rollbackOnly called for "
					+ _theAction);
		}

		synchronized (_theStatus)
		{
			if (_theAction == null)
				throw new NoTransaction();
		}

		_theAction.preventCommit();
	}

	public int hashCode ()
	{
		try
		{
			return _theAction.hash_transaction();
		}
		catch (Exception e)
		{
			return -1;
		}
	}

	public Uid get_uid ()
	{
		return _theAction.get_uid();
	}

	/**
	 * If this transaction current? Assume we have checked that we are actually
	 * a transaction!
	 * 
	 * If not valid then abort this transaction here. Leave current alone.
	 * 
	 * @message com.arjuna.ats.jts.extensions.atnovalidtx {0} - no transaction!
	 * @message com.arjuna.ats.jts.extensions.atcurrenttxnull {0} - current
	 *          transaction is null!
	 * @message com.arjuna.ats.jts.extensions.atoutofseq {0} - terminated out of
	 *          sequence {1}
	 * @message com.arjuna.ats.jts.extensions.atwillabort Will roll back.
	 *          Current transaction is {0}
	 * @message com.arjuna.ats.jts.extensions.atcannotabort {0} - cannot
	 *          rollback {1}
	 * @message com.arjuna.ats.jts.extensions.atgenerror {0} caught unexpected
	 *          exception {1}
	 */

	protected final boolean validTransaction ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::validTransaction called for "
					+ _theAction);
		}

		/*
		 * If we get here then _theAction is not null.
		 */

		CurrentImple current = OTSImpleManager.current();
		boolean valid = false;

		try
		{
			ControlWrapper currentTransaction = current.getControlWrapper();

			if (currentTransaction == null)
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.atnovalidtx", new Object[]
					{ "AtomicTransaction.validTransaction" });
				}

				return false;
			}

			valid = _theAction.equals(currentTransaction);

			if (!valid)
			{
				String transactionName = get_transaction_name();
				String currentTransactionName = currentTransaction.get_transaction_name();

				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.atoutofseq", new Object[]
					{ "AtomicTransaction", transactionName });

					jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.atwillabort", new Object[]
					{ currentTransactionName });
				}

				try
				{
					_theAction.rollback();
				}
				catch (Exception ex)
				{
					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.atcannotabort", new Object[]
						{ "AtomicTransaction", transactionName });
					}
				}
			}
		}
		catch (Exception e)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.atgenerror", new Object[]
				{ "AtomicTransaction", e });
			}
		}

		return valid;
	}

	protected AtomicTransaction (ControlWrapper tx)
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "AtomicTransaction::AtomicTransaction ()");
		}

		_theAction = tx;
		_theStatus = getStatus();

		/*
		 * Once a transaction is created there is no way to get its timeout. So,
		 * we use the timeout associated with the current thread, since that is
		 * most likely to be the right value.
		 */

		_timeout = get_timeout();
	}

	protected final org.omg.CosTransactions.Status getStatus ()
	{
		if (_theStatus != null)
		{
			switch (_theStatus.value())
			{
			case Status._StatusRolledBack:
			case Status._StatusCommitted:
			case Status._StatusNoTransaction:
				return _theStatus;
			default:
				break;
			}
		}

		/*
		 * It shouldn't be possible for _theAction to be null and for the status
		 * to be unset. If it is something went wrong!!
		 */
		
		org.omg.CosTransactions.Status stat = org.omg.CosTransactions.Status.StatusUnknown;

		if (_theAction != null)
		{
			stat = _theAction.get_status();
		}

		return stat;
	}

	protected ControlWrapper _theAction;
	protected org.omg.CosTransactions.Status _theStatus;
	protected int _timeout;

}
