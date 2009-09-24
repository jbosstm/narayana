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
 * $Id: ArjunaTransactionImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.coordinator;

import com.arjuna.ats.jts.extensions.Arjuna;
import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.utils.Utility;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.internal.jts.recovery.RecoveryCreator;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.utils.Helper;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.resources.SynchronizationRecord;
import com.arjuna.ats.internal.jts.resources.ResourceRecord;
import com.arjuna.ats.internal.jts.resources.ExtendedResourceRecord;
import com.arjuna.ats.internal.jts.coordinator.CheckedActions;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;

import org.omg.CosTransactions.*;

import com.arjuna.ArjunaOTS.*;

import org.omg.CORBA.CompletionStatus;

import java.util.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

/**
 * OTS implementation class.
 *
 * Implements both the Coordinator & Terminator interfaces of OTS as a single
 * class.
 *
 * Note, because Java does not support multiple inheritance we must make use of
 * the tie facility (uuuggghhhh!!!!)
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ArjunaTransactionImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 *
 * @message com.arjuna.ats.internal.jts.orbspecific.coordinator.generror {0}
 *          caught exception: {1}
 * @message com.arjuna.ats.internal.jts.orbspecific.coordinator.rbofail {0}
 *          attempt to mark transaction {1} as rollback only threw: {2}
 */

public class ArjunaTransactionImple extends
		com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator implements
		com.arjuna.ArjunaOTS.ArjunaTransactionOperations
{

	public ArjunaTransactionImple (Control myParent)
	{
		this(myParent, null);
	}

	public ArjunaTransactionImple (Control myParent, ArjunaTransactionImple parent)
	{
		super();

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple Begin for < "
					+ get_uid()
					+ " , "
					+ ((parent != null) ? parent.get_uid() : Uid.nullUid())
					+ " >");
		}

		parentTransaction = parent;
		controlHandle = null;
		parentHandle = myParent;
		currentStatus = org.omg.CosTransactions.Status.StatusUnknown;

		rootAction = null;
		_synchs = null;

		super.Begin(parent);

		/*
		 * Add uid of this action to parent.
		 */

		if (parent != null)
			parent.addChildAction(this);

		currentStatus = determineStatus(this);
		rootAction = this;

		/*
		 * Do this once to avoid overhead.
		 */

		hashCode = get_uid().hashCode();

		if (parent != null)
		{
			while ((rootAction.parent()) != null)
				rootAction = rootAction.parent();

			topLevelHashCode = rootAction.get_uid().hashCode();
		}
		else
			topLevelHashCode = hashCode;

		if (ArjunaTransactionImple._checkedTransactions)
		{
			/*
			 * Fully checked transactions only allow the thread which began the
			 * transaction to terminate it. We get the id of the beginning
			 * thread here.
			 *
			 * The spec. says nothing about crash recovery, so we better assume
			 * it can always complete a transaction!
			 *
			 * If the creating thread dies before terminating the transaction
			 * then we have a problem. Requires change to Thread class to abort
			 * outstanding transactions in this case.
			 *
			 * Also, transaction timeouts cause abortion of the transaction by a
			 * *different* thread! This must work even in the presence of
			 * checked transactions!
			 */

			transactionCreator = Thread.currentThread();
		}
		else
			transactionCreator = null;

		CheckedAction ca = CheckedActions.get();

		if (ca != null)
		{
			super.setCheckedAction(ca);
			ca = null;
		}
	}

	public ArjunaTransactionImple (Uid actUid, Control myParent)
	{
		this(actUid, myParent, null);
	}

	public ArjunaTransactionImple (Uid actUid, Control myParent, ArjunaTransactionImple parent)
	{
		super(actUid);

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple Begin for < "
					+ get_uid()
					+ " , "
					+ ((parent != null) ? parent.get_uid() : Uid.nullUid())
					+ " >");
		}

		parentTransaction = parent;
		controlHandle = null;
		parentHandle = myParent;
		currentStatus = org.omg.CosTransactions.Status.StatusUnknown;

		rootAction = null;
		_synchs = null;

		super.Begin(parent);

		/*
		 * Add uid of this action to parent.
		 */

		if (parent != null)
			parent.addChildAction(this);

		currentStatus = determineStatus(this);
		rootAction = this;

		hashCode = get_uid().hashCode();

		if (parent != null)
		{
			while ((rootAction.parent()) != null)
				rootAction = rootAction.parent();

			topLevelHashCode = rootAction.get_uid().hashCode();
		}
		else
			topLevelHashCode = hashCode;

		if (ArjunaTransactionImple._checkedTransactions)
		{
			/*
			 * Fully checked transactions only allow the thread which began the
			 * transaction to terminate it. We get the id of the beginning
			 * thread here.
			 */

			transactionCreator = Thread.currentThread();
		}
		else
			transactionCreator = null;

		CheckedAction ca = CheckedActions.get();

		if (ca != null)
		{
			super.setCheckedAction(ca);
			ca = null;
		}
	}

	/*
	 * Memory management is much better in Java, so we don't have the problem of
	 * the Control referencing the transaction and vice versa.
	 */

	/**
	 * @message com.arjuna.ats.internal.jts.orbspecific.coordinator.zsync {0} -
	 *          none zero Synchronization list!
	 */

	public void finalize ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple.finalize - called for < "
					+ get_uid() + " >");
		}

		if (_synchs != null)
		{
			// should not happen if the transaction has terminated

			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.szync", new Object[]
				{ "ArjunaTransactionImple.finalize()" });
			}

			// "delete" list anyway, but don't do anything with list elements

			_synchs = null;
		}

		controlHandle = null;

		super.finalize();
	}

	public final synchronized ControlImple getControlHandle ()
	{
		return controlHandle;
	}

	public final synchronized void setControlHandle (ControlImple handle)
	{
		controlHandle = handle;
	}

	/**
	 * If the transaction has already been committed (by another thread, for
	 * example) then we do nothing - could throw TransactionRequired or
	 * INVALID_TRANSACTION. However, if it was rolledback then we throw
	 * TRANSACTION_ROLLEDBACK. Seems like an inconsistency.
	 *
	 * report_heuristics is ignored if we are a subtransaction.
	 */

	public void commit (boolean report_heuristics) throws HeuristicMixed,
			HeuristicHazard, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::commit for "
					+ get_uid());
		}

		if (ArjunaTransactionImple._checkedTransactions && !checkAccess())
		{
			throw new NO_PERMISSION(0, CompletionStatus.COMPLETED_NO);
		}

		int outcome = super.status();

		if ((outcome == ActionStatus.RUNNING)
				|| (outcome == ActionStatus.ABORT_ONLY)) // have we already been
														 // committed?
		{
			try
			{
				if (_synchs != null)
                {
					if(outcome == ActionStatus.RUNNING ||
                            (outcome == ActionStatus.ABORT_ONLY && TxControl.isBeforeCompletionWhenRollbackOnly()))
                    {
                        doBeforeCompletion();
                    }
                }
			}
			catch (Exception e)
			{
				/*
				 * Don't do anything, since we will have marked the transaction
				 * as rollback only.
				 */
			}

			/*
			 * Remove the uid of this action from the parent.
			 */

			if (parentTransaction != null)
			{
				parentTransaction.removeChildAction(this);
			}

			outcome = super.End(report_heuristics);

			try
			{
				if (_synchs != null)
				{
					currentStatus = determineStatus(this);

					doAfterCompletion(currentStatus);

					_synchs = null;
				}
			}
			catch (Exception e)
			{
			}

			destroyAction();
		}
		else
		{
			/*
			 * Differentiate between us committing the transaction and some
			 * other thread doing it.
			 */

			throw new INVALID_TRANSACTION(0, CompletionStatus.COMPLETED_NO);
		}

		switch (outcome)
		{
		case ActionStatus.COMMITTED:
		case ActionStatus.H_COMMIT:
		case ActionStatus.COMMITTING: // in case asynchronous commit!
			return;
		case ActionStatus.ABORTED:
		case ActionStatus.ABORTING:  // in case of asynchronous abort!
		case ActionStatus.H_ROLLBACK:
			throw new TRANSACTION_ROLLEDBACK(ExceptionCodes.FAILED_TO_COMMIT,
					CompletionStatus.COMPLETED_NO);
		case ActionStatus.H_MIXED:
		    if (report_heuristics)
			throw new HeuristicMixed();		    
		    break;
		case ActionStatus.H_HAZARD:
		default:
		    if (report_heuristics)
		        throw new HeuristicHazard();		    
		    break;
		}
	}

	public void rollback () throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::rollback for "
					+ get_uid());
		}

		if (ArjunaTransactionImple._checkedTransactions && !checkAccess())
		{
			throw new NO_PERMISSION(0, CompletionStatus.COMPLETED_NO);
		}

		int status = super.status();

		if ((status == ActionStatus.RUNNING)
				|| (status == ActionStatus.ABORT_ONLY)) // already aborted?
		{

            if (ArjunaTransactionImple._syncOn)
            {
                if(TxControl.isBeforeCompletionWhenRollbackOnly())
                {
                    try
                    {
                        if (_synchs != null)
                            doBeforeCompletion();
                    }
                    catch (Exception e)
                    {
                        /*
                           * Don't do anything - we're about to rollback anyway!
                           */
                    }
                }
            }
            else
            {
                /*
                     * If we have any synchronizations delete them now. Can only be
                     * a top-level action.
                     */

                _synchs = null;
            }

			/*
			 * Remove uid of this action from parent even if remote.
			 */

			if (parentTransaction != null)
			{
				parentTransaction.removeChildAction(this);
			}

			super.Abort();

			if (ArjunaTransactionImple._syncOn)
			{
				try
				{
					if (_synchs != null)
					{
						currentStatus = determineStatus(this);

						doAfterCompletion(currentStatus);
					}
				}
				catch (Exception e)
				{
				}
			}

			destroyAction();

			status = super.status();
		}
		else
		{
			/*
			 * Differentiate between us ending the transaction and some other
			 * thread doing it.
			 */

			throw new INVALID_TRANSACTION(0, CompletionStatus.COMPLETED_NO); // means
																			 // transaction
																			 // already
																			 // terminated.
		}

		switch (status)
		{
		case ActionStatus.ABORTING:
		case ActionStatus.ABORTED:
		case ActionStatus.H_ROLLBACK:
			/*
			 * If the transaction has already rolledback then silently ignore
			 * the multiple rollback attempts.
			 */
			return;
		case ActionStatus.PREPARING: // shouldn't be able to get heuristics or
									 // any of these!
		case ActionStatus.PREPARED:
		case ActionStatus.COMMITTING:
		case ActionStatus.COMMITTED:
		case ActionStatus.H_COMMIT:
		case ActionStatus.H_MIXED:
		case ActionStatus.H_HAZARD:
			throw new INVALID_TRANSACTION(0, CompletionStatus.COMPLETED_NO); // means
																			 // transaction
																			 // already
																			 // terminated.
		case ActionStatus.INVALID:
		case ActionStatus.CLEANUP:
			throw new UNKNOWN(ExceptionCodes.UNKNOWN_EXCEPTION,
					CompletionStatus.COMPLETED_MAYBE);
		}
	}

	public org.omg.CosTransactions.Status get_status () throws SystemException
	{
		Status s = determineStatus(this);

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::get_status for "
					+ get_uid() + " returning " + Utility.stringStatus(s));
		}

		return s;
	}

	public org.omg.CosTransactions.Status get_parent_status ()
			throws SystemException
	{
		if (parentTransaction != null)
			return parentTransaction.get_status();
		else
			return get_status();
	}

	public org.omg.CosTransactions.Status get_top_level_status ()
			throws SystemException
	{
		if (rootAction != null)
			return determineStatus(rootAction);
		else
			return get_status();
	}

	public boolean is_same_transaction (Coordinator tc) throws SystemException
	{
		if (tc == null)
			return false;

		/*
		 * Cut down the amount of work we need to do. Hash values for the same
		 * transaction must be the same!
		 */

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::is_same_transaction comparing hash codes: < "
					+ tc.hash_transaction() + ", " + hash_transaction() + " >");
		}

		if (tc.hash_transaction() != hash_transaction())
			return false;

		boolean result = false;

		try
		{
			UidCoordinator ptr = com.arjuna.ArjunaOTS.UidCoordinatorHelper.narrow(tc);

			if (ptr != null)
			{
				/*
				 * Must be an Arjuna coordinator.
				 */

				String myUid = uid();
				String compareUid = ptr.uid();

				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::is_same_transaction comparing uids < "
							+ compareUid + ", " + myUid + " >");
				}

				if (myUid.compareTo(compareUid) == 0)
					result = true;

				myUid = null;
				compareUid = null;

				ptr = null;
			}
			else
				throw new BAD_PARAM();
		}
		catch (SystemException e)
		{
			/*
			 * Narrow failed, so can't be an Arjuna Uid. Therefore, the answer
			 * must be false.
			 */
		}

		return result;
	}

	public boolean is_related_transaction (Coordinator tc)
			throws SystemException
	{
		if (tc == null)
			return false;

		boolean result = false;

		try
		{
			UidCoordinator ptr = com.arjuna.ArjunaOTS.UidCoordinatorHelper.narrow(tc);

			if (ptr != null)
			{
				/*
				 * Must be an Arjuna coordinator.
				 */

				/*
				 * If they have the same parent, then they must be related.
				 */

				String myTLUid = topLevelUid();
				String compareTLUid = ptr.topLevelUid();

				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::is_related_transaction comparing uids < "
							+ compareTLUid + ", " + myTLUid + " >");
				}

				if (myTLUid.compareTo(compareTLUid) == 0)
					result = true;

				myTLUid = null;
				compareTLUid = null;

				ptr = null;
			}
			else
				throw new BAD_PARAM();
		}
		catch (SystemException e)
		{
			/*
			 * Narrow failed, so can't be an Arjuna Uid. Therefore, the answer
			 * must be false.
			 */
		}

		return result;
	}

	/**
	 * Is this transaction an ancestor of tc?
	 */

	public boolean is_ancestor_transaction (Coordinator tc)
			throws SystemException
	{
		if (tc == null)
			return false;

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::is_ancestor_transaction ()");
		}

		if (is_same_transaction(tc))
			return true;
		else
		{
			/*
			 * Are we related?
			 */

			if (is_related_transaction(tc))
			{
				if (is_descendant_transaction(tc))
					return false;
				else
					return true;
			}
			else
				return false;
		}
	}

	/**
	 * Is this transaction a descendant of tc?
	 */

	public boolean is_descendant_transaction (Coordinator tc)
			throws SystemException
	{
		if (tc == null)
			return false;

		try
		{
			UidCoordinator ptr = com.arjuna.ArjunaOTS.UidCoordinatorHelper.narrow(tc);

			if (ptr != null)
			{
				/*
				 * Must be an Arjuna coordinator.
				 */

				Uid lookingFor = new Uid(ptr.uid());
				BasicAction lookingAt = this;

				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::is_descendant_transaction - looking for "
							+ lookingFor);
				}

				while (lookingAt != null)
				{
					if (jtsLogger.logger.isDebugEnabled())
					{
						jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::is_descendant_transaction - looking for "
								+ lookingAt.get_uid());
					}

					if (lookingAt.get_uid().equals(lookingFor))
						return true;
					else
						lookingAt = lookingAt.parent();
				}

				ptr = null;
			}
			else
				throw new BAD_PARAM();
		}
		catch (SystemException e)
		{
			/*
			 * Narrow failed, so can't be an Arjuna Uid. Therefore, the answer
			 * must be false.
			 */
		}

		return false;
	}

	public boolean is_top_level_transaction () throws SystemException
	{
		return (this == rootAction);
	}

	public int hash_transaction () throws SystemException
	{
		return hashCode;
	}

	public int hash_top_level_tran () throws SystemException
	{
		return topLevelHashCode;
	}

	/**
	 * Resources are only registered with the current transaction, whereas
	 * subtransaction aware resources are registered with their parents when the
	 * current transaction ends.
	 *
	 * @message com.arjuna.ats.internal.jts.orbspecific.coordinator.rccreate
	 *          Creation of RecoveryCoordinator for {0} threw: {1}
	 * @message com.arjuna.ats.internal.jts.orbspecific.coordinator.rcnotcreated
	 *          not created!
	 */

	public RecoveryCoordinator register_resource (Resource r)
			throws SystemException, Inactive
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_resource ( "
					+ r + " ) - called for " + get_uid());
		}

		if (r == null)
			throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);

		currentStatus = determineStatus(this);

		if (currentStatus != Status.StatusActive)
		{
			if (jtsLogger.logger.isDebugEnabled())
			{
				jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_resource - transaction not active: "
						+ Utility.stringStatus(currentStatus));
			}

			if (currentStatus == Status.StatusMarkedRollback)
			{
				throw new TRANSACTION_ROLLEDBACK(
						ExceptionCodes.MARKED_ROLLEDBACK,
						CompletionStatus.COMPLETED_NO);
			}
			else
				throw new Inactive();
		}

		AbstractRecord corbaRec = null;
		BasicAction registerIn = this;

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple "
					+ get_uid() + " ::register_resource: ");
		}

		//
		// Creation of recovery coordinator (DBI)
		//

		//
		// Pack the params:
		// [0] = Transaction* this
		//

		int index = 0;
		Object params[] = new Object[10];
		params[index++] = this;

		RecoveryCoordinator recoveryCoordinator = null;
		Uid recoveryCoordinatorUid = null;

		/*
		 * A RecoveryCoordinator can be null but only if the implementation
		 * throws NO_IMPLEMENT. If it tries to return null then that is
		 * considered an error and we will roll back the transaction.
		 */

		try
		{
			recoveryCoordinator = RecoveryCreator.createRecoveryCoordinator(r, params);

			if (recoveryCoordinator == null)
				throw new BAD_OPERATION(
						"RecoveryCoordinator "
								+ jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.coordinator.rcnotcreated"));
		}
		catch (NO_IMPLEMENT ex)
		{
			/*
			 * This is legal, and is meant to show that this ORB or
			 * configuration simply doesn't support crash recovery.
			 */

			recoveryCoordinator = null;
		}
		catch (SystemException e)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.rccreate", new Object[]
				{ get_uid(), e });
			}

			/*
			 * Set transaction to rollback only and re-throw exception.
			 */

			try
			{
				rollback_only();
			}
			catch (Inactive ex1)
			{
			}
			catch (SystemException ex2)
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.rbofail", new Object[]
					{ "ArjunaTransactionImple.register_resource", get_uid(), ex2 });
				}

				throw ex2;
			}

			throw e;
		}

		if (recoveryCoordinator != null)
		{
			//
			// We got a RecoveryCoordinator, so unpack the other return values:
			// [0] = RecoveryCoordinator Uid*
			//

			index = 0;
			recoveryCoordinatorUid = (Uid) params[index++];
		}
		else
		{
			//
			// We didn't get a RecoveryCoordinator, so we don't assume that
			// the other return values have been populated.
			//

			recoveryCoordinatorUid = Uid.nullUid();
		}

		try
		{
			SubtransactionAwareResource staResource = org.omg.CosTransactions.SubtransactionAwareResourceHelper.narrow(r);

			/*
			 * Some Orbs (e.g., Orbix) throw BAD_PARAM is the object in X.narrow
			 * is not of type X, whereas others (e.g., OrbPlus) simply return
			 * NULL!
			 */

			if (staResource != null)
			{
				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_resource for "
							+ get_uid()
							+ " - subtransaction aware resource: YES");
				}

				/*
				 * If here the narrow was ok so we have a subtran aware
				 * resource.
				 */

				Coordinator coord = null;

				if (parentHandle != null)
				{
					/*
					 * If we are a SubTranResource then we get registered with
					 * the current transaction and its parents upon completion.
					 * The first parameter to the record indicates whether we
					 * should be propagated (registered) with the parent
					 * transaction.
					 */

					coord = parentHandle.get_coordinator();
				}

				corbaRec = createOTSRecord(true, r, coord, recoveryCoordinatorUid);

				coord = null;
				staResource = null;
			}
			else
				throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);
		}
		catch (BAD_PARAM ex)
		{
			if (jtsLogger.logger.isDebugEnabled())
			{
				jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_resource for "
						+ get_uid() + " - subtransaction aware resource: NO");
			}

			/* narrow failed must be a plain resource */

			/*
			 * Register with current transaction, but we only receive
			 * invocations at top-level.
			 */

			if (jtsLogger.logger.isDebugEnabled())
			{
				jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple "
						+ get_uid()
						+ " ::register_resource: Simple resource - " + ex);
			}

			corbaRec = createOTSRecord(true, r, null, recoveryCoordinatorUid);
		}
		catch (Unavailable e1)
		{
			throw new Inactive();
		}
		catch (SystemException e2)
		{
			if (jtsLogger.logger.isDebugEnabled())
			{
				jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_resource for "
						+ get_uid() + " : catch (SystemException) - " + e2);
			}

			throw e2;
		}
		catch (Exception e3)
		{
			if (jtsLogger.logger.isDebugEnabled())
			{
				jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_resource for "
						+ get_uid() + " : catch (...) - " + e3);
			}

			/*
			 * Cannot just rethrow exception, so throw UNKNOWN.
			 */

			throw new UNKNOWN(e3.toString(), ExceptionCodes.UNKNOWN_EXCEPTION,
					CompletionStatus.COMPLETED_NO);
		}

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_resource for "
					+ get_uid() + " : try end");
		}

		if (registerIn.add(corbaRec) != AddOutcome.AR_ADDED)
		{
			corbaRec = null;

			throw new INVALID_TRANSACTION(ExceptionCodes.ADD_FAILED,
					CompletionStatus.COMPLETED_NO);
		}
		else
		{
			if (jtsLogger.logger.isDebugEnabled())
			{
				jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_resource for "
						+ get_uid() + " : resource registered");
			}
		}

		return recoveryCoordinator;
	}

	/**
	 * Do not propagate the resource to the parent.
	 */

	public void register_subtran_aware (SubtransactionAwareResource r)
			throws Inactive, NotSubtransaction, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_subtran_aware called for "
					+ get_uid());
		}

		if (r == null)
			throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);

		currentStatus = determineStatus(this);

		if (currentStatus != Status.StatusActive)
		{
			if (currentStatus == Status.StatusMarkedRollback)
			{
				throw new TRANSACTION_ROLLEDBACK(
						ExceptionCodes.MARKED_ROLLEDBACK,
						CompletionStatus.COMPLETED_NO);
			}
			else
				throw new Inactive();
		}

		if (this == rootAction)
		{
			if (jtsLogger.logger.isDebugEnabled())
			{
				jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_subtran_aware called for "
						+ get_uid() + " : not a subtransaction!");
			}

			throw new NotSubtransaction();
		}
		else
		{
			Coordinator coord = null;
			AbstractRecord corbaRec = null;

			try
			{
				coord = parentHandle.get_coordinator();
				corbaRec = createOTSRecord(false, r, coord);
			}
			catch (Unavailable ex)
			{
				throw new UNKNOWN(ExceptionCodes.INACTIVE_TRANSACTION,
						CompletionStatus.COMPLETED_NO); // what else to raise?
			}

			coord = null;

			/*
			 * Throw some exception here?
			 */

			if (add(corbaRec) != AddOutcome.AR_ADDED)
			{
				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_subtran_aware called for "
							+ get_uid() + " : could not add.");
				}

				corbaRec = null;
				throw new Inactive(); // what else to raise??
			}
		}

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_subtran_aware called for "
					+ get_uid() + " : subtran_aware_resource registered");
		}
	}

	public void rollback_only () throws SystemException, Inactive
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::rollback_only - called for "
					+ get_uid());
		}

		if (determineStatus(this) != Status.StatusPrepared)
		{
			if (!preventCommit())
			{
				throw new INVALID_TRANSACTION(
						ExceptionCodes.INACTIVE_TRANSACTION,
						CompletionStatus.COMPLETED_NO);
			}
		}
		else
			throw new Inactive();
	}

	/**
	 * To be used for debugging purposes only.
	 */

	public String get_transaction_name () throws SystemException
	{
		return get_uid().stringForm();
	}

	public Control create_subtransaction () throws SystemException,
			SubtransactionsUnavailable, Inactive
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::create_subtransaction - called for "
					+ get_uid());
		}

		if (determineStatus(this) != Status.StatusActive)
			throw new Inactive();
		else
		{
			if (!_subtran)
				throw new SubtransactionsUnavailable();
			else
			{
				if (controlHandle == null)
					throw new Inactive();
				else
					return TransactionFactoryImple.create_subtransaction(controlHandle.getControl(), this);
			}
		}
	}

	/**
	 * The spec states that a synchronization is registered with a single
	 * top-level action only. However, if this is a nested transaction there is
	 * no appropriate exception to raise. So, we raise
	 * SynchronizationUnavailable. We could simply get our parent and register
	 * the synchronization with it, but this may not be what the user expects.
	 * If it is, then the user can get the parent and do it directly!
	 */

	// why not use SynchronizationRecords?
	public void register_synchronization (Synchronization sync)
			throws Inactive, SynchronizationUnavailable, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_synchronization - called for "
					+ get_uid());
		}

		if (sync == null)
			throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);

		if (!is_top_level_transaction()) // are we a top-level transaction?
		{
			if (jtsLogger.logger.isDebugEnabled())
			{
				jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_synchronization - "
						+ get_uid() + " is not a top-level transaction!");
			}

			throw new SynchronizationUnavailable();
		}
		else
		{
			currentStatus = determineStatus(this);

			if (currentStatus == Status.StatusActive) // is transaction still
													  // running?
			{
				synchronized (this)
				{
					if (_synchs == null)
                    {
                        // Synchronizations should be stored (or at least iterated) in their natural order
						_synchs = new TreeSet();
                    }
				}

                SynchronizationRecord otsSync;

                if(sync._is_a(JTAInterposedSynchronizationHelper.id()))
                {
                    otsSync = new SynchronizationRecord(sync, true);
                }
                else
                {
                    otsSync = new SynchronizationRecord(sync);
                }

                // disallow addition of Synchronizations that would appear
				// earlier in sequence than any that has already been called
				// during the pre-commmit phase. This is required for
				// JTA 1.1 Synchronization ordering behaviour
				if(_currentRecord != null) {
					Comparable c = (Comparable)otsSync;
					if(c.compareTo(_currentRecord) != 1) {
						throw new UNKNOWN(ExceptionCodes.ADD_FAILED, CompletionStatus.COMPLETED_NO);
					}
				}

                if (!_synchs.add(otsSync))
				{
					otsSync = null;
					throw new UNKNOWN(ExceptionCodes.ADD_FAILED,
							CompletionStatus.COMPLETED_NO); // what else to
															// raise?
				}
			}
			else
			{
				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::register_synchronization - "
							+ get_uid()
							+ " is not active: "
							+ Utility.stringStatus(currentStatus));
				}

				if (currentStatus == Status.StatusMarkedRollback)
				{
					throw new TRANSACTION_ROLLEDBACK(
							ExceptionCodes.MARKED_ROLLEDBACK,
							CompletionStatus.COMPLETED_NO);
				}
				else
					throw new Inactive();
			}
		}
	}

	public PropagationContext get_txcontext () throws Unavailable,
			SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::get_txcontext - called for "
					+ get_uid());
		}

		/*
		 * Throw an exception if we are not active.
		 */

		currentStatus = determineStatus(this);

		if ((currentStatus != Status.StatusActive)
				&& (currentStatus != Status.StatusMarkedRollback))
		{
			throw new Unavailable();
		}
		else
		{
			try
			{
				return propagationContext();
			}
			catch (Exception e)
			{
				throw new Unavailable();
			}
		}
	}

	/*
	 * Some Arjuna specific methods.
	 */

	/**
	 * We use these to determine relationships between transactions. Using the
	 * hash function is not sufficient, since a hash value is not guaranteed to
	 * be unique.
	 */

	public String uid () throws SystemException
	{
		return get_uid().stringForm();
	}

	public String topLevelUid () throws SystemException
	{
		if (rootAction != null)
			return rootAction.get_uid().stringForm();
		else
			return null;
	}

	public String type ()
	{
		return ArjunaTransactionImple.typeName();
	}

	public static final int interpositionType ()
	{
		return _ipType;
	}

	public static String typeName ()
	{
		return "/StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple";
	}

	public String toString ()
	{
		return "ArjunaTransactionImple < " + get_uid() + " >";
	}

	public boolean equals (Object o)
	{
		if (o instanceof ArjunaTransactionImple)
		{
			ArjunaTransactionImple tx = (ArjunaTransactionImple) o;

			if (tx == this)
				return true;
			else
				return tx.get_uid().equals(get_uid());
		}
		else
			return false;
	}

	public boolean forgetHeuristics ()
	{
		return super.forgetHeuristics();
	}

	/**
	 * For crash recovery purposes only.
	 */

	protected ArjunaTransactionImple (Uid actUid)
	{
		super(actUid);

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::ArjunaTransactionImple ( "
					+ actUid + " )");
		}

		parentTransaction = null;
		controlHandle = null;
		parentHandle = null;
		currentStatus = org.omg.CosTransactions.Status.StatusUnknown;

		transactionCreator = null;

		rootAction = null;
		_synchs = null;

		/*
		 * Leave activation of transaction up to caller. Transaction status will
		 * remain as Unknown until then.
		 */

		rootAction = this;

		/*
		 * Do this once to avoid overhead.
		 */

		hashCode = actUid.hashCode();
		topLevelHashCode = hashCode; // this must be a top-level transaction

		/*
		 * Don't bother with checked transactions for recovery.
		 */
	}

	protected void doBeforeCompletion () throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::doBeforeCompletion for "
					+ get_uid());
		}

		boolean problem = false;
		SystemException exp = null;

		/*
		 * If we have a synchronization list then we must be top-level.
		 */
        if (_synchs != null)
		{
			boolean doSuspend = false;
			ControlWrapper cw = null;

			try
			{
				/*
				 * Make sure that this transaction is active on the thread
				 * before we invoke any Synchronizations. They are
				 * TransactionalObjects and must have the context flowed to
				 * them.
				 */

				try
				{
					//		    cw = OTSImpleManager.systemCurrent().getControlWrapper();

					cw = OTSImpleManager.current().getControlWrapper();

					/*
					 * If there's no transaction incoming, then use the one that
					 * we got at creation time.
					 */

					if ((cw == null) || (!controlHandle.equals(cw.getImple())))
					{
						//			OTSImpleManager.systemCurrent().resumeImple(controlHandle);

						OTSImpleManager.current().resumeImple(controlHandle);

						doSuspend = true;
					}
				}
				catch (Exception ex)
				{
					/*
					 * It should be OK to continue with the invocations even if
					 * we couldn't resume, because a Synchronization is only
					 * supposed to be associated with a single transaction. So,
					 * it should be able to infer the transaction.
					 */
				}

               /*
                * Since Synchronizations may add register other Synchronizations, we can't simply
                * iterate the collection. Instead we work from an ordered copy, which we periodically
                * check for freshness. The addSynchronization method uses _currentRecord to disallow
                * adding records in the part of the array we have already traversed, thus all
                * Synchronization will be called and the (jta only) rules on ordering of interposed
                * Synchronization will be respected.
                */
               int lastIndexProcessed = -1;
               SynchronizationRecord[] copiedSynchs = (SynchronizationRecord[])_synchs.toArray(new SynchronizationRecord[] {});

               while( (lastIndexProcessed < _synchs.size()-1) && !problem) {

                   // if new Synchronization have been registered, refresh our copy of the collection:
                   if(copiedSynchs.length != _synchs.size()) {
                       copiedSynchs = (SynchronizationRecord[])_synchs.toArray(new SynchronizationRecord[] {});
                   }

                   lastIndexProcessed = lastIndexProcessed+1;
                   _currentRecord = copiedSynchs[lastIndexProcessed];

					Synchronization c = _currentRecord.contents();
					c.before_completion();
				}
			}
			catch (SystemException e)
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.generror", new Object[]
					{ "ArjunaTransactionImple.doBeforeCompletion", e });
				}

				if (!problem)
				{
					exp = e;

					problem = true;

					/*
					 * Mark as rollback_only, so when we try to commit it will
					 * fail.
					 */

					try
					{
						rollback_only();
					}
					catch (Inactive ex)
					{
						/*
						 * This should not happen. If it does, continue with
						 * commit to tidy-up.
						 */

						if (jtsLogger.loggerI18N.isWarnEnabled())
						{
							jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.rbofail", new Object[]
							{ "ArjunaTransactionImple.doBeforeCompletion", get_uid(), ex });
						}
					}
				}
			}
			finally
			{
				if (doSuspend)
				{
					try
					{
						//			OTSImpleManager.systemCurrent().resumeWrapper(cw);

						if (cw != null)
							OTSImpleManager.current().resumeWrapper(cw);
						else
							OTSImpleManager.current().suspend();
					}
					catch (Exception ex)
					{
					}

					//		    OTSImpleManager.systemCurrent().suspend();
				}
			}
		}

		if (problem)
		{
			if (exp != null)
				throw exp;
			else
				throw new UNKNOWN(ExceptionCodes.SYNCHRONIZATION_EXCEPTION,
						CompletionStatus.COMPLETED_NO);
		}
	}

	/**
	 * @message com.arjuna.ats.internal.jts.orbspecific.coordinator.txrun {0}
	 *          called on still running transaction!
	 */

	protected void doAfterCompletion (org.omg.CosTransactions.Status myStatus)
			throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::doAfterCompletion for "
					+ get_uid());
		}

		if (myStatus == Status.StatusActive)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.txrun", new Object[]
				{ "ArjunaTransactionImple.doAfterCompletion" });
			}

			return;
		}

		boolean problem = false;
		SystemException exp = null;

		if (_synchs != null)
		{
			ControlWrapper cw = null;
			boolean doSuspend = false;

			try
			{
				//		cw = OTSImpleManager.systemCurrent().getControlWrapper();

				cw = OTSImpleManager.current().getControlWrapper();

				/*
				 * If there isn't a transaction context shipped, then use the
				 * one we had during creation.
				 */

				if ((cw == null) || (!controlHandle.equals(cw.getImple())))
				{
					//		    OTSImpleManager.systemCurrent().resumeImple(controlHandle);

					OTSImpleManager.current().resumeImple(controlHandle);

					doSuspend = true;
				}
			}
			catch (Exception ex)
			{
				/*
				 * It should still be OK to make the call without a context
				 * because a Synchronization can only be associated with a
				 * single transaction.
				 */

				problem = true;
			}

			/*
			 * Regardless of failures, we must tell all synchronizations what
			 * happened.
			 */

			// afterCompletions should run in reverse order compared to beforeCompletions
			Stack stack = new Stack();
			Iterator iterator = _synchs.iterator();
			while(iterator.hasNext()) {
				stack.push(iterator.next());
			}

			iterator = stack.iterator();

			/*
			 * Regardless of failures, we must tell all synchronizations what
			 * happened.
			 */
			while(!stack.isEmpty())
			{
				SynchronizationRecord value = (SynchronizationRecord)stack.pop();
				Synchronization c = value.contents();

				try
				{
					c.after_completion(myStatus);
				}
				catch (SystemException e)
				{
					if (jtsLogger.logger.isDebugEnabled())
					{
						jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple.doAfterCompletion - caught exception "
								+ e);
					}

					problem = true;

					/*
					 * Remember the first exception we get, because it may well
					 * be the only one. In which case we can return it, rather
					 * than UNKNOWN.
					 */

					if (exp == null)
						exp = e;
				}
			}

			if (doSuspend)
			{
				try
				{
					//		    OTSImpleManager.systemCurrent().resumeWrapper(cw);

					if (cw != null)
						OTSImpleManager.current().resumeWrapper(cw);
					else
						OTSImpleManager.current().suspend();
				}
				catch (Exception ex)
				{
				}
			}

			_synchs = null;
		}

		if (problem)
		{
			if (exp != null)
				throw exp;
			else
				throw new UNKNOWN(ExceptionCodes.SYNCHRONIZATION_EXCEPTION,
						CompletionStatus.COMPLETED_NO);
		}
	}

	/**
	 * Called by transaction reaper to force rollback. We do not check the id of
	 * the calling thread here, as it will definitely not be the thread which
	 * created this transaction. With checked transactions this is normally not
	 * allowed, so we need some way to circumvent this.
	 */

	final void forceRollback () throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PACKAGE, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::forceRollback for "
					+ get_uid());
		}

		if (super.status() == ActionStatus.RUNNING) // already aborted?
		{
			/*
			 * If we have any synchronizations delete them now. Can only be a
			 * top-level action.
			 */

			if (ArjunaTransactionImple._syncOn)
			{
                if(TxControl.isBeforeCompletionWhenRollbackOnly())
                {
                    try
                    {
                        if (_synchs != null)
                            doBeforeCompletion();
                    }
                    catch (Exception e)
                    {
                        /*
                           * Don't do anything - we're about to rollback anyway!
                           */
                    }
                }
			}
			else
			{
				/*
				 * If we have any synchronizations delete them now. Can only be
				 * a top-level action.
				 */

				_synchs = null;
			}

			/*
			 * Remove uid of this action from parent even if remote.
			 */

			if (parentTransaction != null)
			{
				parentTransaction.removeChildAction(this);
			}

			Abort();

			if (ArjunaTransactionImple._syncOn)
			{
				try
				{
					if (_synchs != null)
					{
						currentStatus = determineStatus(this);

						doAfterCompletion(currentStatus);
					}
				}
				catch (Exception e)
				{
				}
			}

			destroyAction();
		}
	}

	private final org.omg.CosTransactions.Status determineStatus (BasicAction whichAction)
	{
		org.omg.CosTransactions.Status theStatus = org.omg.CosTransactions.Status.StatusUnknown;

		if (whichAction != null)
		{
			switch (whichAction.status())
			{
			case ActionStatus.INVALID: // probably locked, so try again later
				theStatus = Status.StatusUnknown;
				break;
			case ActionStatus.RUNNING:
				theStatus = Status.StatusActive;
				break;
			case ActionStatus.PREPARED:
				theStatus = Status.StatusPrepared;
				break;
			case ActionStatus.COMMITTED:
			case ActionStatus.H_COMMIT:
			case ActionStatus.H_MIXED:
			case ActionStatus.H_HAZARD:
				theStatus = Status.StatusCommitted;
				break;
			case ActionStatus.ABORTED:
			case ActionStatus.H_ROLLBACK:
				theStatus = Status.StatusRolledBack;
				break;
			case ActionStatus.ABORT_ONLY:
				theStatus = Status.StatusMarkedRollback;
				break;
			case ActionStatus.PREPARING:
				theStatus = Status.StatusPreparing;
				break;
			case ActionStatus.COMMITTING:
				theStatus = Status.StatusCommitting;
				break;
			case ActionStatus.ABORTING:
				theStatus = Status.StatusRollingBack;
				break;
			default:
				theStatus = Status.StatusUnknown;
			}
		}
		else
			theStatus = Status.StatusNoTransaction;

		return theStatus;
	}

	/**
	 * @message com.arjuna.ats.internal.jts.orbspecific.coordinator.uidfail {0} -
	 *          could not get unique identifier of object.
	 */

	protected final AbstractRecord createOTSRecord (boolean propagate, Resource resource, Coordinator coord)
	{
		return createOTSRecord(propagate, resource, coord, null);
	}

	protected final AbstractRecord createOTSRecord (boolean propagate, Resource resource, Coordinator coord, Uid recCoordUid)
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::createOTSRecord for "
					+ get_uid());
		}

		/*
		 * If the resource is an ArjunaOTS.OTSAbstractRecord or an
		 * ArjunaOTS.ArjunaSubtranAwareResource then we can do better record
		 * manipulation, and proper nested actions.
		 *
		 * Based on the type of resource we create the right abstract record to
		 * handle it, rather than a single abstract record which switches
		 * protocols internally.
		 */

		ArjunaSubtranAwareResource absRec = null;
		AbstractRecord corbaRec = null;

		if (resource != null)
		{
			try
			{
				absRec = com.arjuna.ArjunaOTS.ArjunaSubtranAwareResourceHelper.narrow(resource);

				if (absRec == null)
					throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);
			}
			catch (Exception e)
			{
				// can't be an ArjunaOTS.ArjunaSubtranAwareResource

				absRec = null;
			}
		}

		if (absRec == null)
		{
			corbaRec = new ResourceRecord(propagate, resource, coord,
					recCoordUid, this);
		}
		else
		{
			Uid u = null;
			OTSAbstractRecord otsRec;

			try
			{
				otsRec = com.arjuna.ArjunaOTS.OTSAbstractRecordHelper.narrow(absRec);

				if (otsRec == null)
					throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);
			}
			catch (Exception e)
			{
				otsRec = null;
			}

			if (otsRec != null)
			{
				try
				{
					u = new Uid(otsRec.uid());
				}
				catch (Exception e)
				{
					u = null;
				}

				if (u == null)
				{
					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.uidfail", new Object[]
						{ "ArjunaTransactionImple.createOTSRecord" });
					}
				}
			}

			if (u == null)
				u = new Uid();

			corbaRec = new ExtendedResourceRecord(propagate, u, absRec, coord,
					recCoordUid, this);

			otsRec = null;
			absRec = null;
			u = null;
		}

		return corbaRec;
	}

	/*
	 * Is the calling thread the one which began this transaction?
	 */

	private final boolean checkAccess ()
	{
		if (Thread.currentThread() == transactionCreator)
			return true;
		else
			return false;
	}

	/*
	 * The caller should delete the context.
	 *
	 * The propagation context is specified on a per client thread basis.
	 * Therefore, at the server side we must maintain a hierarchy for each
	 * thread. However, the server cannot simply tear down this hierarchy
	 * whenever it receives a completely new one from the same thread, since the
	 * OTS lets a thread suspend/resume contexts at will. Potential for memory
	 * leaks in C++ version, but not Java!!
	 *
	 * Currently we assume that the hierarchy will be JBoss transactions so we
	 * can get the parents of transactions. If it is not then we could simply
	 * just call get_txcontext on the control!
	 */

	private final PropagationContext propagationContext () throws Unavailable,
			Inactive, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::propagationContext for "
					+ get_uid());
		}

		String theUid = null;
		Control currentControl = controlHandle.getControl();
		PropagationContext context = new PropagationContext();
		int sequenceThreshold = 1; // most transactions will be top-level
		int sequenceIncrement = 5;

		context.parents = null;

		context.current = new TransIdentity();
		context.implementation_specific_data = ORBManager.getORB().orb().create_any(); // uughh!!

		/*
		 * Some ORBs (e.g., JBroker) don't like to pass round an unused Any,
		 * i.e., one which has only been created and had nothing put in it! So
		 * we have to put something in it!!
		 */

		context.implementation_specific_data.insert_short((short) 0);

		/*
		 * Set up current information. We must leave the timeout for now, since
		 * it only applies to the top-level transaction.
		 */

		try
		{
			context.current.coord = controlHandle.get_coordinator();
			context.timeout = 0; // will reset later!

			/*
			 * Only send the terminator if explicitly asked to. This prevents
			 * anyone other than the creator from terminating a transaction.
			 */

			if (ArjunaTransactionImple._propagateTerminator)
			{
				context.current.term = controlHandle.get_terminator();
			}
			else
				context.current.term = null;
		}
		catch (Exception e)
		{
			return null;
		}

		/*
		 * We send the Uid hierarchy as the otid_t part of the TransIdentity.
		 */

		// the sequence should do the memory management for us.
		theUid = controlHandle.get_uid().stringForm();

		context.current.otid = Utility.uidToOtid(theUid);
		context.current.otid.formatID = ArjunaTransactionImple._ipType;

		int index = 0;

		while (currentControl != null)
		{
			try
			{
				ActionControl control = com.arjuna.ArjunaOTS.ActionControlHelper.narrow(currentControl);

				if (control != null)
				{
					/*
					 * Must be an Arjuna control.
					 */

					currentControl = control.getParentControl();

					if (currentControl != null)
					{
						if (index == 0) // first time
						{
							context.parents = new TransIdentity[sequenceThreshold]; // initial
																					// length
																					// to
																					// avoid
																					// realloc

							for (int ii = 0; ii < sequenceThreshold; ii++)
								context.parents[ii] = null;
						}

						context.parents[index] = new TransIdentity();
						context.parents[index].coord = currentControl.get_coordinator();

						if (ArjunaTransactionImple._propagateTerminator)
							context.parents[index].term = currentControl.get_terminator();
						else
							context.parents[index].term = null;

						/*
						 * Don't bother checking whether narrow works because we
						 * can't cope with mixed transaction types anyway! If we
						 * got here then the root transaction must be an Arjuna
						 * transaction, so the nested transactions *must* also
						 * be JBoss transactions!
						 */

						UidCoordinator uidCoord = Helper.getUidCoordinator(context.parents[index].coord);

						theUid = uidCoord.uid();

						context.parents[index].otid = Utility.uidToOtid(theUid);
						context.parents[index].otid.formatID = ArjunaTransactionImple._ipType;

						theUid = null;
						uidCoord = null;

						index++;

						if (index >= sequenceThreshold)
						{
							sequenceThreshold = index + sequenceIncrement;
							context.parents = resizeHierarchy(context.parents, index
									+ sequenceIncrement);
						}
					}
					else
					{
						/*
						 * Found the top-level transaction, so we can now setup
						 * the timeout value. All transactions will non-zero
						 * timeouts will have been registered with the
						 * transaction reaper.
						 */

						/*
						 * By default we send over the time remaining (in seconds), since that
						 * is what OTS 1.2 requires. For backward compatibility with earlier
						 * versions, there's a configurable option.
						 */

						if (TransactionReaper.transactionReaper() != null)
						{
						    if (_propagateRemainingTimeout)
                            {
                                long timeInMills = TransactionReaper.transactionReaper().getRemainingTimeoutMills(control);
                                context.timeout = (int)(timeInMills/1000L);
                            }
                            else
                            {
                                context.timeout = TransactionReaper.transactionReaper().getTimeout(control);
                            }
                        }
						else
                        {
                            context.timeout = 0;
                        }
                    }

					control = null;
				}
				else
					throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);
			}
			catch (SystemException e)
			{
				/*
				 * Not an Arjuna control!! Should not happen!!
				 */

				currentControl = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();

				currentControl = null;
			}
		}

		try
		{
			context.parents = resizeHierarchy(context.parents, index);
		}
		catch (Exception e)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.generror", new Object[]
				{ "ArjunaTransactionImple.resizeHierarchy", e });
			}

			context = null;
		}

		return context;
	}

	/*
	 * Watch out - we can resize down as well as up!
	 */

	private final TransIdentity[] resizeHierarchy (TransIdentity[] current, int size)
	{
		if ((current == null) || (size == 0))
			return new TransIdentity[0];

		if (current.length == size)
			return current;

		TransIdentity[] toReturn = new TransIdentity[size];
		int copySize = ((size < current.length) ? size : current.length);

		System.arraycopy(current, 0, toReturn, 0, copySize);

		if (copySize < size)
		{
			for (int j = copySize; j < size; j++)
				toReturn[j] = null;
		}

		return toReturn;
	}

	/*
	 * Could perhaps do garbage collection here after a certain number of
	 * transactions have been destroyed. Environment variable enabled?
	 */

	protected final void destroyAction ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ArjunaTransactionImple::destroyAction for "
					+ get_uid());
		}

		/*
		 * Only do if we are a top-level transaction, since otherwise we cannot
		 * have created recovery coordinators.
		 */

		if (parentHandle == null)
		{
			Object params[] = new Object[1];

			params[0] = this;

			RecoveryCreator.destroyAllRecoveryCoordinators(params);
		}
		else
			parentHandle = null;

		/*
		 * switch (super.getHeuristicDecision()) { case
		 * TwoPhaseOutcome.HEURISTIC_ROLLBACK: case
		 * TwoPhaseOutcome.HEURISTIC_COMMIT: case
		 * TwoPhaseOutcome.HEURISTIC_MIXED: case
		 * TwoPhaseOutcome.HEURISTIC_HAZARD: { if
		 * (BasicAction.maintainHeuristics()) return; } }
		 */

		try
		{
			/*
			 * We do not need to do worry about deleting the transaction in Java
			 * as we do in C++ because of the way garbage collection works - the
			 * committing thread has a reference to the transaction which keeps
			 * it alive.
			 */

			if (controlHandle != null)
			{
				OTSManager.destroyControl(controlHandle);

				controlHandle = null;
			}
		}
		catch (ActiveThreads ex1)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.generror", new Object[]
				{ "ArjunaTransactionImple.destroyAction", ex1 });
			}
		}
		catch (BadControl ex2)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.generror", new Object[]
				{ "ArjunaTransactionImple.destroyAction", ex2 });
			}
		}
		catch (ActiveTransaction ex3)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.generror", new Object[]
				{ "ArjunaTransactionImple.destroyAction", ex3 });
			}
		}
		catch (Destroyed ex4)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.generror", new Object[]
				{ "ArjunaTransactionImple.destroyAction", ex4 });
			}
		}
		catch (OutOfMemoryError ex5)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.generror", new Object[]
				{ "ArjunaTransactionImple.destroyAction", ex5 });
			}

			/*
			 * Rather than try again after running gc simply return and let the
			 * user deal with it. May help with memory!
			 */

			System.gc();
		}
		catch (Exception ex6)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.generror", new Object[]
				{ "ArjunaTransactionImple.destroyAction", ex6 });
			}
		}
	}

	protected ArjunaTransactionImple parentTransaction; // rather than rely on

	// BasicAction.parent()
	protected ControlImple controlHandle;

	private int hashCode;

	private int topLevelHashCode;

	private Control parentHandle;

	private org.omg.CosTransactions.Status currentStatus;

	private Thread transactionCreator; // probably null most of the time!

	private BasicAction rootAction;

	private SortedSet _synchs;
    private SynchronizationRecord _currentRecord; // the most recently processed Synchronization.

    static int _ipType = Arjuna.XID();

	static boolean _subtran = true;

	static boolean _syncOn = true;

	static boolean _checkedTransactions = false;

	static boolean _propagateTerminator = false;

	static boolean _propagateRemainingTimeout = true;  // OTS 1.2 onwards supported this.

	/**
	 * @message com.arjuna.ats.internal.jts.orbspecific.coordinator.ipunknown
	 *          {0} - unknown interposition type: {1}
	 */

	static
	{
		String interpositionType = jtsPropertyManager.getJTSEnvironmentBean().getInterposition();

		if (interpositionType != null)
		{
			int ipType = Arjuna.nameToXID(interpositionType);

			if (ipType != -1)
				ArjunaTransactionImple._ipType = ipType;
			else
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.coordinator.ipunknown", new Object[]
					{ "ArjunaTransactionImple.init", interpositionType });
				}
			}
		}

		_subtran = jtsPropertyManager.getJTSEnvironmentBean().isSupportSubtransactions();
		_syncOn = jtsPropertyManager.getJTSEnvironmentBean().isSupportRollbackSync();
		_checkedTransactions = jtsPropertyManager.getJTSEnvironmentBean().isCheckedTransactions();
		_propagateTerminator = jtsPropertyManager.getJTSEnvironmentBean().isPropagateTerminator();
		_propagateRemainingTimeout = jtsPropertyManager.getJTSEnvironmentBean().isTimeoutPropagation();
	}

    public java.util.Map<Uid, String> getSynchronizations()
    {
        if (_synchs != null)
        {
            java.util.Map<Uid, String> synchMap = new java.util.HashMap<Uid, String> ();
            SynchronizationRecord[] synchs = (SynchronizationRecord[]) _synchs.toArray(new SynchronizationRecord[] {});

            for (SynchronizationRecord synch : synchs)
            {
                Synchronization c = synch.contents();
                String cn;

                if (c._is_a(ManagedSynchronizationHelper.id()))
                {
                    ManagedSynchronization mc = ManagedSynchronizationHelper.narrow(c);

                    try {
                        cn = mc.instanceName(); //implementationType() ;
                    } catch (Throwable t) {
                        cn = synch.getClass().getCanonicalName();
                    }
                }
                else {
                    cn = synch.getClass().getCanonicalName();
                }

                synchMap.put(synch.get_uid(), cn);
            }

            return synchMap;
        }

        return Collections.EMPTY_MAP;
    }
}
