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
 * Copyright (C) 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TwoPhaseCoordinator.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.ats.arjuna.logging.tsLogger;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Stack;

/**
 * Adds support for synchronizations to BasicAction. It does not change thread
 * associations either. It also allows any thread to terminate a transaction,
 * even if it is not the transaction that is marked as current for the thread
 * (unlike the BasicAction default).
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TwoPhaseCoordinator.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 3.0.
 */

public class TwoPhaseCoordinator extends BasicAction implements Reapable
{

	public TwoPhaseCoordinator ()
	{
	}

	public TwoPhaseCoordinator (Uid id)
	{
		super(id);
	}

	public int start ()
	{
		return start(BasicAction.Current());
	}

	public int start (BasicAction parentAction)
	{
		if (parentAction != null)
			parentAction.addChildAction(this);

		return super.Begin(parentAction);
	}

	public int end (boolean report_heuristics)
	{
		int outcome;

		if (parent() != null)
			parent().removeChildAction(this);

		if (beforeCompletion())
		{
			outcome = super.End(report_heuristics);
		}
		else
			outcome = super.Abort();

		afterCompletion(outcome);

		return outcome;
	}

	public int cancel ()
	{
		if (parent() != null)
			parent().removeChildAction(this);

		// beforeCompletion();

		int outcome = super.Abort();

		afterCompletion(outcome);

		return outcome;
	}

	public int addSynchronization (SynchronizationRecord sr)
	{
		if (sr == null)
			return AddOutcome.AR_REJECTED;

		int result = AddOutcome.AR_REJECTED;

		if (parent() != null)
			return AddOutcome.AR_REJECTED;

		switch (status())
		{
			case ActionStatus.RUNNING:
			{
				synchronized (this)
				{
					if (_synchs == null)
					{
						// Synchronizations should be stored (or at least iterated) in their natural order
						_synchs = new TreeSet();
					}
				}

				// disallow addition of Synchronizations that would appear
				// earlier in sequence than any that has already been called
				// during the pre-commmit phase. This generic support is required for
				// JTA Synchronization ordering behaviour
				if(sr instanceof Comparable && _currentRecord != null) {
					Comparable c = (Comparable)sr;
					if(c.compareTo(_currentRecord) != 1) {
						return AddOutcome.AR_REJECTED;
					}
				}

				if (_synchs.add(sr))
				{
					result = AddOutcome.AR_ADDED;
				}
			}
			break;
		default:
			break;
		}

		return result;
	}

	/**
	 * @return <code>true</code> if the transaction is running,
	 *         <code>false</code> otherwise.
	 */

	public boolean running ()
	{
		return (boolean) (status() == ActionStatus.RUNNING);
	}

	/**
	 * Overloads BasicAction.type()
	 */

	public String type ()
	{
		return "/StateManager/BasicAction/AtomicAction/TwoPhaseCoordinator";
	}

	/**
	 * Get any Throwable that was caught during commit processing but not directly rethrown.
	 * @return
	 */
	public Throwable getDeferredThrowable() {
		return _deferredThrowable;
	}

	protected TwoPhaseCoordinator (int at)
	{
		super(at);
	}

	protected TwoPhaseCoordinator (Uid u, int at)
	{
		super(u, at);
	}

	/**
	 * @message com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_1
	 *          [com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_1]
	 *          TwoPhaseCoordinator.beforeCompletion - attempted rollback_only
	 *          failed!
	 * @message com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_2
	 *          [com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_2]
	 *          TwoPhaseCoordinator.beforeCompletion - failed for {0}
	 */

	protected boolean beforeCompletion ()
	{
		boolean problem = false;

		/*
		 * If we have a synchronization list then we must be top-level.
		 */

		if (_synchs != null)
		{
			/*
			 * We must always call afterCompletion() methods, so just catch (and
			 * log) any exceptions/errors from beforeCompletion() methods.
			 *
			 * If one of the Syncs throws an error the Record wrapper returns false
			 * and we will rollback. Hence we don't then bother to call beforeCompletion
			 * on the remaining records (it's not done for rollabcks anyhow).
			 *
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

				try
				{
					problem = !_currentRecord.beforeCompletion();

					// if something goes wrong, we can't just throw the exception, we need to continue to
					// complete the transaction. However, the exception may have interesting information that
					// we want later, so we keep a reference to it as well as logging it.

				}
				catch (Exception ex)
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_2", new Object[]
					{ _currentRecord }, ex);
					if(_deferredThrowable == null) {
						_deferredThrowable = ex;
					}
					problem = true;
				}
				catch (Error er)
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_2", new Object[]
					{ _currentRecord }, er);
					if(_deferredThrowable == null) {
						_deferredThrowable = er;
					}
					problem = true;
				}
			}

			if (problem)
			{
				if (!preventCommit())
				{
					/*
					 * This should not happen. If it does, continue with commit
					 * to tidy-up.
					 */

					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_1");
				}
			}
		}

		return !problem;
	}

	/**
	 * @message com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_3
	 *          [com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_3]
	 *          TwoPhaseCoordinator.beforeCompletion
	 *          TwoPhaseCoordinator.afterCompletion called on still running
	 *          transaction!
	 * @message com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_4
	 *          [com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_4]
	 *          TwoPhaseCoordinator.afterCompletion - returned failure for {0}
	 * @message com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_4a
	 *          [com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_4a]
	 *          TwoPhaseCoordinator.afterCompletion - failed for {0} with exception {1}
	 * @message com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_4b
	 *          [com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_4b]
	 *          TwoPhaseCoordinator.afterCompletion - failed for {0} with error {1}
	 */

	protected boolean afterCompletion (int myStatus)
	{
		if (myStatus == ActionStatus.RUNNING)
		{
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_3");

			return false;
		}

		boolean problem = false;

		if (_synchs != null)
		{
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
				SynchronizationRecord record = (SynchronizationRecord)stack.pop();

				try
				{
					if (!record.afterCompletion(myStatus))
					{
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_4", new Object[]
						{ record });

						problem = true;
					}
				}
				catch (Exception ex)
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_4a", new Object[]
					{ record, ex });
					problem = true;
				}
				catch (Error er)
				{
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator_4b", new Object[]
					{ record, er });
					problem = true;
				}
			}

			_synchs = null;
			_currentRecord = null;
		}

		return !problem;
	}

	//private HashList _synchs;
	private SortedSet _synchs;
	private SynchronizationRecord _currentRecord; // the most recently processed Synchronization.
	private Throwable _deferredThrowable;
}
