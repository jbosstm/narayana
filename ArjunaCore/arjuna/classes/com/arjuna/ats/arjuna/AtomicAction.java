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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AtomicAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

/**
 * This is a user-level transaction class, unlike BasicAction. AtomicAction
 * takes care of thread-to-action scoping. This is a "one-shot" object, i.e.,
 * once terminated, the instance cannot be re-used for another transaction.
 *
 * An instance of this class is a transaction that can be started and terminated
 * (either committed or rolled back). There are also methods to allow
 * participants (AbstractRecords) to be enlisted with the transaction and to
 * associate/disassociate threads with the transaction.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: AtomicAction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class AtomicAction extends TwoPhaseCoordinator
{

	public static final int NO_TIMEOUT = -1;

	/**
	 * Create a new transaction. If there is already a transaction associated
	 * with the thread then this new transaction will be automatically nested.
	 * The transaction is *not* running at this point.
	 *
	 * No timeout is associated with this transaction, i.e., it will not be
	 * automatically rolled back by the system.
	 */

	public AtomicAction ()
	{
		super();
	}

	/**
	 * AtomicAction constructor with a Uid. This constructor is for recreating
	 * an AtomicAction, typically during crash recovery.
	 */

	public AtomicAction (Uid objUid)
	{
		super(objUid);
	}

	/**
	 * Start the transaction running.
	 *
	 * If the transaction is already running or has terminated, then an error
	 * code will be returned. No timeout is associated with the transaction.
	 *
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	public int begin ()
	{
		return begin(AtomicAction.NO_TIMEOUT);
	}

	/**
	 * Start the transaction running.
	 *
	 * If the transaction is already running or has terminated, then an error
	 * code will be returned.
	 *
	 * @param timeout the timeout associated with the transaction. If the
	 *            transaction is still active when this timeout elapses, the
	 *            system will automatically roll it back.
	 *
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	public int begin (int timeout)
	{
		int status = super.start();

		if (status == ActionStatus.RUNNING)
		{
			/*
			 * Now do thread/action tracking.
			 */

			ThreadActionData.pushAction(this);

			_timeout = timeout;

			if (_timeout == 0)
				_timeout = TxControl.getDefaultTimeout();

			if (_timeout > 0)
				TransactionReaper.transactionReaper().insert(this, _timeout);
		}

		return status;
	}

	/**
	 * Commit the transaction, and have heuristic reporting. Heuristic reporting
	 * via the return code is enabled.
	 *
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	public int commit ()
	{
		return commit(true);
	}

	/**
	 * Commit the transaction. The report_heuristics parameter can be used to
	 * determine whether or not heuristic outcomes are reported.
	 *
	 * If the transaction has already terminated, or has not begun, then an
	 * appropriate error code will be returned.
	 *
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	public int commit (boolean report_heuristics)
	{
		int status = super.end(report_heuristics);

		/*
		 * Now remove this thread from the action state.
		 */

		ThreadActionData.popAction();

		TransactionReaper.transactionReaper().remove(this);

		return status;
	}

	/**
	 * Abort (rollback) the transaction.
	 *
	 * If the transaction has already terminated, or has not been begun, then an
	 * appropriate error code will be returned.
	 *
	 * @return <code>ActionStatus</code> indicating outcome.
	 */

	public int abort ()
	{
		int status = super.cancel();

		/*
		 * Now remove this thread from the action state.
		 */

		ThreadActionData.popAction();

		TransactionReaper.transactionReaper().remove(this);

		return status;
	}

	public int end (boolean report_heuristics)
	{
		int outcome = super.end(report_heuristics);

		/*
		 * Now remove this thread from the reaper. Leave
		 * the thread-to-tx association though.
		 */

		TransactionReaper.transactionReaper().remove(this);

		return outcome;
	}

	public int cancel ()
	{
		int outcome = super.cancel();

		/*
		 * Now remove this thread from the reaper. Leave
		 * the thread-to-tx association though.
		 */

		TransactionReaper.transactionReaper().remove(this);

		return outcome;
	}

	/*
	 * @return the timeout associated with this instance.
	 */

	public final int getTimeout ()
	{
		return _timeout;
	}

	/**
	 * The type of the class is used to locate the state of the transaction log
	 * in the object store.
	 *
	 * Overloads BasicAction.type()
	 *
	 * @return a string representation of the hierarchy of the class for storing
	 *         logs in the transaction object store.
	 */

	public String type ()
	{
		return "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction";
	}

	/**
	 * Register the current thread with the transaction. This operation is not
	 * affected by the state of the transaction.
	 *
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public boolean addThread ()
	{
		return addThread(Thread.currentThread());
	}

	/**
	 * Register the specified thread with the transaction. This operation is not
	 * affected by the state of the transaction.
	 *
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public boolean addThread (Thread t)
	{
		if (t != null)
		{
			ThreadActionData.pushAction(this);
			return true;
		}

		return false;
	}

	/**
	 * Unregister the current thread from the transaction. This operation is not
	 * affected by the state of the transaction.
	 *
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public boolean removeThread ()
	{
		return removeThread(Thread.currentThread());
	}

	/**
	 * Unregister the specified thread from the transaction. This operation is
	 * not affected by the state of the transaction.
	 *
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public boolean removeThread (Thread t)
	{
		if (t != null)
		{
			ThreadActionData.purgeAction(this, t);
			return true;
		}

		return false;
	}

	/**
	 * Suspend all transaction association from the invoking thread. When this
	 * operation returns, the thread will be associated with no transactions.
	 *
	 * If the current transaction is not an AtomicAction then this method will
	 * not suspend.
	 *
	 * @return a handle on the current AtomicAction (if any) so that the thread
	 *         can later resume association if required.
	 *
	 */

	public static final AtomicAction suspend ()
	{
		BasicAction curr = ThreadActionData.currentAction();

		if (curr != null)
		{
			if (curr instanceof AtomicAction)
				ThreadActionData.purgeActions();
			else {
                tsLogger.i18NLogger.warn_ats_atomicaction_1(curr.toString());

                curr = null;
            }
		}

		return (AtomicAction) curr;
	}

	/**
	 * Resume transaction association on the current thread. If the specified
	 * transaction is null, then this is the same as doing a suspend. If the
	 * current thread is associated with transactions then those associations
	 * will be lost.
	 *
	 * @param act the transaction to associate. If this is a nested
	 *            transaction, then the thread will be associated with all of
	 *            the transactions in the hierarchy.
	 *
	 * @return <code>true</code> if association is successful,
	 *         <code>false</code> otherwise.
	 */

	public static final boolean resume (AtomicAction act)
	{
		if (act == null)
		{
			suspend(); // If you ever change this, you need to change the way resume is handled in /ArjunaJTS/integration/src/main/java/com/arjuna/ats/jbossatx/BaseTransactionManagerDelegate.java
		}
		else
			ThreadActionData.restoreActions(act);

		return true;
	}

	/**
	 * Create a new transaction of the specified type.
	 */

	protected AtomicAction (int at)
	{
		super(at);
	}

	/**
	 * By default the BasicAction class only allows the termination of a
	 * transaction if it's the one currently associated with the thread. We
	 * override this here.
	 *
	 * @return <code>true</code> to indicate that this transaction can only be
	 *         terminated by the right thread.
	 */

	protected boolean checkForCurrent ()
	{
		return true;
	}

	private int _timeout = NO_TIMEOUT;

}
