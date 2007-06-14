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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: BasicAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.gandiva.ObjectName;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.utils.ThreadUtil;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.internal.arjuna.thread.*;
import java.util.*;

import java.io.IOException;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

/**
 * BasicAction does most of the work of an atomic action, but does not manage
 * thread scoping. This is the responsibility of any derived classes.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: BasicAction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 * 
 * 
 * 
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_1
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_1] - Action nesting
 *          error - deletion of action id {0} invoked while child actions active
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_2
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_2] - Aborting child
 *          {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_3
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_3] - Destructor of
 *          still running action id {0} invoked - Aborting
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_5
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_5] - Activate of
 *          atomic action with id {0} and type {1} unexpectedly failed
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_6
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_6] -
 *          BasicAction::addChildThread () action {0} adding {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_7
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_7] -
 *          BasicAction::addChildThread () action {0} adding {1} result = {2}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_8
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_8] -
 *          BasicAction::removeChildThread () action {0} removing {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_9
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_9] - \
 *          BasicAction::removeChildThread () action {0} removing {1} result =
 *          {2}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_10
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_10] -
 *          BasicAction::addAction () action {0} adding {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_11
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_11] -
 *          BasicAction::addChildAction () action {0} adding {1} result = {2}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_12
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_12] -
 *          BasicAction::removeChildAction () action {0} removing {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_13
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_13] -
 *          BasicAction::removeChildAction () action {0} removing {1} result =
 *          {2}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_14
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_14] -
 *          BasicAction::save_state - next record to pack is a \n {0} record
 *          ({1}) should save it? = {2}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_15
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_15] - Packing a {0}
 *          record
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_16
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_16] - Packing a
 *          NONE_RECORD
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_17
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_17] - HeuristicList -
 *          packing a {0} record
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_18
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_18] - HeuristicList -
 *          packing a NONE_RECORD
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_19
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_19] - Packing action
 *          status of {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_20
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_20] - Unpacked a {0}
 *          record
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_21
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_21] -
 *          BasicAction.restore_state - could not recover {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_22
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_22] - HeuristicList -
 *          Unpacked heuristic list size of {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_23
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_23] - HeuristicList -
 *          Unpacked a {0} record
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_24
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_24] -
 *          BasicAction.restore_state - error unpacking action status.
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_25
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_25] - Restored action
 *          status of {0} {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_26
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_26] - Restored action
 *          type {0} {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_27
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_27] - Restored
 *          heuristic decision of {0} {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_28
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_28] -
 *          BasicAction.destroy called on {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_29
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_29] -
 *          BasicAction.Begin of action {0} ignored - incorrect invocation
 *          sequence {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_30
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_30] -
 *          BasicAction.Begin of action {0} ignored - no parent and set as
 *          nested action!
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_31
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_31] -
 *          BasicAction.Begin of action {0} ignored - parent action {1} is not
 *          running: {2}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_32
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_32] - The Arjuna
 *          licence does not permit any further transactions to be committed!
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_33
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_33] - End called on
 *          non-running atomic action {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_34
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_34] - End called on
 *          already committed atomic action {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_35
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_35] - End called
 *          illegally on atomic action {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_36
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_36] -
 *          BasicAction.End() - prepare phase of action-id {0} failed.
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_37
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_37] - Received
 *          heuristic: {0} .
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_38
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_38] - Action Aborting
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_39
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_39] - Abort called on
 *          non-running atomic action {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_40
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_40] - Abort called on
 *          already aborted atomic action {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_41
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_41] - Abort called
 *          illegaly on atomic action {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_42
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_42] - BasicAction {0} -
 *          non-empty ( {1} ) pendingList {2}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_43
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_43] - Transaction {0}
 *          marked as rollback only. Will abort.
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_44
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_44] - Cannot force
 *          parent to rollback - no handle!
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_45
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_45] -
 *          BasicAction::prepare - creating intentions list failed for {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_46
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_46] -
 *          BasicAction::prepare - intentions list write failed for {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_47
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_47] - One-phase
 *          commit of action {0} received heuristic decision: {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_48
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_48] -
 *          BasicAction.onePhaseCommit failed - no object store for atomic
 *          action state!
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_49
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_49] - Prepare phase
 *          of nested action {0} received inconsistent outcomes.
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_50
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_50] - Prepare phase
 *          of action {0} received heuristic decision: {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_51
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_51] -
 *          BasicAction.doCommit for {0} received {1} from {2}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_52
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_52] - Top-level abort
 *          of action {0} received heuristic decision: {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_53
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_53] - Nested abort of
 *          action {0} received heuristic decision: {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_54
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_54] - Top-level abort
 *          of action {0} received {1} from {2}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_55
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_55] - Nested abort of
 *          action {0} received {1} from {2}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_56
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_56] -
 *          BasicAction.checkIsCurrent {0} - terminating non-current
 *          transaction: {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_57
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_57] - Commit of
 *          action id {0} invoked while multiple threads active within it.
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_58
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_58] - Abort of action
 *          id {0} invoked while multiple threads active within it.
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_59
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_59] - Commit of
 *          action id {0} invoked while child actions active
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_60
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_60] - Abort of action
 *          id {0} invoked while child actions active
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_61
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_61] - \tAborting
 *          child: {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_62
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_62] - \tNow aborting
 *          self: {0}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_63
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_63] -
 *          BasicAction::removeAllChildThreads () action {0} removing {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_64
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_64] -
 *          BasicAction.updateState - Could not create ObjectState for
 *          failedList
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_65
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_65] - BasicAction.End -
 *          Could not write failed list
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_66
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_66] - Action {0} with
 *          parent status {1}
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_67
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_67] - Running Top
 *          Level Action {0} from within nested action ({1})
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_68
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_68] - (Internal)
 *          BasicAction.merge - record rejected
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_69
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_69] - No object store
 *          for:
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_70
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_70] - Could not
 *          remove intentions list:
 * @message com.arjuna.ats.arjuna.coordinator.BasicAction_71
 *          [com.arjuna.ats.arjuna.coordinator.BasicAction_71] - Deactivation of
 *          atomic action with id {0} and type {1} unexpectedly failed
 */

public class BasicAction extends StateManager
{

	public BasicAction ()
	{
		super(ObjectType.NEITHER);

		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::BasicAction()");
		}

		pendingList = null;
		preparedList = null;
		readonlyList = null;
		failedList = null;
		heuristicList = null;

		currentHierarchy = null;
		currentStore = null;
		savedIntentionList = false;

		actionStatus = ActionStatus.CREATED;
		actionType = ActionType.NESTED;

		parentAction = null;
		recordBeingHandled = null;

		heuristicDecision = TwoPhaseOutcome.PREPARE_OK;
		_checkedAction = new CheckedAction();

		_childThreads = null;
		_childActions = null;
	}

	/**
	 * BasicAction constructor with a Uid. This constructor is for recreating an
	 * BasicAction, typically during crash recovery.
	 */

	public BasicAction (Uid objUid)
	{
		super(objUid, ObjectType.NEITHER);

		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::BasicAction("
					+ objUid + ")");
		}

		pendingList = null;
		preparedList = null;
		readonlyList = null;
		failedList = null;
		heuristicList = null;

		currentHierarchy = null;
		currentStore = null;
		savedIntentionList = false;

		actionStatus = ActionStatus.CREATED;
		actionType = ActionType.NESTED;

		parentAction = null;
		recordBeingHandled = null;

		heuristicDecision = TwoPhaseOutcome.PREPARE_OK;
		_checkedAction = new CheckedAction();

		_childThreads = null;
		_childActions = null;
	}

	/**
	 * BasicAction destructor. Under normal circumstances we do very little.
	 * However there exists the possibility that this action is being deleted
	 * while still running (user forgot to commit/abort) - in which case we do
	 * an abort for him and mark all our parents as unable to commit.
	 * Additionally due to scoping we may not be the current action - but in
	 * that case the current action must be one of our nested actions so by
	 * applying abort to it we should end up at ourselves!
	 */

	public void finalize ()
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::finalize()");
		}

		if ((actionStatus == ActionStatus.RUNNING)
				|| (actionStatus == ActionStatus.ABORT_ONLY))
		{
			/* If current action is one of my children there's an error */

			BasicAction currentAct = BasicAction.Current();

			if ((currentAct != null) && (currentAct != this))
			{
				/*
				 * Is the current action a child of this action? If so, abort
				 * until we get to the current action. This works even in a
				 * multi-threaded environment where each thread may have a
				 * different notion of current, since Current returns the thread
				 * specific current.
				 */

				if (currentAct.isAncestor(get_uid()))
				{
					if (tsLogger.arjLoggerI18N.isWarnEnabled())
					{
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_1", new Object[]
						{ get_uid() });
					}

					while ((currentAct != this) && (currentAct != null))
					{
						if (tsLogger.arjLoggerI18N.isWarnEnabled())
						{
							tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_2", new Object[]
							{ currentAct.get_uid() });
						}

						currentAct.Abort();

						currentAct = BasicAction.Current();
					}
				}
			}

			BasicAction parentAct = parent();

			/* prevent commit of parents (safety) */

			while (parentAct != null)
			{
				parentAct.preventCommit();
				parentAct = parentAct.parent();
			}

			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_3", new Object[]
				{ get_uid() });
			}

			/* This will also kill any children */

			Abort();
		}
		else
		{
			if (actionStatus == ActionStatus.PREPARED)
				Thread.yield();
		}

		pendingList = null;
		preparedList = null;
		readonlyList = null;
		failedList = null;
		heuristicList = null;

		currentStore = null;
		currentHierarchy = null;

		_checkedAction = null;

		if (_childThreads != null)
		{
			_childThreads.clear();
			_childThreads = null;
		}

		if (_childActions != null)
		{
			_childActions.clear();
			_childActions = null;
		}
	}

	/**
	 * Return the action hierarchy for this transaction.
	 */

	public final synchronized ActionHierarchy getHierarchy ()
	{
		return currentHierarchy;
	}

	/**
	 * Force the only outcome for the transaction to be to rollback. Only
	 * possible if this transaction has not (or is not) terminated.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public final boolean preventCommit ()
	{
		boolean res = false;

		//	if (lockMutex())
		{
			if (actionStatus == ActionStatus.RUNNING)
				actionStatus = ActionStatus.ABORT_ONLY;

			res = (actionStatus == ActionStatus.ABORT_ONLY);

			//	    unlockMutex();
		}

		return res;
	}

	/**
	 * @return the number of threads associated with this transaction.
	 */

	public final int activeThreads ()
	{
		if (_childThreads != null)
			return _childThreads.size();
		else
			return 0;
	}

	/**
	 * Add a record to the atomic action. This function returns AR_ADDED if the
	 * record is added. AR_REJECTED if the record cannot be added because the
	 * action is past the prepare phase, and IGNORED otherwise.
	 * 
	 * @return <code>AddOutcome</code> indicating outcome.
	 */

	public final synchronized int add (AbstractRecord A)
	{
		int result = AddOutcome.AR_REJECTED;

		criticalStart();

		if ((actionStatus <= ActionStatus.ABORTING)
				&& ((recordBeingHandled == null) || !(recordBeingHandled.equals(A))))
		{
			if (pendingList == null)
				pendingList = new RecordList();

			result = (pendingList.insert(A) ? AddOutcome.AR_ADDED
					: AddOutcome.AR_DUPLICATE);
		}

		criticalEnd();

		return result;
	}

	/**
	 * @return the depth of the current transaction hierarchy.
	 */

	public final synchronized int hierarchyDepth ()
	{
		if (currentHierarchy != null)
			return currentHierarchy.depth();
		else
			return 0; /* should never happen */
	}

	/**
	 * boolean function that checks whether the Uid passed as an argument is the
	 * Uid for an ancestor of the current atomic action.
	 * 
	 * @return <code>true</code> if the parameter represents an ancestor,
	 *         <code>false</code> otherwise.
	 */

	public final boolean isAncestor (Uid ancestor)
	{
		boolean res = false;

		if (get_uid().equals(ancestor)) /* actions are their own ancestors */
			res = true;
		else
		{
			if ((parentAction != null) && (actionType != ActionType.TOP_LEVEL))
				res = parentAction.isAncestor(ancestor);
		}

		return res;
	}

	/**
	 * @return a reference to the parent BasicAction
	 */

	public final BasicAction parent ()
	{
		if (actionType == ActionType.NESTED)
			return parentAction;
		else
			return null;
	}

	public final int typeOfAction ()
	{
		return actionType;
	}

	/**
	 * @return the status of the BasicAction
	 */

	public final int status ()
	{
		int s = ActionStatus.INVALID;

		//	if (tryLockMutex())
		{
			s = actionStatus;

			//	    unlockMutex();
		}

		return s;
	}

	/**
	 * Set up an object store and assign it to the currentStore variable.
	 * 
	 * @return the object store implementation to use.
	 * @see com.arjuna.ats.arjuna.objectstore.ObjectStore
	 */

	public ObjectStore getStore ()
	{
		if (currentStore == null)
		{
			currentStore = TxControl.getStore();
		}

		return currentStore;
	}

	public final ObjectStore store ()
	{
		return getStore();
	}

	/**
	 * The following function returns the Uid of the top-level atomic action. If
	 * this is the top-level transaction then it is equivalent to calling
	 * get_uid().
	 * 
	 * @return the top-level transaction's <code>Uid</code>.
	 */

	public final Uid topLevelActionUid ()
	{
		BasicAction root = this;

		while (root.parent() != null)
			root = root.parent();

		return root.get_uid();
	}

	/**
	 * @return a reference to the top-level transaction. If this is the
	 *         top-level transaction then a reference to itself will be
	 *         returned.
	 */

	public final BasicAction topLevelAction ()
	{
		BasicAction root = this;

		while (root.parent() != null)
			root = root.parent();

		return root;
	}

	/**
	 * Overloaded version of activate -- sets up the store, performs read_state
	 * followed by restore_state. The store root is <code>null</code>.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public boolean activate ()
	{
		return activate(null);
	}

	/**
	 * Overloaded version of activate -- sets up the store, performs read_state
	 * followed by restore_state. The root of the object store to use is
	 * specified in the <code>root</code> parameter.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public boolean activate (String root)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::activate() for action-id "
					+ get_uid());
		}

		boolean restored = false;

		// Set up store
		ObjectStore aaStore = store();

		if (aaStore == null)
			return false;

		try
		{
			// Read object state

			InputObjectState oState = aaStore.read_committed(getSavingUid(), type());

			if (oState != null)
			{
				synchronized (this)
				{
					restored = restore_state(oState, ObjectType.ANDPERSISTENT);
				}

				oState = null;
			}
			else
			{
				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_5", new Object[]
					{ get_uid(), type() });
				}

				restored = false;
			}

			return restored;
		}
		catch (ObjectStoreException e)
		{
			if (tsLogger.arjLogger.isWarnEnabled())
				tsLogger.arjLogger.warn(e.getMessage());

			return false;
		}
	}

	/**
	 * This operation deactivates a persistent object. It behaves in a similar
	 * manner to the activate operation, but has an extra argument which defines
	 * whether the object's state should be committed or written as a shadow.
	 * 
	 * The root of the object store is <code>null</code>. It is assumed that
	 * this is being called during a transaction commit.
	 * 
	 * @return <code>true</code> on success, <code>false</code> otherwise.
	 */

	public boolean deactivate ()
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::deactivate() for action-id "
					+ get_uid());
		}

		boolean deactivated = false;

		// Set up store
		ObjectStore aaStore = store();

		if (aaStore == null)
			return false;

		try
		{
			// Write object state
			OutputObjectState oState = new OutputObjectState();

			if (save_state(oState, ObjectType.ANDPERSISTENT))
			{
				if (aaStore.write_committed(getSavingUid(), type(), oState))
				{
					deactivated = true;
				}
				else
				{
					deactivated = false;
				}

				oState = null;
			}
			else
			{
				deactivated = false;
			}

			/** If we failed to deactivate then output warning * */
			if (!deactivated)
			{
				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_5", new Object[]
					{ get_uid(), type() });
				}
			}
		}
		catch (ObjectStoreException e)
		{
			if (tsLogger.arjLogger.isWarnEnabled())
				tsLogger.arjLogger.warn(e.getMessage());

			deactivated = false;
		}

		return deactivated;
	}

	/**
	 * Add the current thread to the list of threads associated with this
	 * transaction.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public final boolean addChildThread () // current thread
	{
		return addChildThread(Thread.currentThread());
	}

	/**
	 * Add the specified thread to the list of threads associated with this
	 * transaction.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public final boolean addChildThread (Thread t)
	{
		if (tsLogger.arjLoggerI18N.debugAllowed())
		{
			tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_6", new Object[]
			{ get_uid(), t });
		}

		if (t == null)
			return false;

		boolean result = false;

		criticalStart();

		synchronized (this)
		{
			if (actionStatus <= ActionStatus.ABORTING)
			{
				if (_childThreads == null)
					_childThreads = new Hashtable();

				_childThreads.put(ThreadUtil.getThreadId(t), t); // makes sure so we don't get
												   // duplicates

				result = true;
			}
		}

		criticalEnd();

		if (tsLogger.arjLoggerI18N.debugAllowed())
		{
			tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_7", new Object[]
			{ get_uid(), t, new Boolean(result) });
		}

		return result;
	}

	/*
	 * Can be done at any time (Is this correct?)
	 */

	/**
	 * Remove a child thread. The current thread is removed.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public final boolean removeChildThread () // current thread
	{
		return removeChildThread(ThreadUtil.getThreadId());
	}

	/**
	 * Remove the specified thread from the transaction.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public final boolean removeChildThread (String threadId)
	{
		if (tsLogger.arjLoggerI18N.debugAllowed())
		{
			tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_8", new Object[]
			{ get_uid(), threadId });
		}

		if (threadId == null)
			return false;

		boolean result = false;

		criticalStart();

		synchronized (this)
		{
			if (_childThreads != null)
			{
				_childThreads.remove(threadId);
				result = true;
			}
		}

		criticalEnd();

		if (tsLogger.arjLoggerI18N.debugAllowed())
		{
			if (result)
				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_9", new Object[]
				{ get_uid(), threadId, "true" });
			else
				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_9", new Object[]
				{ get_uid(), threadId, "false" });
		}

		return result;
	}

	/**
	 * Add a new child action to the atomic action.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public final boolean addChildAction (BasicAction act)
	{
		if (tsLogger.arjLoggerI18N.debugAllowed())
		{
			tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_10", new Object[]
			{ get_uid(), ((act != null) ? act.get_uid() : Uid.nullUid()) });
		}

		if (act == null)
			return false;

		boolean result = false;

		criticalStart();

		synchronized (this)
		{
			/*
			 * Must be <= as we sometimes need to do processing during commit
			 * phase.
			 */

			if (actionStatus <= ActionStatus.ABORTING)
			{
				if (_childActions == null)
					_childActions = new Hashtable();

				_childActions.put(act, act);
				result = true;
			}
		}

		criticalEnd();

		if (tsLogger.arjLoggerI18N.debugAllowed())
		{
			if (result)
				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_11", new Object[]
				{ get_uid(), act.get_uid(), "true" });
			else
				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_11", new Object[]
				{ get_uid(), act.get_uid(), "false" });
		}

		return result;
	}

	/*
	 * Can be done at any time (Is this correct?)
	 */

	/**
	 * Remove a child action.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public final boolean removeChildAction (BasicAction act)
	{
		if (tsLogger.arjLoggerI18N.debugAllowed())
		{
			tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_12", new Object[]
			{ get_uid(), ((act != null) ? act.get_uid() : Uid.nullUid()) });
		}

		if (act == null)
			return false;

		boolean result = false;

		criticalStart();

		synchronized (this)
		{
			if (_childActions != null)
			{
				_childActions.remove(act);
				result = true;
			}
		}

		criticalEnd();

		if (tsLogger.arjLoggerI18N.debugAllowed())
		{
			if (result)
				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_13", new Object[]
				{ get_uid(), act.get_uid(), "true" });
			else
				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_13", new Object[]
				{ get_uid(), act.get_uid(), "false" });
		}

		return result;
	}

	/**
	 * Add the specified CheckedAction object to this transaction.
	 * 
	 * @return the previous <code>CheckedAction</code>.
	 * @see com.arjuna.ats.arjuna.coordinator.CheckedAction
	 */

	public final synchronized CheckedAction setCheckedAction (CheckedAction c)
	{
		criticalStart();

		CheckedAction toReturn = _checkedAction;

		_checkedAction = c;

		criticalEnd();

		return toReturn;
	}

	/**
	 * @return the Uid that the transaction's intentions list will be saved
	 *         under.
	 */

	public Uid getSavingUid ()
	{
		return get_uid();
	}

	/**
	 * Overloads Object.toString()
	 */

	public String toString ()
	{
		return new String("BasicAction: " + get_uid() + " status: "
				+ ActionStatus.stringForm(actionStatus));
	}

	/**
	 * Redefined version of save_state and restore_state from StateManager.
	 * 
	 * Normal operation (no crashes):
	 * 
	 * BasicAction.save_state is called after a successful prepare. This causes
	 * and BasicAction object to be saved in the object store. This object
	 * contains primarily the "intentions list" of the BasicAction. After
	 * successfully completing phase 2 of the commit protocol, the BasicAction
	 * object is deleted from the store.
	 * 
	 * Failure cases:
	 * 
	 * If a server crashes after successfully preparing, then upon recovery the
	 * action must be resolved (either committed or aborted) depending upon
	 * whether the co-ordinating atomic action committed or aborted. Upon server
	 * recovery, the crash recovery mechanism detects ServerBasicAction objects
	 * in the object store and attempts to activate the BasicAction object of
	 * the co-ordinating action. If this is successful then the SAA is committed
	 * else aborted.
	 * 
	 * If, when processing phase 2 of the commit protocol, the co-ordinator
	 * experiences a failure to commit from one of the records then the
	 * BasicAction object is NOT deleted. It is rewritten when a new state which
	 * contains a list of the records that failed during phase 2 commit. This
	 * list is called the "failedList".
	 * 
	 * The crash recovery manager will detect local BasicAction objects in
	 * addition to SAA objects in the objectstore. An attempt will be made to
	 * commit these actions. If the action contained a call to a now dead
	 * server, this action can never be resolved and the AA object can never be
	 * removed. However, if the action is purely local then after the processing
	 * is complete the removed by crash recovery.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public boolean save_state (OutputObjectState os, int ot)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::save_state ()");
		}

		try
		{
			packHeader(os, get_uid(), Utility.getProcessUid());
		}
		catch (IOException e)
		{
			return false;
		}

		/*
		 * In a presumed abort scenario, this routine is called: a) After a
		 * successful prepare - to save the intentions list. b) After a failure
		 * during phase 2 of commit - to overwrite the intentions list by the
		 * failedList.
		 * 
		 * If we're using presumed nothing, then it could be called: a) Whenever
		 * a participant is registered.
		 */

		RecordList listToSave = null;
		boolean res = true;

		/*
		 * If we have a failedList then we are re-writing a BasicAction object
		 * after a failure during phase 2 commit
		 */

		if ((failedList != null) && (failedList.size() > 0))
		{
			listToSave = failedList;
		}
		else
		{
			listToSave = preparedList;
		}

		AbstractRecord first = ((listToSave != null) ? listToSave.getFront()
				: null);
		AbstractRecord temp = first;
		boolean havePacked = ((listToSave == null) ? false : true);

		while ((res) && (temp != null))
		{
			listToSave.putRear(temp);

			/*
			 * First check to see if we need to call save_state. If we do then
			 * we must first save the record type (and enum) and then save the
			 * unique identity of the record (a string). The former is needed to
			 * determine what type of record we are restoring, while the latter
			 * is required to re-create the actual record.
			 */

			/*
			 * First check to see if we need to call save_state. If we do then
			 * we must first save the record type. This is used to determine
			 * which type of record to create when restoring.
			 */

			if (tsLogger.arjLoggerI18N.debugAllowed())
			{
				if (temp.doSave())
					tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_14", new Object[]
					{ Integer.toString(temp.typeIs()), temp.type(), "true" });
				else
					tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_14", new Object[]
					{ Integer.toString(temp.typeIs()), temp.type(), "false" });
			}

			if (temp.doSave())
			{
				res = true;

				try
				{
					if (tsLogger.arjLoggerI18N.debugAllowed())
					{
						tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_15", new Object[]
						{ Integer.toString(temp.typeIs()) });
					}

					os.packInt(temp.typeIs());
					res = temp.save_state(os, ot);
				}
				catch (IOException e)
				{
					res = false;
				}
			}

			temp = listToSave.getFront();

			if (temp == first)
			{
				listToSave.putFront(temp);
				temp = null;
			}
		}

		/*
		 * If we only ever had a heuristic list (e.g., one-phase commit) then
		 * pack a record delimiter.
		 */

		if (res && (os.notempty() || !havePacked))
		{
			try
			{
				if (tsLogger.arjLoggerI18N.debugAllowed())
				{
					tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_16");
				}

				os.packInt(RecordType.NONE_RECORD);
			}
			catch (IOException e)
			{
				res = false;
			}
		}

		if (res)
		{
			// Now deal with anything on the heuristic list!

			int hSize = ((heuristicList == null) ? 0 : heuristicList.size());

			try
			{
				os.packInt(hSize);
			}
			catch (IOException e)
			{
				res = false;
			}

			if (res && (hSize > 0))
			{
				first = heuristicList.getFront();
				temp = first;

				while (res && (temp != null))
				{
					heuristicList.putRear(temp);

					if (temp.doSave())
					{
						res = true;

						try
						{
							if (tsLogger.arjLoggerI18N.debugAllowed())
							{
								tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_17", new Object[]
								{ Integer.toString(temp.typeIs()) });
							}

							os.packInt(temp.typeIs());
							res = temp.save_state(os, ot);
						}
						catch (IOException e)
						{
							res = false;
						}
					}

					temp = heuristicList.getFront();

					if (temp == first)
					{
						heuristicList.putFront(temp);
						temp = null;
					}
				}

				if (res && os.notempty())
				{
					try
					{
						if (tsLogger.arjLoggerI18N.debugAllowed())
						{
							tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_18");
						}

						os.packInt(RecordType.NONE_RECORD);
					}
					catch (IOException e)
					{
						res = false;
					}
				}
			}
		}

		if (res && os.notempty())
		{
			try
			{
				if (tsLogger.arjLoggerI18N.debugAllowed())
				{
					tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_19", new Object[]
					{ ActionStatus.stringForm(actionStatus) });
				}

				os.packInt(actionStatus);
				os.packInt(actionType); // why pack since only top-level?
				os.packInt(heuristicDecision); // can we optimize?
			}
			catch (IOException e)
			{
				res = false;
			}
		}

		return res;
	}

	/**
	 * This assumes the various lists are zero length when it is called.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public boolean restore_state (InputObjectState os, int ot)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::restore_state ()");
		}

		createPreparedLists();

		boolean res = true;
		int record_type = RecordType.NONE_RECORD;
		int tempActionStatus = ActionStatus.INVALID;
		int tempActionType = ActionType.TOP_LEVEL;
		int tempHeuristicDecision = TwoPhaseOutcome.PREPARE_OK;

		/*
		 * Unpack the prepared list. Note: This may either be a full intentions
		 * list or just the failedList, either way, restore it as the prepared
		 * list.
		 */

		try
		{
			Uid txId = new Uid(Uid.nullUid());
			Uid processUid = new Uid(Uid.nullUid());

			unpackHeader(os, txId, processUid);
		}
		catch (IOException e)
		{
			return false;
		}

		try
		{
			record_type = os.unpackInt();

			if (tsLogger.arjLoggerI18N.debugAllowed())
			{
				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_20", new Object[]
				{ Integer.toString(record_type) });
			}
		}
		catch (IOException e)
		{
			res = false;
		}

		while ((res) && (record_type != RecordType.NONE_RECORD))
		{
			RecoveryAbstractRecord recRecord = new RecoveryAbstractRecord(
					RecordType.typeToClassName(record_type), record_type, true);
			AbstractRecord record = recRecord.record();

			if (record == null)
			{
				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_21", new Object[]
					{ Integer.toString(record_type) });
				}

				res = false;
			}
			else
				res = (record.restore_state(os, ot) && preparedList.insert(record));

			if (res)
			{
				try
				{
					record_type = os.unpackInt();

					if (tsLogger.arjLoggerI18N.debugAllowed())
					{
						tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_20", new Object[]
						{ Integer.toString(record_type) });
					}
				}
				catch (IOException e)
				{
					res = false;
				}
			}
		}

		// Now deal with the heuristic list!

		int hSize = 0;

		if (res)
		{
			try
			{
				hSize = os.unpackInt();

				if (tsLogger.arjLoggerI18N.debugAllowed())
				{
					tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_22", new Object[]
					{ Integer.toString(hSize) });
				}
			}
			catch (IOException e)
			{
				res = false;
			}
		}

		if (hSize > 0)
		{
			try
			{
				record_type = os.unpackInt();

				if (tsLogger.arjLoggerI18N.debugAllowed())
				{
					tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_23", new Object[]
					{ Integer.toString(record_type) });
				}
			}
			catch (IOException e)
			{
				res = false;
			}

			while ((res) && (record_type != RecordType.NONE_RECORD))
			{
				RecoveryAbstractRecord record = new RecoveryAbstractRecord(
						RecordType.typeToClassName(record_type), record_type,
						true);

				res = (record.restore_state(os, ot) && heuristicList.insert(record));

				try
				{
					record_type = os.unpackInt();

					if (tsLogger.arjLoggerI18N.debugAllowed())
					{
						tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_23", new Object[]
						{ Integer.toString(record_type) });
					}
				}
				catch (IOException e)
				{
					res = false;
				}
			}
		}

		if (res)
		{
			try
			{
				tempActionStatus = os.unpackInt();
				tempActionType = os.unpackInt();
				tempHeuristicDecision = os.unpackInt();
			}
			catch (IOException e)
			{
				if (tsLogger.arjLoggerI18N.isWarnEnabled())
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_24");

				res = false;
			}
		}

		if (res)
		{
			if (tsLogger.arjLoggerI18N.debugAllowed())
			{
				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_25", new Object[]
				{ ActionStatus.stringForm(tempActionStatus), Integer.toString(tempActionStatus) });

				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_26", new Object[]
				{ ((tempActionType == ActionType.NESTED) ? "Nested"
						: "Top-level"), Integer.toString(tempActionType) });

				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_27", new Object[]
				{ TwoPhaseOutcome.stringForm(tempHeuristicDecision), Integer.toString(tempHeuristicDecision) });
			}

			actionStatus = tempActionStatus;
			actionType = tempActionType;
			heuristicDecision = tempHeuristicDecision;
			savedIntentionList = true;
		}

		return res;
	}

	/**
	 * Overloads StateManager.type()
	 */

	public String type ()
	{
		return "/StateManager/BasicAction";
	}

	/**
	 * @return the thread's notion of the current transaction.
	 */

	public static BasicAction Current ()
	{
		return ThreadActionData.currentAction();
	}

	/**
	 * If heuristic outcomes are returned, by default we will not save the state
	 * once the forget method has been called on them (which happens as soon as
	 * we have received all outcomes from registered resources). By specifying
	 * otherwise, we will always maintain the heuristic information, which may
	 * prove useful for logging and off-line resolution.
	 * 
	 * @return <code>true</code> if the transaction should save its heuristic
	 *         information, <code>false</code> otherwise.
	 */

	public static boolean maintainHeuristics ()
	{
		return TxControl.maintainHeuristics;
	}

	/**
	 * Overloads <code>StateManager.destroy</code> to prevent destroy being
	 * called on a BasicAction. Could be a *very* bad idea!!
	 * 
	 * @return <code>false</code>.
	 * @see com.arjuna.ats.arjuna.StateManager
	 */

	public boolean destroy ()
	{
		return true;
	}

	/**
	 * @return the list of child transactions. Currently only their ids are
	 *         given.
	 * @since JTS 2.2.
	 */

	public final Object[] childTransactions ()
	{
		int size = _childActions.size();

		if (size > 0)
		{
			Collection c = _childActions.values();

			return c.toArray();
		}

		return null;
	}

	public boolean equals (java.lang.Object obj)
	{
		if (obj instanceof BasicAction)
		{
			if (((BasicAction) obj).get_uid().equals(get_uid()))
				return true;
		}

		return false;
	}

	/**
	 * Forget any heuristics we may have received, and tell the resources which
	 * generated them to forget too.
	 * 
	 * @return <code>true</code> if heuristic information (if any) was
	 *         successfully forgotten, <code>false</code> otherwise.
	 */

	protected boolean forgetHeuristics ()
	{
		if ((heuristicList != null) && (heuristicList.size() > 0))
		{
			doForget(heuristicList);
			updateState();

			if (heuristicList.size() == 0)
				return true;
			else
				return false;
		}
		else
			return true;
	}

	/**
	 * Atomic action Begin operation. Does not change the calling thread's
	 * notion of the current transaction.
	 * 
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	protected synchronized int Begin (BasicAction parentAct)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::Begin() for action-id "
					+ get_uid());
		}
		
		if (actionStatus != ActionStatus.CREATED)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_29", new Object[]
				{ get_uid(), ActionStatus.stringForm(actionStatus) });
			}
		}
		else
		{
			actionInitialise(parentAct);
			actionStatus = ActionStatus.RUNNING;

			if ((actionType != ActionType.TOP_LEVEL)
					&& ((parentAct == null) || (parentAct.status() > ActionStatus.RUNNING)))
			{
				actionStatus = ActionStatus.ABORT_ONLY;

				if (parentAct == null)
				{
					if (tsLogger.arjLoggerI18N.isWarnEnabled())
					{
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_30", new Object[]
						{ get_uid() });
					}
				}
				else
				{
					if (tsLogger.arjLoggerI18N.debugAllowed())
					{
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_31", new Object[]
						{ get_uid(), parentAct.get_uid(), Integer.toString(parentAct.status()) });
					}
				}
			}

			ActionManager.manager().put(this);

			if (TxControl.enableStatistics)
			{
				TxStats.incrementTransactions();

				if (parentAct != null)
					TxStats.incrementNestedTransactions();
			}
		}

		return actionStatus;
	}

	/**
	 * End the atomic action by committing it. This invokes the prepare()
	 * operation. If this succeeds then the pendingList should be empty and the
	 * records that were formally on it will have been distributed between the
	 * preparedList and the readonlyList, also if the action is topLevel then
	 * the intention list will have been written to the object store. Then
	 * invoke phase2Commit and clean up the object store if necessary
	 * 
	 * If prepare() fails invoke phase2Abort. In this case the pendingList may
	 * still contain records but phase2Abort takes care of these. Also in this
	 * case no intention list has been written.
	 * 
	 * Does not change the calling thread's notion of the current transaction.
	 * 
	 * Any heuristic outcomes will only be reported if the parameter is
	 * <code>true</code>.
	 * 
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	protected synchronized int End (boolean reportHeuristics)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::End() for action-id "
					+ get_uid());
		}

		/* Check for superfluous invocation */

		if ((actionStatus != ActionStatus.RUNNING)
				&& (actionStatus != ActionStatus.ABORT_ONLY))
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
				switch (actionStatus)
				{
				case ActionStatus.CREATED:
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_33", new Object[]
					{ get_uid() });
					break;
				case ActionStatus.COMMITTED:
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_34", new Object[]
					{ get_uid() });
					break;
				default:
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_35", new Object[]
					{ get_uid() });
					break;
				}
			}

			return actionStatus;
		}

		/*
		 * Check we are the current action. Abort parents if not true. Check we
		 * have not children (threads or actions).
		 */

		if (!checkIsCurrent() || checkChildren(true)
				|| (actionStatus == ActionStatus.ABORT_ONLY))
		{
			return Abort();
		}
	
		if (pendingList != null)
		{
			/*
			 * If we only have a single item on the prepare list then we can try
			 * to commit in a single phase.
			 */

			if (doOnePhase())
			{
				onePhaseCommit(reportHeuristics);
			}
			else
			{			
				if (prepare(reportHeuristics) == TwoPhaseOutcome.PREPARE_NOTOK)
				{
					if (tsLogger.arjLoggerI18N.isWarnEnabled())
					{
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_36", new Object[]
						{ get_uid() });
					}

					if (heuristicDecision != TwoPhaseOutcome.PREPARE_OK)
					{
						if (tsLogger.arjLoggerI18N.isWarnEnabled())
						{
							tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_37", new Object[]
							{ TwoPhaseOutcome.stringForm(heuristicDecision) });
						}
					}

					if (tsLogger.arjLoggerI18N.isWarnEnabled())
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_38");

					if (!reportHeuristics && TxControl.asyncCommit
							&& (parentAction == null))
					{
						AsyncCommit.create(this, false);
					}
					else
						phase2Abort(reportHeuristics); /* first phase failed */
				}
				else
				{
					if (!reportHeuristics && TxControl.asyncCommit
							&& (parentAction == null))
					{
						AsyncCommit.create(this, true);
					}
					else
						phase2Commit(reportHeuristics); /* first phase succeeded */
				}
			}
		}
		else
			actionStatus = ActionStatus.COMMITTED;

		//BasicAction.allActions.remove(get_uid());
		ActionManager.manager().remove(get_uid());

		boolean returnCurrentStatus = false;

		if (reportHeuristics || (!reportHeuristics && !TxControl.asyncCommit))
			returnCurrentStatus = true;

		if (TxControl.enableStatistics)
			TxStats.incrementCommittedTransactions();

		if (returnCurrentStatus)
		{
			if (reportHeuristics)
			{
				switch (heuristicDecision)
				{
				case TwoPhaseOutcome.PREPARE_OK:
				case TwoPhaseOutcome.FINISH_OK:
					break;
				case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
					return ActionStatus.H_ROLLBACK;
				case TwoPhaseOutcome.HEURISTIC_COMMIT:
					return ActionStatus.H_COMMIT;
				case TwoPhaseOutcome.HEURISTIC_MIXED:
					return ActionStatus.H_MIXED;
				case TwoPhaseOutcome.HEURISTIC_HAZARD:
				default:
					return ActionStatus.H_HAZARD;
				}
			}

			return actionStatus;
		}
		else
			return ActionStatus.COMMITTING; // if asynchronous then fake it.
	}

	/**
	 * This is the user callable abort operation. It is invoked prior to the
	 * start of two-phase commit and hence only processes records in the
	 * pendingList (the other lists should be empty).
	 * 
	 * Does not change the calling thread's notion of the current transaction.
	 * 
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	protected synchronized int Abort ()
	{
		if (tsLogger.arjLogger.isDebugEnabled())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::Abort() for action-id "
					+ get_uid());
		}

		/* Check for superfluous invocation */

		if ((actionStatus != ActionStatus.RUNNING)
				&& (actionStatus != ActionStatus.ABORT_ONLY)
				&& (actionStatus != ActionStatus.COMMITTING))
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
				switch (actionStatus)
				{
				case ActionStatus.CREATED:
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_39", new Object[]
					{ get_uid() });
					break;
				case ActionStatus.ABORTED:
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_40", new Object[]
					{ get_uid() });
					break;
				default:
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_41", new Object[]
					{ get_uid() });
					break;
				}
			}

			return actionStatus;
		}

		/*
		 * Check we are the current action. Abort parents if not true. Some
		 * implementations may want to override this.
		 */

		checkIsCurrent();

		/*
		 * Check we have no children (threads or actions).
		 */

		checkChildren(false);

		if (pendingList != null)
		{
			actionStatus = ActionStatus.ABORTING;

			while (pendingList.size() > 0)
				doAbort(pendingList, false); // turn off heuristics reporting

			/*
			 * In case we get here because an End has failed. In this case we
			 * still need to tell the heuristic resources to forget their
			 * decision.
			 */

			forgetHeuristics();
		}

		ActionManager.manager().remove(get_uid());

		actionStatus = ActionStatus.ABORTED;

		if (TxControl.enableStatistics)
			TxStats.incrementAbortedTransactions();

		return actionStatus;
	}

	/**
	 * Create a transaction of the specified type.
	 */

	protected BasicAction (int at)
	{
		super(ObjectType.NEITHER);

		pendingList = null;
		preparedList = null;
		readonlyList = null;
		failedList = null;
		heuristicList = null;

		currentHierarchy = null;
		currentStore = null;
		savedIntentionList = false;

		actionStatus = ActionStatus.CREATED;
		actionType = at;
		parentAction = null;
		recordBeingHandled = null;

		heuristicDecision = TwoPhaseOutcome.PREPARE_OK;
		_checkedAction = new CheckedAction();

		_childThreads = null;
		_childActions = null;
	}

	protected BasicAction (Uid u, int at, ObjectName objectName)
	{
		super(u, at, objectName);
	}

	/**
	 * Recreate the specified transaction. Used for crash recovery purposes.
	 */

	protected BasicAction (Uid u, int at)
	{
		super(u, ObjectType.NEITHER);

		pendingList = null;
		preparedList = null;
		readonlyList = null;
		failedList = null;
		heuristicList = null;

		currentHierarchy = null;
		currentStore = null;
		savedIntentionList = false;

		actionStatus = ActionStatus.CREATED;
		actionType = at;
		parentAction = null;
		recordBeingHandled = null;

		heuristicDecision = TwoPhaseOutcome.PREPARE_OK;
		_checkedAction = new CheckedAction();

		_childThreads = null;
		_childActions = null;
	}

	/**
	 * Defines the start of a critical region by setting the critical flag. If
	 * the signal handler is called the class variable abortAndExit is set. The
	 * value of this variable is checked in the corresponding operation to end
	 * the critical region.
	 */

	protected final void criticalStart ()
	{
		//	_lock.lock();
	}

	/**
	 * Defines the end of a critical region by resetting the critical flag. If
	 * the signal handler is called the class variable abortAndExit is set. The
	 * value of this variable is checked when ending the critical region.
	 */

	protected final void criticalEnd ()
	{
		//	_lock.unlock();
	}

	/**
	 * Cleanup phase for actions. If an action is in the PREPARED state when a
	 * terminate signal is delivered (ie the coordinator node has crashed) then
	 * we need to cleanup. This is essentially the same as phase2Abort but we
	 * call cleanup ops rather than abort ops and let the records take care of
	 * appropriate cleanup.
	 * 
	 * The pendingList is processed because it may not be empty - since
	 * prepare() stops processing the list at the first PREPARE_NOTOK result.
	 * 
	 * The read_only list is processed to ensure that actions are aborted
	 * immediately and any servers killed at that point since they need not hang
	 * around. This contrasts with commit where readonlyList entries are simply
	 * merged with the parent list or discarded
	 */

	protected final synchronized void phase2Cleanup ()
	{
		if (tsLogger.arjLogger.isDebugEnabled())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::phase2Cleanup() for action-id "
					+ get_uid());
		}

		criticalStart();

		actionStatus = ActionStatus.CLEANUP;

		while ((preparedList != null) && (preparedList.size() > 0))
			doCleanup(preparedList);

		while ((readonlyList != null) && (readonlyList.size() > 0))
			doCleanup(readonlyList);

		while ((pendingList != null) && (pendingList.size() > 0))
			doCleanup(pendingList);

		criticalEnd();
	}

	/**
	 * Second phase of the two-phase commit protocol for committing actions.
	 * This operation first invokes the doCommit operation on the preparedList.
	 * This ensures that the appropriate commit operation is performed on each
	 * entry which is then either deleted (top_level) or merged into the
	 * parent's pendingList.
	 * 
	 * Processing of the readonlyList is different in that if the action is
	 * top_level then all records in the readonlyList are deleted without
	 * further processing. If nested the records must be merged. This is an
	 * optimisation to avoid unnecessary processing.
	 * 
	 * Note that at this point the pendingList SHOULD be empty due to the prior
	 * invocation of prepare().
	 */

	protected synchronized final void phase2Commit (boolean reportHeuristics)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::phase2Commit() for action-id "
					+ get_uid());
		}

		if ((pendingList != null) && (pendingList.size() > 0))
		{
			int size = ((pendingList == null) ? 0 : pendingList.size());

			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_42", new Object[]
				{ get_uid(), Integer.toString(size), pendingList });
			}

			phase2Abort(reportHeuristics);
		}
		else
		{
			criticalStart();

			actionStatus = ActionStatus.COMMITTING;

			/*
			 * If we get a heuristic during commit then we continue to commit
			 * since we may have already told some records to commit. We could
			 * optimise this if the first record raises the heuristic by
			 * aborting (or going with the heuristic decision).
			 */

			doCommit(preparedList, reportHeuristics); /*
													   * process the
													   * preparedList
													   */

			/*
			 * Now check any heuristic decision. If we received one then we may
			 * have to raise HEURISTIC_MIXED since we will have committed some
			 * resources, whereas others may have aborted.
			 */

			if (heuristicDecision != TwoPhaseOutcome.PREPARE_OK)
			{
				/*
				 * Heuristic decision matched the actual outcome!
				 */

				if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_COMMIT)
					heuristicDecision = TwoPhaseOutcome.FINISH_OK;
			}

			/* The readonlyList requires special attention */

			if ((readonlyList != null) && (readonlyList.size() > 0))
			{
				while (((recordBeingHandled = readonlyList.getFront()) != null))
				{
					if ((actionType == ActionType.NESTED)
							&& (recordBeingHandled.propagateOnCommit()))
					{
						merge(recordBeingHandled);
					}
					else
					{
						recordBeingHandled = null;
					}
				}
			}

			forgetHeuristics();

			actionStatus = ActionStatus.COMMITTED;

			updateState();

			criticalEnd();
		}
	}

	/**
	 * Second phase of the two phase commit protocol for aborting actions.
	 * Actions are aborted by invoking the doAbort operation on the
	 * preparedList, the readonlyList, and the pendingList.
	 * 
	 * The pendingList is processed because it may not be empty - since
	 * prepare() stops processing the list at the first PREPARE_NOTOK result.
	 * 
	 * By default, records that responsed PREPARE_READONLY will not be contacted
	 * during second-phase abort, just as they are not during second-phase
	 * commit. This can be overridden at runtime using the READONLY_OPTIMISATION
	 * variable.
	 */

	protected synchronized final void phase2Abort (boolean reportHeuristics)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::phase2Abort() for action-id "
					+ get_uid());
		}

		criticalStart();

		actionStatus = ActionStatus.ABORTING;

		if (preparedList != null)
			doAbort(preparedList, reportHeuristics);

		if (!TxControl.readonlyOptimisation)
		{
			if (readonlyList != null)
				doAbort(readonlyList, reportHeuristics);
		}

		if (pendingList != null)
			doAbort(pendingList, reportHeuristics);

		/*
		 * Check heuristic decision, and try to make it match outcome.
		 */

		if (heuristicDecision != TwoPhaseOutcome.PREPARE_OK)
		{
			if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
				heuristicDecision = TwoPhaseOutcome.FINISH_OK;
		}

		forgetHeuristics();

		actionStatus = abortStatus();

		updateState(); // we may end up saving more than the heuristic list
					   // here!

		criticalEnd();
	}

	/**
	 * Phase one of a two phase commit protocol. This function returns the
	 * ouctome of the prepare operation. If all goes well it will be PREPARE_OK,
	 * if not PREPARE_NOTOK. The value PREPARE_READONLY may also be returned if
	 * all the records indicate that they are readonly records. Such records do
	 * not take part in the second phase commit processing.
	 * 
	 * @return <code>TwoPhaseOutcome</code> indicating outcome.
	 */

	protected synchronized final int prepare (boolean reportHeuristics)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::prepare () for action-id "
					+ get_uid());
		}

		boolean commitAllowed = (actionStatus != ActionStatus.ABORT_ONLY);

		actionStatus = ActionStatus.PREPARING;

		/* If we cannot commit - say the prepare failed */

		if (!commitAllowed)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_43", new Object[]
				{ get_uid() });
			}

			actionStatus = ActionStatus.PREPARED;

			return TwoPhaseOutcome.PREPARE_NOTOK;
		}

		/*
		 * Make sure the object store is set up for a top-level atomic action.
		 */

		if (actionType == ActionType.TOP_LEVEL)
		{
			if (store() == null)
			{
				actionStatus = ActionStatus.ABORT_ONLY;

				return TwoPhaseOutcome.PREPARE_NOTOK;
			}
		}

		criticalStart();

		createPreparedLists();

		/*
		 * Here is the start of the hard work. Walk down the pendingList
		 * invoking the appropriate prepare operation. If it succeeds put the
		 * record on either the preparedList or the read_only list and continue
		 * until the pendingList is exhausted.
		 * 
		 * If prepare fails on any record stop processing immediately and put
		 * the offending record back on the pendingList
		 */

		int p = TwoPhaseOutcome.PREPARE_OK;

		/*
		 * If asynchronous prepare, then spawn a separate thread to handle each
		 * entry in the intentions list. Could have some configurable option to
		 * allow more limited number of threads to divide up the intentions
		 * list.
		 */

		if ((actionType == ActionType.TOP_LEVEL) && (TxControl.asyncPrepare))
		{
			int numberOfThreads = ((pendingList != null) ? pendingList.size()
					: 0);
			Thread[] threads = new Thread[numberOfThreads];
			int i;

			/*
			 * First create them in a suspended way, so that we can purge the
			 * list before it is added to (in the event of failures!)
			 */

			for (i = 0; i < numberOfThreads; i++)
			{
				threads[i] = AsyncPrepare.create(this, reportHeuristics, pendingList.getFront());
			}

			/*
			 * Now start the threads running.
			 */

			for (i = 0; i < numberOfThreads; i++)
			{
				threads[i].start();
				Thread.yield();
			}

			/*
			 * If one of these threads fails (PREPARE_NOTOK) do we terminate the
			 * others or simply let them finish? Currently we wait and let them
			 * all terminate regardless.
			 */

			/*
			 * Now synchronise with the threads.
			 */

			for (int j = 0; j < numberOfThreads; j++)
			{
				while (threads[j].isAlive())
				{
					try
					{
						threads[j].join();
					}
					catch (Exception e)
					{
						if (tsLogger.arjLogger.isWarnEnabled())
						{
							tsLogger.arjLogger.warn("BasicAction.prepare - "
									+ e);
						}

						p = TwoPhaseOutcome.PREPARE_NOTOK;
					}
				}

				/*
				 * Only set the outcome if the current value is PREPARE_OK.
				 */

				if (p == TwoPhaseOutcome.PREPARE_OK)
					p = ((AsyncPrepare) threads[j]).outcome();

				threads[j] = null;
			}
		}
		else
		{
			boolean ok = true;

			do
			{
				try
				{
					p = doPrepare(reportHeuristics);

					/*
					 * Stop as soon as we get a failure return code.
					 */

					if ((p != TwoPhaseOutcome.PREPARE_OK)
							&& (p != TwoPhaseOutcome.PREPARE_READONLY))
					{
						ok = false;
					}
				}
				catch (IndexOutOfBoundsException e)
				{
					// end of list!

					ok = false;
				}

			} while (ok);
		}

		if ((p != TwoPhaseOutcome.PREPARE_OK)
				&& (p != TwoPhaseOutcome.PREPARE_READONLY))
		{
			if ((actionType == ActionType.NESTED)
					&& ((preparedList.size() > 0) && (p == TwoPhaseOutcome.ONE_PHASE_ERROR)))
			{
				/*
				 * For the OTS we must merge those records told to commit with
				 * the parent, as the rollback invocation must come from that
				 * since they have already been told this transaction has
				 * committed!
				 */

				AbstractRecord tmpRec = preparedList.getFront();

				while (tmpRec != null)
				{
					merge(tmpRec);
					tmpRec = preparedList.getFront();
				}

				if (parentAction != null)
					parentAction.preventCommit();
				else
				{
					if (tsLogger.arjLoggerI18N.isWarnEnabled())
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_44");
				}
			}

			criticalEnd();

			return TwoPhaseOutcome.PREPARE_NOTOK;
		}

		/*
		 * Now work out whether there is any state to save. Since we should be
		 * single threaded once again, there is no need to protect the lists
		 * with a synchronization.
		 */

		/*
		 * Could do this as we traverse the lists above, but would need some
		 * compound class for return values.
		 */

		boolean stateToSave = false;
		RecordListIterator iter = new RecordListIterator(preparedList);

		/*
		 * First check the prepared list.
		 */

		while (((recordBeingHandled = iter.iterate()) != null))
		{
			if (!stateToSave)
				stateToSave = recordBeingHandled.doSave();

			if (stateToSave)
				break;
		}

		iter = null;

		if (!stateToSave)
		{
			iter = new RecordListIterator(heuristicList);

			/*
			 * Now check the heuristic list.
			 */

			while (((recordBeingHandled = heuristicList.getFront()) != null))
			{
				if (!stateToSave)
					stateToSave = recordBeingHandled.doSave();

				if (stateToSave)
					break;
			}

			iter = null;
		}

		/*
		 * The actual state we want to write depends upon whether or not we are
		 * in charge of the transaction outcome:
		 * 
		 * (i) if we are a root transaction, or an interposed transaction which
		 * received a commit_one_phase call, then we have complete control over
		 * what the transaction outcome will be. So, we will always try to
		 * commit, and can set the state to committing.
		 * 
		 * (ii) if we are an interposed transaction and it receives a complete
		 * two-phase protocol, then the root is in control. So, we set the state
		 * to prepared.
		 * 
		 * (iii) nested transactions never write state, so the state is set to
		 * prepared anyway.
		 */

		if (actionType == ActionType.TOP_LEVEL)
			actionStatus = preparedStatus();
		else
			actionStatus = ActionStatus.PREPARED;

		/*
		 * If we are here then everything went okay so save the intention list
		 * in the ObjectStore in case of a node crash providing that its not
		 * empty
		 */

		if ((actionType == ActionType.TOP_LEVEL) && (stateToSave)
				&& ((preparedList.size() > 0) || (heuristicList.size() > 0)))
		{
			/* Only do this if we have some records worth saving! */

			Uid u = getSavingUid();
			String tn = type();
			OutputObjectState state = new OutputObjectState(u, tn);

			if (!save_state(state, ObjectType.ANDPERSISTENT))
			{
				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_45", new Object[]
					{ get_uid() });
				}

				criticalEnd();

				return TwoPhaseOutcome.PREPARE_NOTOK;
			}

			if (state.notempty())
			{
				try
				{				
					if (!currentStore.write_committed(u, tn, state))
					{
						if (tsLogger.arjLoggerI18N.isWarnEnabled())
						{
							tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_46", new Object[]
							{ get_uid() });
						}

						criticalEnd();

						return TwoPhaseOutcome.PREPARE_NOTOK;
					}
					else
						savedIntentionList = true;
				}
				catch (ObjectStoreException e)
				{
					criticalEnd();

					return TwoPhaseOutcome.PREPARE_NOTOK;
				}
			}
		}

		criticalEnd();

		if ((preparedList.size() == 0) && (readonlyList.size() >= 0))
			return TwoPhaseOutcome.PREPARE_READONLY;
		else
			return TwoPhaseOutcome.PREPARE_OK;
	}

	/**
	 * There is only one record on the intentions list. Only called from
	 * synchronized methods. Don't bother about creating separate threads here!
	 */

	protected void onePhaseCommit (boolean reportHeuristics)
	{
		if (tsLogger.arjLogger.isDebugEnabled())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::onePhaseCommit() for action-id "
					+ get_uid());
		}

		/* Are we forced to abort? */

		if (actionStatus == ActionStatus.ABORT_ONLY)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_43", new Object[]
				{ get_uid() });
			}

			Abort();

			return;
		}

		actionStatus = ActionStatus.COMMITTING;

		criticalStart();

		if ((heuristicList == null) && reportHeuristics)
			heuristicList = new RecordList(); // the only list we'll need.

		/*
		 * Since it is one-phase, the outcome from the record is the outcome of
		 * the transaction. Therefore, we don't need to save much intermediary
		 * transaction state - only heuristics in the case of interposition.
		 */

		boolean stateToSave = false;

		recordBeingHandled = pendingList.getFront();
	
		int p = ((actionType == ActionType.TOP_LEVEL) ? recordBeingHandled.topLevelOnePhaseCommit()
				: recordBeingHandled.nestedOnePhaseCommit());
	
		if ((p == TwoPhaseOutcome.FINISH_OK)
				|| (p == TwoPhaseOutcome.PREPARE_READONLY))
		{
			if ((actionType == ActionType.NESTED)
					&& recordBeingHandled.propagateOnCommit())
			{
				merge(recordBeingHandled);
			}
			else
			{
				recordBeingHandled = null;
			}

			actionStatus = ActionStatus.COMMITTED;
		}
		else
		{
			// aborted or heuristic which we aren't interested in

			if (p == TwoPhaseOutcome.FINISH_ERROR)
			{
				/*
				 * Don't bother about the failedList - we are aborting, and
				 * there is only one record!
				 */

				recordBeingHandled = null;

				actionStatus = ActionStatus.ABORTED;
			}
			else
			{
				/*
				 * Heuristic decision!!
				 */

				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_47", new Object[]
					{ get_uid(), TwoPhaseOutcome.stringForm(p) });
				}

				if (reportHeuristics)
				{
					updateHeuristic(p, true);

					if (!heuristicList.insert(recordBeingHandled))
						recordBeingHandled = null;
					else
					{
						if (!stateToSave)
							stateToSave = recordBeingHandled.doSave();
					}
				}

				if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
				{
					/*
					 * Signal that the action outcome is the same as the
					 * heuristic decision.
					 */

					heuristicDecision = TwoPhaseOutcome.PREPARE_OK; // means no
																	// heuristic
																	// was
																	// raised.

					actionStatus = ActionStatus.ABORTED;
				}
				else if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_COMMIT)
				{
					heuristicDecision = TwoPhaseOutcome.PREPARE_OK;
					actionStatus = ActionStatus.COMMITTED;
				}
				else
					actionStatus = ActionStatus.COMMITTED; // can't really say
														   // (could have
														   // aborted)
			}
		}

		if (actionType == ActionType.TOP_LEVEL)
		{			
			if (stateToSave && (heuristicList.size() > 0))
			{
				if (store() == null)
				{
					if (tsLogger.arjLoggerI18N.isFatalEnabled())
					{
						tsLogger.arjLoggerI18N.fatal("com.arjuna.ats.arjuna.coordinator.BasicAction_48");
					}

					throw new com.arjuna.ats.arjuna.exceptions.FatalError(
							tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.coordinator.BasicAction_69")
									+ get_uid());
				}

				updateState();
			}
		}

		forgetHeuristics();

		criticalEnd();
	}

	/**
	 * @return the current heuristic decision. Each time a heuristic outcome is
	 *         received, we need to merge it with any previous outcome to
	 *         determine what the overall heuristic decision is (e.g., a
	 *         heuristic rollback followed by a heuristic commit means the
	 *         overall decision is heuristic mixed.)
	 */

	protected final synchronized int getHeuristicDecision ()
	{
		return heuristicDecision;
	}

	/**
	 * Add the specified abstract record to the transaction. Does not do any of
	 * the runtime checking of BasicAction.add, so should be used with care.
	 * Currently used by crash recovery.
	 */

	protected final synchronized void addRecord (AbstractRecord A)
	{
		preparedList.insert(A);
	}

	/**
	 * @return the transaction's prepared status.
	 * 
	 * @since JTS 2.0.
	 */

	protected int preparedStatus ()
	{
		if (actionType == ActionType.TOP_LEVEL)
			return ActionStatus.COMMITTING;
		else
			return ActionStatus.PREPARED;
	}

	protected int abortStatus ()
	{
		return ActionStatus.ABORTED;
	}

	protected int commitStatus ()
	{
		return ActionStatus.COMMITTED;
	}

	/*
	 * Package visibility.
	 */

	/*
	 * The single-threaded version of doPrepare. If we do not use asynchronous
	 * prepare, then we don't need to lock the RecordLists - only one thread can
	 * access them anyway!
	 */

	/*
	 * If we do not use asynchronous prepare, then we don't need to lock the
	 * RecordLists - only one thread can access them anyway! However, if
	 * asynchronous prepare is being used then we need to synchronize on them.
	 * The 'lock' parameter (default false) indicates whether we should try to
	 * synchronize the RecordList or not.
	 */

	protected int doPrepare (boolean reportHeuristics)
			throws IndexOutOfBoundsException
	{
		/*
		 * Here is the start of the hard work. Walk down the pendingList
		 * invoking the appropriate prepare operation. If it succeeds put the
		 * record on either the preparedList or the read_only list and continue
		 * until the pendingList is exhausted.
		 * 
		 * If prepare fails on any record stop processing immediately and put
		 * the offending record back on the pendingList.
		 */

		int p = TwoPhaseOutcome.PREPARE_NOTOK;

		do
		{
			AbstractRecord rec = ((pendingList != null) ? pendingList.getFront()
					: null);

			/*
			 * March down the pendingList and pass the head of the list to the
			 * main work routine until either we run out of elements, or one of
			 * them fails.
			 */

			if (rec != null)
			{
				/*
				 * If a failure occurs then the record will be put back on to
				 * the pending list. Otherwise it is moved to another list or
				 * dropped if readonly.
				 */

				p = doPrepare(reportHeuristics, rec);

				/*
				 * If failure then it's back on the pending list by now. Move on
				 * or terminate?
				 * 
				 * Move on if a cohesion, but terminate if an atom!!
				 */
			}
			else
				throw new IndexOutOfBoundsException();

		} while ((p == TwoPhaseOutcome.PREPARE_OK)
				|| (p == TwoPhaseOutcome.PREPARE_READONLY));

		return p;
	}

	/*
	 * The multi-threaded version of doPrepare. Each thread was given the record
	 * it should process when it was created so that if a failure occurs we can
	 * put it back onto the pendingList at the right place. It also cuts down on
	 * the amount of synchronisation we must do.
	 */

	protected int doPrepare (boolean reportHeuristics, AbstractRecord theRecord)
			throws IndexOutOfBoundsException
	{
		/*
		 * Here is the start of the hard work. Walk down the pendingList
		 * invoking the appropriate prepare operation. If it succeeds put the
		 * record on either the preparedList or the read_only list and continue
		 * until the pendingList is exhausted.
		 * 
		 * If prepare fails on any record stop processing immediately and put
		 * the offending record back on the pendingList.
		 */

		int p = TwoPhaseOutcome.PREPARE_NOTOK;
		AbstractRecord record = theRecord;

		if (record != null)
		{
			p = ((actionType == ActionType.TOP_LEVEL) ? record.topLevelPrepare()
					: record.nestedPrepare());

			if (p == TwoPhaseOutcome.PREPARE_OK)
			{
				record = insertRecord(preparedList, record);
			}
			else
			{
				if (p == TwoPhaseOutcome.PREPARE_READONLY)
				{
					record = insertRecord(readonlyList, record);
				}
				else
				{
					if ((p == TwoPhaseOutcome.PREPARE_NOTOK)
							|| (p == TwoPhaseOutcome.ONE_PHASE_ERROR)
							|| (!reportHeuristics))
					{
						/*
						 * If we are a subtransaction and this is an OTS
						 * resource then we may be in trouble: we may have
						 * already told other records to commit.
						 */

						if (actionType == ActionType.NESTED)
						{
							if ((preparedList.size() > 0)
									&& (p == TwoPhaseOutcome.ONE_PHASE_ERROR))
							{
								if (tsLogger.arjLoggerI18N.isWarnEnabled())
								{
									tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_49", new Object[]
									{ get_uid() });
								}

								/*
								 * Force parent to rollback. If this is not the
								 * desired result then we may need to check some
								 * environment variable (either here or in the
								 * OTS) and act accordingly. If we check in the
								 * OTS then we need to return something other
								 * than PREPARE_NOTOK.
								 */

								/*
								 * For the OTS we must merge those records told
								 * to commit with the parent, as the rollback
								 * invocation must come from that since they
								 * have already been told this transaction has
								 * committed!
								 * 
								 * However, since we may be multi-threaded
								 * (asynchronous prepare) we don't do the
								 * merging yet. Wait until all threads have
								 * terminated and then do it.
								 * 
								 * Therefore, can't force parent to rollback
								 * state at present, or merge will fail.
								 */
							}
						}

						/*
						 * Prepare on this record failed - we are in trouble.
						 * Add the record back onto the pendingList and return.
						 */

						record = insertRecord(pendingList, record);

						record = null;

						actionStatus = ActionStatus.PREPARED;

						return p;
					}
					else
					{
						/*
						 * Heuristic decision!!
						 */

						/*
						 * Only report if request to do so.
						 */

						if (tsLogger.arjLoggerI18N.isWarnEnabled())
						{
							tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_50", new Object[]
							{ get_uid(), TwoPhaseOutcome.stringForm(p) });
						}

						if (reportHeuristics)
							updateHeuristic(p, false);

						/*
						 * Don't add to the prepared list. We process heuristics
						 * separately during phase 2. The processing of records
						 * will not be in the same order as during phase 1, but
						 * does this matter for heuristic decisions? If so, then
						 * we need to modify RecordList so that records can
						 * appear on multiple lists at the same time.
						 */

						record = insertRecord(heuristicList, record);

						/*
						 * If we have had a heuristic decision, then attempt to
						 * make the action outcome the same. If we have a
						 * conflict, then we will abort.
						 */

						if (heuristicDecision != TwoPhaseOutcome.HEURISTIC_COMMIT)
						{
							actionStatus = ActionStatus.PREPARED;

							return TwoPhaseOutcome.PREPARE_NOTOK;
						}
						else
						{
							/*
							 * Heuristic commit, which is ok since we want to
							 * commit anyway! So, ignore it (but remember the
							 * resource so we can tell it to forget later.)
							 */
						}
					}
				}
			}
		}
		else
			// no more entries in pendingList
			throw new IndexOutOfBoundsException();

		return p;
	}

	/**
	 * Walk down a record list extracting records and calling the appropriate
	 * commit function. Discard or merge records as appropriate
	 */

	protected int doCommit (RecordList rl, boolean reportHeuristics)
	{
		if ((rl != null) && (rl.size() > 0))
		{
			AbstractRecord rec;
			boolean pastFirstParticipant = false;

			while (((rec = rl.getFront()) != null))
			{
				int outcome = doCommit(reportHeuristics, rec);

				/*
				 * Check the outcome and if we have a heuristic rollback try to
				 * rollback everything else in the list *if* we have not already
				 * committed something. That way we make the outcome for all
				 * participants the same as the first (rollback) and don't get a
				 * heuristic!
				 */

				switch (outcome)
				{
				case TwoPhaseOutcome.FINISH_OK:
				case TwoPhaseOutcome.HEURISTIC_COMMIT:
					pastFirstParticipant = true;
					break;
				case TwoPhaseOutcome.HEURISTIC_MIXED:
				case TwoPhaseOutcome.HEURISTIC_HAZARD:
				default:
					/*
					 * Do nothing and continue to commit everything else. We've
					 * got this far as errors have caused problems, but we gain
					 * nothing by now rolling back some participants. This could
					 * cause further heuristics!
					 */

					pastFirstParticipant = true;
					break;
				case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
				{
					/*
					 * A heuristic decision of commit means that we have got
					 * past the first entry in the list. So there is no going
					 * back now!
					 */

					if (pastFirstParticipant)
						break;
					else
					{
						/*
						 * Remember the heuristic decision so we can restore it
						 * after rolling back. Otherwise we can't return the
						 * right value from commit.
						 */

						pastFirstParticipant = true;

						int oldDecision = heuristicDecision;

						phase2Abort(reportHeuristics);

						heuristicDecision = oldDecision;
					}
				}
					break;
				}
			}
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	protected int doCommit (boolean reportHeuristics, AbstractRecord record)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::doCommit ("
					+ record + ")");
		}

		/*
		 * To get heuristics right, as soon as we manage to commit the first
		 * record we set the heuristic to HEURISTIC_COMMIT. Then, if any other
		 * heuristics are raised we can manage the final outcome correctly.
		 */

		int ok = TwoPhaseOutcome.FINISH_ERROR;

		recordBeingHandled = record;

		if (recordBeingHandled != null)
		{
			if (actionType == ActionType.TOP_LEVEL)
			{
				if ((ok = recordBeingHandled.topLevelCommit()) == TwoPhaseOutcome.FINISH_OK)
				{
					/*
					 * Record successfully committed, we can delete it now.
					 */

					recordBeingHandled = null;

					updateHeuristic(TwoPhaseOutcome.FINISH_OK, true); // must
																	  // remember
																	  // that
																	  // something
																	  // has
																	  // committed
				}
				else
				{				
					if (tsLogger.arjLoggerI18N.debugAllowed(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, FacilityCode.FAC_ATOMIC_ACTION))
					{
						tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_51", new Object[]
						{ get_uid(), TwoPhaseOutcome.stringForm(ok), RecordType.typeToClassName(recordBeingHandled.typeIs()) });
					}

					if ((reportHeuristics)
							&& ((ok == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
									|| (ok == TwoPhaseOutcome.HEURISTIC_COMMIT)
									|| (ok == TwoPhaseOutcome.HEURISTIC_MIXED) || (ok == TwoPhaseOutcome.HEURISTIC_HAZARD)))
					{
						updateHeuristic(ok, true);
						heuristicList.insert(recordBeingHandled);
					}
					else
					{
						if (ok == TwoPhaseOutcome.NOT_PREPARED)
						{
							/*
							 * If this is the first resource then rollback,
							 * otherwise promote to HEURISTIC_HAZARD, but don't
							 * add to heuristicList.
							 */

							updateHeuristic(TwoPhaseOutcome.HEURISTIC_HAZARD, true);
						}
						else
						{
							/*
							 * The commit failed. Add this record to the failed
							 * list to indicate this.
							 */

							failedList.insert(recordBeingHandled);
						}
					}
				}
			}
			else
			{
				/*
				 * Thankfully nested actions cannot raise heuristics!
				 */

				ok = recordBeingHandled.nestedCommit();

				if (recordBeingHandled.propagateOnCommit())
				{
					merge(recordBeingHandled);
				}
				else
				{
					recordBeingHandled = null;
				}
			}

			if (ok != TwoPhaseOutcome.FINISH_OK)
			{
				/* Preserve error messages */
			}
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	/*
	 * Walk down a record list extracting records and calling the appropriate
	 * abort function. Discard records when done.
	 */

	protected int doAbort (RecordList list_toprocess, boolean reportHeuristics)
	{
		if ((list_toprocess != null) && (list_toprocess.size() > 0))
		{
			while ((recordBeingHandled = list_toprocess.getFront()) != null)
			{
				doAbort(reportHeuristics, recordBeingHandled);
			}
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	protected int doAbort (boolean reportHeuristics, AbstractRecord record)
	{
		if (tsLogger.arjLogger.isDebugEnabled())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::doAbort ("
					+ record + ")");
		}

		int ok = TwoPhaseOutcome.FINISH_OK;

		recordBeingHandled = record;

		if (recordBeingHandled != null)
		{
			if (actionType == ActionType.TOP_LEVEL)
				ok = recordBeingHandled.topLevelAbort();
			else
				ok = recordBeingHandled.nestedAbort();

			if ((actionType != ActionType.TOP_LEVEL)
					&& (recordBeingHandled.propagateOnAbort()))
			{
				merge(recordBeingHandled);
			}
			else
			{
				if (ok == TwoPhaseOutcome.FINISH_OK)
				{
					updateHeuristic(TwoPhaseOutcome.FINISH_OK, false); // remember
																	   // that
																	   // something
																	   // aborted
																	   // ok
				}
				else
				{
					if ((reportHeuristics)
							&& ((ok == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
									|| (ok == TwoPhaseOutcome.HEURISTIC_COMMIT)
									|| (ok == TwoPhaseOutcome.HEURISTIC_MIXED) || (ok == TwoPhaseOutcome.HEURISTIC_HAZARD)))
					{
						if (tsLogger.arjLoggerI18N.isWarnEnabled())
						{
							if (actionType == ActionType.TOP_LEVEL)
								tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_52", new Object[]
								{ get_uid(), TwoPhaseOutcome.stringForm(ok) });
							else
								tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_53", new Object[]
								{ get_uid(), TwoPhaseOutcome.stringForm(ok) });
						}

						updateHeuristic(ok, false);
						heuristicList.insert(recordBeingHandled);
					}
					else
					{
						if (ok != TwoPhaseOutcome.FINISH_OK)
						{
							if (tsLogger.arjLoggerI18N.isWarnEnabled())
							{
								if (actionType == ActionType.TOP_LEVEL)
									tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_54", new Object[]
									{ get_uid(), TwoPhaseOutcome.stringForm(ok), RecordType.typeToClassName(recordBeingHandled.typeIs()) });
								else
									tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_55", new Object[]
									{ get_uid(), TwoPhaseOutcome.stringForm(ok), RecordType.typeToClassName(recordBeingHandled.typeIs()) });
							}
						}
					}
				}

				/*
				 * Don't need a canDelete as in the C++ version since Java's
				 * garbage collection will deal with things for us.
				 */

				recordBeingHandled = null;
			}
		}

		return ok;
	}

	protected AbstractRecord insertRecord (RecordList reclist, AbstractRecord record)
	{
		boolean lock = TxControl.asyncPrepare;

		if (lock)
		{
			synchronized (reclist)
			{
				if (!reclist.insert(record))
					record = null;
			}
		}
		else
		{
			if (!reclist.insert(record))
				record = null;
		}

		return record;
	}

	/**
	 * Do we want to check that a transaction can only be terminated by a thread
	 * that has it as its current transaction? The base class has this check
	 * enabled (i.e., we check), but some implementations may wish to override
	 * this.
	 * 
	 * @return <code>false</code> to disable checking.
	 */

	protected boolean checkForCurrent ()
	{
		return false;
	}

	/*
	 * If we get a single heuristic then we will always rollback during prepare.
	 * 
	 * Getting a heuristic during commit is slightly different, since some
	 * resources may have already committed, changing the type of heuristic we
	 * may need to throw. However, once we get to commit we know that it will be
	 * the final outcome. So, as soon as a single resource commits successfully,
	 * we can take it as a HEURISTIC_COMMIT. We will forget a HEURISTIC_COMMIT
	 * outcome at the end anyway.
	 */

	protected final synchronized void updateHeuristic (int p, boolean commit)
	{
		/*
		 * Some resource has prepared/committed ok, so we need to remember this
		 * in case we get a future heuristic.
		 */

		if (p == TwoPhaseOutcome.FINISH_OK)
		{
			if (commit)
			{
				if (heuristicDecision == TwoPhaseOutcome.PREPARE_OK)
					p = TwoPhaseOutcome.HEURISTIC_COMMIT;

				if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
					heuristicDecision = TwoPhaseOutcome.HEURISTIC_MIXED;
			}
			else
			{
				if (heuristicDecision == TwoPhaseOutcome.PREPARE_OK)
					p = TwoPhaseOutcome.HEURISTIC_ROLLBACK;

				if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_COMMIT)
					heuristicDecision = TwoPhaseOutcome.HEURISTIC_MIXED;
			}

			// leave HAZARD and MIXED alone
		}

		/*
		 * Is this the first heuristic? Always give HEURISTIC_MIXED priority,
		 * but if we have no heuristic and we get a HEURISTIC_HAZARD then go
		 * with that until something better comes along!
		 */

		/*
		 * Have we already been given a conflicting heuristic? If so, raise the
		 * decision to the next heuristic level.
		 */

		switch (heuristicDecision)
		{
		case TwoPhaseOutcome.PREPARE_OK:
			if ((p != TwoPhaseOutcome.PREPARE_OK)
					&& (p != TwoPhaseOutcome.FINISH_OK)) // first heuristic
														 // outcome.
				heuristicDecision = p;
			break;
		case TwoPhaseOutcome.HEURISTIC_COMMIT:
			if ((p == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
					|| (p == TwoPhaseOutcome.HEURISTIC_MIXED))
				heuristicDecision = TwoPhaseOutcome.HEURISTIC_MIXED;
			else
			{
				if (p == TwoPhaseOutcome.HEURISTIC_HAZARD)
					heuristicDecision = TwoPhaseOutcome.HEURISTIC_HAZARD;
			}
			break;
		case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
			if ((p == TwoPhaseOutcome.HEURISTIC_COMMIT)
					|| (p == TwoPhaseOutcome.HEURISTIC_MIXED))
				heuristicDecision = TwoPhaseOutcome.HEURISTIC_MIXED;
			else
			{
				if (p == TwoPhaseOutcome.HEURISTIC_HAZARD)
					heuristicDecision = TwoPhaseOutcome.HEURISTIC_HAZARD;
			}
			break;
		case TwoPhaseOutcome.HEURISTIC_HAZARD:
			if (p == TwoPhaseOutcome.HEURISTIC_MIXED)
				heuristicDecision = TwoPhaseOutcome.HEURISTIC_MIXED;
			break;
		case TwoPhaseOutcome.HEURISTIC_MIXED:
			break;
		default:
			heuristicDecision = p; // anything!
			break;
		}

		if (TxControl.enableStatistics)
			TxStats.incrementHeuristics();
	}

	protected void updateState ()
	{
		if (tsLogger.arjLogger.isDebugEnabled())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::updateState() for action-id "
					+ get_uid());
		}

		/*
		 * If the action is topLevel then prepare() will have written the
		 * intention_list to the object_store. If any of the phase2Commit
		 * processing failed then records will exist on the failedList. If this
		 * is the case then we need to re-write the BasicAction record in the
		 * object store. If the failed list is empty we can simply delete the
		 * BasicAction record.
		 */

		if (actionType == ActionType.TOP_LEVEL)
		{
			/*
			 * make sure the object store is set up for a top-level atomic
			 * action.
			 */

			store();

			/*
			 * If we have failures then rewrite the intentions list. Otherwise,
			 * delete the log entry.
			 */

			if (((failedList != null) && (failedList.size() > 0))
					|| ((heuristicList != null) && (heuristicList.size() > 0)))
			{
				/*
				 * Re-write the BasicAction record with the failed list
				 */

				Uid u = getSavingUid();
				String tn = type();
				OutputObjectState state = new OutputObjectState(u, tn);

				if (!save_state(state, ObjectType.ANDPERSISTENT))
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_64");

					// what else?
				}

				if (state.notempty())
				{
					try
					{
						if (!currentStore.write_committed(u, tn, state))
						{
							tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_65");
						}
					}
					catch (ObjectStoreException e)
					{
						tsLogger.arjLogger.warn(e.getMessage());
					}
				}
			}
			else
			{
				try
				{
					if (savedIntentionList)
					{						
						if (currentStore.remove_committed(getSavingUid(), type()))
						{
							savedIntentionList = false;
						}
					}
				}
				catch (ObjectStoreException e)
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_70", new Object[]
					{ e });
				}
			}
		}
	}

	/*
	 * This is only meant as an instance cut of the children, so don't lock the
	 * entire transaction. Thus, the list may change before we return.
	 */

	private final void createPreparedLists ()
	{
		if (preparedList == null)
			preparedList = new RecordList();

		if (readonlyList == null)
			readonlyList = new RecordList();

		if (failedList == null)
			failedList = new RecordList();

		if (heuristicList == null)
			heuristicList = new RecordList();

		if (pendingList == null)
			pendingList = new RecordList();
	}

	/**
	 * Check to see if this transaction is the one that is current for this
	 * thread. If it isn't, then we mark this transaction as rollback only.
	 * 
	 * @return <code>true</code> if the transaction is current,
	 *         <code>false</code> otherwise.
	 */

	private final boolean checkIsCurrent ()
	{
		boolean isCurrent = true;

		if (checkForCurrent())
		{
			BasicAction currentAct = BasicAction.Current();

			/* Ensure I am the currently active action */

			if ((currentAct != null) && (currentAct != this))
			{
				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_56", new Object[]
					{ currentAct.get_uid(), get_uid() });
				}

				isCurrent = false;

				if (currentAct.isAncestor(get_uid()))
				{
					/* current action is one of my children */

					BasicAction parentAct = parent();

					/* prevent commit of my parents (ensures safety) */

					while (parentAct != null)
					{
						parentAct.preventCommit();
						parentAct = parentAct.parent();
					}
				}
			}

			currentAct = null;
		}

		return isCurrent;
	}

	private final boolean checkChildren (boolean isCommit)
	{
		boolean problem = false;

		/*
		 * If we have child threads then by default we just print a warning and
		 * continue. The other threads will eventually find out the outcome.
		 */

		if ((_childThreads != null) && (_childThreads.size() > 0))
		{
			if ((_childThreads.size() != 1)
					|| ((_childThreads.size() == 1) && (!_childThreads.contains(Thread.currentThread()))))
			{
				/*
				 * More than one thread or the one thread is not the current
				 * thread
				 */

				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
					if (isCommit)
					{
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_57", new Object[]
						{ get_uid() });
					}
					else
					{
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_58", new Object[]
						{ get_uid() });
					}
				}

				if (_checkedAction != null)
					_checkedAction.check(isCommit, get_uid(), _childThreads);

				removeAllChildThreads();
			}
		}

		/* Ensure I have no child actions */

		if ((_childActions != null) && (_childActions.size() > 0))
		{
			problem = true;

			Enumeration iter = _childActions.elements();
			BasicAction child = null;
			boolean printError = true;

			/*
			 * We may have already aborted our children, e.g., because of an
			 * out-of-sequence commit, so we check here to reduce the number of
			 * error messages!
			 * 
			 * We can't just remove the children when we are finished with them
			 * because BasicAction is not responsible for action tracking.
			 */

			while (iter.hasMoreElements())
			{
				child = (BasicAction) iter.nextElement();

				if (child.status() != ActionStatus.ABORTED)
				{
					if (printError)
					{
						if (tsLogger.arjLoggerI18N.isWarnEnabled())
						{
							if (isCommit)
							{
								tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_59", new Object[]
								{ get_uid() });
							}
							else
							{
								tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_60", new Object[]
								{ get_uid() });
							}
						}

						printError = false;
					}

					if (tsLogger.arjLoggerI18N.isWarnEnabled())
					{
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_61", new Object[]
						{ child.get_uid() });
					}

					child.Abort();
					child = null;
				}
			}

			iter = null;

			if (isCommit)
			{
				if (tsLogger.arjLoggerI18N.isWarnEnabled())
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_62", new Object[]
					{ child.get_uid() });
				}
			}
		}

		return problem;
	}

	/*
	 * Just in case we are deleted/terminated with threads still registered. We
	 * must make sure those threads don't try to remove themselves from this
	 * action later. So we unregister them ourselves now.
	 * 
	 * This is only called by End/Abort and so all child actions will have been
	 * previously terminated as well.
	 */

	private final void removeAllChildThreads ()
	{
		/*
		 * Do not remove the current thread as it is committing/aborting!
		 */

		criticalStart();

		if ((_childThreads != null) && (_childThreads.size() != 0))
		{
			Thread currentThread = Thread.currentThread();

			/*
			 * Iterate through all registered threads and tell them to ignore
			 * the action pointer, i.e., they are now no longer within this
			 * action.
			 */

			Enumeration iter = _childThreads.elements();
			Thread t = null;

			while (iter.hasMoreElements())
			{
				t = (Thread) iter.nextElement();

				if (tsLogger.arjLoggerI18N.debugAllowed())
				{
					tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_63", new Object[]
					{ get_uid(), t });
				}

				if (t != currentThread)
					ThreadActionData.purgeAction(this, t);
			}
		}

		criticalEnd();
	}

	/**
	 * actionInitialise determines whether the BasicAction is a nested,
	 * top-level, or a top-level nested atomic action
	 */

	private final void actionInitialise (BasicAction parent)
	{
		if (tsLogger.arjLogger.isDebugEnabled())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::actionInitialise() for action-id "
					+ get_uid());
		}

		criticalStart();

		if (parent != null) /* ie not top_level */
		{
			if (tsLogger.arjLoggerI18N.debugAllowed())
			{
				tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_66", new Object[]
				{ get_uid(), Integer.toString(parent.actionStatus) });
			}

			currentHierarchy = new ActionHierarchy(parent.getHierarchy());
		}
		else
		{
			currentHierarchy = new ActionHierarchy(
					ActionHierarchy.DEFAULT_HIERARCHY_DEPTH);

			/*
			 * This is a top-level atomic action so set the signal handler block
			 * a number of signals.
			 */
		}

		currentHierarchy.add(get_uid(), actionType);

		switch (actionType)
		{
		case ActionType.TOP_LEVEL:
			if (parent != null)
			{
				/*
				 * do not want to print warning all the time as this is what
				 * nested top-level actions are used for.
				 */

				if (tsLogger.arjLoggerI18N.debugAllowed())
				{
					tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, FacilityCode.FAC_ATOMIC_ACTION, "com.arjuna.ats.arjuna.coordinator.BasicAction_67", new Object[]
					{ get_uid(), parent.get_uid() });
				}
			}
			break;
		case ActionType.NESTED:
			if (parent == null)
				actionType = ActionType.TOP_LEVEL;
			break;
		}

		parentAction = parent;

		criticalEnd();
	}

	private final void doForget (RecordList list_toprocess)
	{
		if (tsLogger.arjLogger.isDebugEnabled())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::doForget ("
					+ list_toprocess + ")");
		}

		/*
		 * If the user has selected to maintain all heuristic information, then
		 * we never explicitly tell resources to forget. We assume that the user
		 * (or some management tool) will do this, and simply save as much
		 * information as we can into the action state to allow them to do so.
		 * 
		 * However, if we had a resource that returned a heuristic outcome and
		 * we managed to make the outcome of this transaction the same as that
		 * outcome, we removed the heuristic. So, we need to tell the resource
		 * regardless, or it'll never be able to tidy up.
		 */

		boolean force = (boolean) (heuristicDecision == TwoPhaseOutcome.FINISH_OK);

		if (!TxControl.maintainHeuristics || force)
		{
			if (list_toprocess.size() > 0)
			{
				RecordList tmpList = new RecordList();

				while (((recordBeingHandled = list_toprocess.getFront())) != null)
				{
					/*
					 * Remember for later if we cannot tell it to forget.
					 */

					if (recordBeingHandled.forgetHeuristic())
						recordBeingHandled = null;
					else
						tmpList.putFront(recordBeingHandled);
				}

				/*
				 * Now put those resources we couldn't tell to forget back on
				 * the heuristic list.
				 */

				if (tmpList.size() > 0)
				{
					while ((recordBeingHandled = tmpList.getFront()) != null)
						list_toprocess.putFront(recordBeingHandled);
				}
			}
		}
	}

	/*
	 * Walk down a record list extracting records and calling the appropriate
	 * cleanup function. Discard records when done. NOTE: We only need to do
	 * cleanup at top level since cleanup at nested level would be subsumed when
	 * the parent action is forced to abort
	 * 
	 * Ignore heuristics. Who can we report them to?
	 * 
	 * This routine is called by phase2Cleanup, which gets called only in
	 * exceptional circumstances. By default we leave cleaning up the various
	 * lists until the action instance goes out of scope.
	 */

	private final void doCleanup (RecordList list_toprocess)
	{
		if (tsLogger.arjLogger.isDebugEnabled())
		{
			tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, FacilityCode.FAC_ATOMIC_ACTION, "BasicAction::doCleanup ("
					+ list_toprocess + ")");
		}

		if (list_toprocess.size() > 0)
		{
			int ok = TwoPhaseOutcome.FINISH_OK;

			while (((recordBeingHandled = list_toprocess.getFront()) != null))
			{
				if (actionType == ActionType.TOP_LEVEL)
					ok = recordBeingHandled.topLevelCleanup();
				else
					ok = recordBeingHandled.nestedCleanup();

				if ((actionType != ActionType.TOP_LEVEL)
						&& (recordBeingHandled.propagateOnAbort()))
				{
					merge(recordBeingHandled);
				}
				else
				{
					if (ok != TwoPhaseOutcome.FINISH_OK)
					{
						/* Preserve error messages */
					}

					recordBeingHandled = null;
				}
			}
		}
	}

	private final synchronized boolean doOnePhase ()
	{
		if (TxControl.onePhase)
		{
			return (((pendingList == null) || (pendingList.size() == 1)) ? true
					: false);
		}
		else
			return false;
	}

	/*
	 * Operation to merge a record into those held by the parent BasicAction.
	 * This is accomplished by invoking the add operation of the parent
	 * BasicAction. If the add operation does not return AR_ADDED, the record is
	 * deleted
	 */

	private final synchronized void merge (AbstractRecord A)
	{
		int as;

		if ((as = parentAction.add(A)) != AddOutcome.AR_ADDED)
		{
			A = null;

			if (as == AddOutcome.AR_REJECTED)
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.BasicAction_68");
		}
	}

	/* These (genuine) lists hold the abstract records */

	protected RecordList pendingList;
	protected RecordList preparedList;
	protected RecordList readonlyList;
	protected RecordList failedList;
	protected RecordList heuristicList;
	protected boolean savedIntentionList;
	
	private ActionHierarchy currentHierarchy;
	private ObjectStore currentStore;

	//    private boolean savedIntentionList;

	/* Atomic action status variables */

	private int actionStatus;
	private int actionType;
	private BasicAction parentAction;
	private AbstractRecord recordBeingHandled;
	private int heuristicDecision;
	private CheckedAction _checkedAction; // control what happens if threads
										  // active when terminating.

	/*
	 * We need to keep track of the number of threads associated with each
	 * action. Since we can't override the basic thread methods, we have to
	 * provide an explicit means of registering threads with an action.
	 */

	private Hashtable _childThreads;
	private Hashtable _childActions;

	//    private Mutex _lock = new Mutex(); // TODO

}

