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

import java.util.*;
import java.util.concurrent.*;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.logging.tsLogger;

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
		{
		    if (typeOfAction() == ActionType.NESTED)
			parentAction.addChildAction(this);
		}

		return super.Begin(parentAction);
	}

	public int end (boolean report_heuristics)
	{
		int outcome;

		if (parent() != null)
		{
		    parent().removeChildAction(this);
		}

        boolean canEnd = true;
        if(status() != ActionStatus.ABORT_ONLY || TxControl.isBeforeCompletionWhenRollbackOnly())
        {
            canEnd = beforeCompletion();
        }

		if (canEnd)
		{
			outcome = super.End(report_heuristics);
		}
		else
			outcome = super.Abort();

		afterCompletion(outcome, report_heuristics);

		return outcome;
	}

	/**
	 * If this method is called and a transaction is not in a status of RUNNING,
	 * ABORT_ONLY or COMMITTING then do not call afterCompletion.
	 * 
	 * A scenario where this may occur is if during the completion of a previous
	 * transaction, a runtime exception is thrown from one of the AbstractRecords 
	 * methods.
	 * 
	 * RuntimeExceptions are not part of the contract of the API and as such all we
	 * can do is leave the transaction alone.
	 */
	public int cancel ()
	{
		if (parent() != null)
			parent().removeChildAction(this);

		// beforeCompletion();

		int outcome = super.Abort(true);

		if (outcome == ActionStatus.ABORTED) { 
			afterCompletion(outcome);
		}

		return outcome;
	}

	public int addSynchronization (SynchronizationRecord sr)
	{
		if (sr == null)
			return AddOutcome.AR_REJECTED;

		int result = AddOutcome.AR_REJECTED;

		// only allow registration for top-level transactions.
		
		if (parent() != null)
			return AddOutcome.AR_REJECTED;

		switch (status())
		{
		// https://jira.jboss.org/jira/browse/JBTM-608
		case ActionStatus.RUNNING:
		case ActionStatus.PREPARING:
		{
		    synchronized (this)
		    {
		        if (_synchs == null)
		        {
		            // Synchronizations should be stored (or at least iterated) in their natural order
		            _synchs = new TreeSet<SynchronizationRecord>();
		        }
		    }

            synchronized (_synchs) {
                if (runningSynchronizations != null) {
                    if (executingInterposedSynchs && !sr.isInterposed())
                        return AddOutcome.AR_REJECTED;

                    runningSynchronizations.add(synchronizationCompletionService.submit(
                            new AsyncBeforeSynchronization(this, sr)));

                    return AddOutcome.AR_ADDED;
                }

                // disallow addition of Synchronizations that would appear
                // earlier in sequence than any that has already been called
                // during the pre-commmit phase. This generic support is required for
                // JTA Synchronization ordering behaviour
                if(_currentRecord != null) {
                    if(sr.compareTo(_currentRecord) != 1) {
                        return AddOutcome.AR_REJECTED;
                    }
                }

                // need to guard against synchs being added while we are performing beforeCompletion processing
                if (_synchs.add(sr))
                {
                    result = AddOutcome.AR_ADDED;
                }
            }
		}
		break;
		default:
		    break;
		}

		return result;
	}

    private boolean asyncBeforeCompletion() {
        boolean problem = false;
        Collection<SynchronizationRecord> interposedSynchs = new ArrayList<SynchronizationRecord>();

        synchronized (_synchs) {
            synchronizationCompletionService = TwoPhaseCommitThreadPool.getNewCompletionService();
            runningSynchronizations = new ArrayList<Future<Boolean>>(_synchs.size());

            for (SynchronizationRecord synchRecord : _synchs) {
                if (synchRecord.isInterposed())
                    interposedSynchs.add(synchRecord);
                else
                    runningSynchronizations.add(synchronizationCompletionService.submit(
                            new AsyncBeforeSynchronization(this, synchRecord)));
            }

            // any further additions to _synchs from here on can only be interposed synchronizations
        }

        try {
            int processed = 0;

            do {
                synchronized (_synchs) {
                    if (processed == runningSynchronizations.size()) {
                        if (executingInterposedSynchs || interposedSynchs.size() == 0)
                            break; // all synchronizations have been executed

                        // all non interposed synchronizations have been executed
                        executingInterposedSynchs = true;
                        processed = 0;
                        runningSynchronizations.clear();

                        for (SynchronizationRecord synchRecord : interposedSynchs) {
                            runningSynchronizations.add(synchronizationCompletionService.submit(
                                    new AsyncBeforeSynchronization(this, synchRecord)));
                        }
                    }
                }

                processed += 1;

                try {
                    if (!synchronizationCompletionService.take().get())
                        problem = true;
                } catch (ExecutionException e) {
                    if (_deferredThrowable == null)
                        _deferredThrowable = e.getCause();

                    // the wrapper around the synchronization will already have logged the error
                    problem = true;
                } catch (InterruptedException e) {
                    tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_2(_currentRecord.toString(), e);
                    problem = true;
                }
            } while (!problem);
        }  finally {
            // if there was a problem then cancel any remaining synchronizations
            try {
                for (Future<Boolean> f : runningSynchronizations)
                    f.cancel(false); // canceling a completed task is a null op
            } finally {
                runningSynchronizations.clear();
            }
        }

        return !problem;
    }

	/**
	 * @return <code>true</code> if the transaction is running,
	 *         <code>false</code> otherwise.
	 */

	public boolean running ()
	{
		return (status() == ActionStatus.RUNNING || status() == ActionStatus.ABORT_ONLY);
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
	 * @return the Throwable, if any
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
	 * Drive beforeCompletion participants.
	 * 
	 * @return true if successful, false otherwise.
	 */
	
	protected boolean beforeCompletion ()
	{
	    boolean problem = false;

	    synchronized (_syncLock)
	    {
	        if (!_beforeCalled)
	        {
	            _beforeCalled = true;

	            /*
	             * If we have a synchronization list then we must be top-level.
	             */
                if (_synchs == null) {
	                /*
	                 * beforeCompletions already called. Assume everything is alright
	                 * to proceed to commit. The TM instance will flag the outcome. If
	                 * it's rolling back, then we'll get an exception. If it's committing
	                 * then we'll be blocked until the commit (assuming we're still the
	                 * slower thread).
	                 */
                } else if (TxControl.asyncBeforeSynch && _synchs.size() > 1) {
                    problem = !asyncBeforeCompletion();
                } else {
	                /*
	                 * We must always call afterCompletion() methods, so just catch (and
	                 * log) any exceptions/errors from beforeCompletion() methods.
	                 *
	                 * If one of the Syncs throws an error the Record wrapper returns false
	                 * and we will rollback. Hence we don't then bother to call beforeCompletion
	                 * on the remaining records (it's not done for rollabcks anyhow).
	                 *
	                 * Since Synchronizations may register other Synchronizations, we can't simply
	                 * iterate the collection. Instead we work from an ordered copy, which we periodically
	                 * check for freshness. The addSynchronization method uses _currentRecord to disallow
	                 * adding records in the part of the array we have already traversed, thus all
	                 * Synchronization will be called and the (jta only) rules on ordering of interposed
	                 * Synchronization will be respected.
	                 */

	                int lastIndexProcessed = -1;
	                SynchronizationRecord[] copiedSynchs;
	                // need to guard against synchs being added while we are performing beforeCompletion processing
	                synchronized (_synchs) {
	                    copiedSynchs = (SynchronizationRecord[])_synchs.toArray(new SynchronizationRecord[] {});
	                }
	                while( (lastIndexProcessed < _synchs.size()-1) && !problem) {

	                    synchronized (_synchs) {
	                        // if new Synchronization have been registered, refresh our copy of the collection:
	                        if(copiedSynchs.length != _synchs.size()) {
	                            copiedSynchs = (SynchronizationRecord[])_synchs.toArray(new SynchronizationRecord[] {});
	                        }
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
	                    catch (Exception ex) {
	                        tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_2(_currentRecord.toString(), ex);
	                        if (_deferredThrowable == null) {
	                            _deferredThrowable = ex;
	                        }
	                        problem = true;
	                    }
	                    catch (Error er) {
	                        tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_2(_currentRecord.toString(), er);
	                        if (_deferredThrowable == null) {
	                            _deferredThrowable = er;
	                        }
	                        problem = true;
	                    }
	                }
	            }
	        }

            if (problem && !preventCommit()) {
	            /*
	             * This should not happen. If it does, continue with commit
	             * to tidy-up.
	             */

                tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_1();
            }
	    }

	    return !problem;
	}

    protected boolean asyncAfterCompletion(int myStatus, boolean report_heuristics) {
        boolean problem = false;

        // note there is no need to synchronize on _synchs since synchronizations cannot be registered once
        // the action has started to commit
        for (Iterator<SynchronizationRecord> i =_synchs.iterator(); i.hasNext(); ) {
            SynchronizationRecord synchRecord = i.next();

            if (!report_heuristics && synchRecord instanceof HeuristicNotification)
                ((HeuristicNotification) synchRecord).heuristicOutcome(getHeuristicDecision());

            if (synchRecord.isInterposed()) {
                // run interposed synchronizations first
                i.remove();

                runningSynchronizations.add(synchronizationCompletionService.submit(
                        new AsyncAfterSynchronization(this, synchRecord, myStatus)));
            }
        }

        int processed = 0;

        executingInterposedSynchs = true;

        while (true) {
            if (processed == runningSynchronizations.size()) {
                if (!executingInterposedSynchs || _synchs.size() == 0)
                    break; // all synchronizations have been executed

                // all interposed synchronizations have been executed
                executingInterposedSynchs = false;
                processed = 0;
                runningSynchronizations.clear();

                for (SynchronizationRecord synchRecord : _synchs) {
                    runningSynchronizations.add(synchronizationCompletionService.submit(
                            new AsyncAfterSynchronization(this, synchRecord, myStatus)));
                }

                _synchs.clear();
            }

            processed += 1;

            try {
                if (!synchronizationCompletionService.take().get())
                    problem = true;
            } catch (InterruptedException e) {
                problem = true;
            } catch (ExecutionException e) {
                problem = true;
            }
        }

        return !problem;
    }

	/**
         * Drive afterCompletion participants.
         * 
         * @param myStatus the outcome of the transaction (ActionStatus.COMMITTED or ActionStatus.ABORTED).
         * 
         * @return true if successful, false otherwise.
         */
	
	protected boolean afterCompletion (int myStatus)
	{
	    return afterCompletion(myStatus, false);
	}
	
	/**
	 * Drive afterCompletion participants.
	 * 
	 * @param myStatus the outcome of the transaction (ActionStatus.COMMITTED or ActionStatus.ABORTED).
	 * @param report_heuristics does the caller want to be informed about heurisitics at the point of invocation?
	 * 
	 * @return true if successful, false otherwise.
	 */
	
	protected boolean afterCompletion (int myStatus, boolean report_heuristics)
	{
		if (myStatus == ActionStatus.RUNNING) {
            tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_3();

            return false;
        }

		boolean problem = false;

		synchronized (_syncLock)
		{
			if (!_afterCalled)
			{
				_afterCalled = true;

                if (_synchs == null) {
                    return !problem;
                } else if (TxControl.asyncAfterSynch && _synchs.size() > 1) {
                    problem = asyncAfterCompletion(myStatus, report_heuristics);
                } else {
					// afterCompletions should run in reverse order compared to
					// beforeCompletions
					Stack stack = new Stack();
					Iterator iterator = _synchs.iterator();
					while(iterator.hasNext()) {
						stack.push(iterator.next());
					}

					/*
					 * Regardless of failures, we must tell all synchronizations what
					 * happened.
					 */
					while(!stack.isEmpty())
					{
						SynchronizationRecord record = (SynchronizationRecord)stack.pop();

						/*
						 * If the caller doesn't want to be informed of heuristics during completion
						 * then it's possible the application (or admin) may still want to be informed.
						 * So special participants can be registered with the transaction which are
						 * triggered during the Synchronization phase and given the true outcome of
						 * the transaction. We do not dictate a specific implementation for what these
						 * participants do with the information (e.g., OTS allows for the CORBA Notification Service
						 * to be used).
						 */
						
						if (!report_heuristics)
						{
						    if (record instanceof HeuristicNotification)
						    {
						        ((HeuristicNotification) record).heuristicOutcome(getHeuristicDecision());
						    }
						}
						
						try
						{
							if (!record.afterCompletion(myStatus)) {
                                tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_4(record.toString());

                                problem = true;
                            }
						}
						catch (Exception ex) {
                            tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_4a(record.toString(), ex);
                            problem = true;
                        }
						catch (Error er) {
                            tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_4b(record.toString(), er);
                            problem = true;
                        }
					}

                    synchronized (_synchs) {
                        // nulling _syncs causes concurrency problems, so dispose contents instead:
                        _synchs.clear();
                    }
				}
			}
		}

		return !problem;
	}

    public java.util.Map<Uid, String> getSynchronizations()
    {
        java.util.Map<Uid, String> synchs = new java.util.HashMap<Uid, String> ();

        synchronized (this) {
            if (_synchs != null)
            {
                for (Object _synch : _synchs)
                {
                    SynchronizationRecord synch = (SynchronizationRecord) _synch;

                    synchs.put(synch.get_uid(), synch.toString());
                }
            }
        }

        return synchs;
    }

    private SortedSet<SynchronizationRecord> _synchs;
    private List<Future<Boolean>> runningSynchronizations = null;
    private CompletionService<Boolean> synchronizationCompletionService = null;
    private boolean executingInterposedSynchs = false;
	private SynchronizationRecord _currentRecord; // the most recently processed Synchronization.
	private Throwable _deferredThrowable;

	private Object _syncLock = new Object();

	private boolean _beforeCalled = false;
	private boolean _afterCalled = false;
}
