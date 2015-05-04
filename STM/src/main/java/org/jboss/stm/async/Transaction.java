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
 * (C) 2015,
 * @author JBoss Inc.
 */


package org.jboss.stm.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.stm.internal.async.TransactionExecutorAbort;
import org.jboss.stm.internal.async.TransactionExecutorBegin;
import org.jboss.stm.internal.async.TransactionExecutorCommit;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;

/**
 * This is a user-level transaction class. Unlike AtomicAction which it uses, this
 * provides an asynchronous begin, commit and rollback capability. There
 * are a number of ways in which we currently support asynchronous interactions with
 * the transaction system, such as async prepare or async commit. The developer could
 * also register a two-phase aware participant or a Synchronisation and use callbacks
 * themselves to determine the outcome of the transaction. These continue to be
 * available to the developer but this API is intended to provide a simplified interface
 * to allow clients to make use of asynchronous transactions.
 * 
 * Note, we deliberately don't derive from AtomicAction so that developers must make
 * a conscious choice between a synchronous or asynchronous transaction.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id$
 */

public class Transaction
{
    /**
     * Create a new transaction. If there is already a transaction associated
     * with the thread then this new transaction will be automatically nested.
     * The transaction is *not* running at this point.
     *
     * No timeout is associated with this transaction, i.e., it will not be
     * automatically rolled back by the system.
     */

    public Transaction ()
    {
        _theTransaction = new AtomicAction();
    }

    /**
     * AtomicAction constructor with a Uid. This constructor is for recreating
     * an AtomicAction, typically during crash recovery.
     */

    public Transaction (Uid objUid)
    {
        _theTransaction = new AtomicAction(objUid);
    }

    /**
     * Start the transaction running.
     *
     * If the transaction is already running or has terminated, then an error
     * code will be returned. No timeout is associated with the transaction.
     *
     * @return <code>ActionStatus</code> indicating outcome.
     */

    public Future<Integer> begin ()
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

    public Future<Integer> begin (int timeout)
    {
        return _threadPool.submit(new TransactionExecutorBegin(timeout, _theTransaction));
    }

    /**
     * Commit the transaction, and have heuristic reporting. Heuristic reporting
     * via the return code is enabled.
     *
     * @return <code>ActionStatus</code> indicating outcome.
     */

    public Future<Integer> commit ()
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

    public Future<Integer> commit (boolean report_heuristics)
    {
        return _threadPool.submit(new TransactionExecutorCommit(report_heuristics, _theTransaction));
    }

    /**
     * Abort (rollback) the transaction.
     *
     * If the transaction has already terminated, or has not been begun, then an
     * appropriate error code will be returned.
     *
     * @return <code>ActionStatus</code> indicating outcome.
     */

    public Future<Integer> abort ()
    {
        return _threadPool.submit(new TransactionExecutorAbort(_theTransaction));
    }

    /*
     * @return the timeout associated with this instance.
     */

    public final int getTimeout ()
    {
        return _theTransaction.getTimeout();
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
        return _theTransaction.type();
    }

    /**
     * Suspend all transaction association from the invoking thread. When this
     * operation returns, the thread will be associated with no transactions.
     *
     * If the current transaction is not an AtomicAction then this method will
     * not suspend.
     * 
     * Note, we do not provide an async version of suspend because it would be
     * wrong for an application thread to proceed under the assumption it had
     * succeeded/happened yet. Ordering of events makes a difference here.
     *
     * @return a handle on the current AtomicAction (if any) so that the thread
     *         can later resume association if required.
     *
     */

    public static final Transaction suspend ()
    {
        return new Transaction(AtomicAction.suspend());
    }

    /**
     * Resume transaction association on the current thread. If the specified
     * transaction is null, then this is the same as doing a suspend. If the
     * current thread is associated with transactions then those associations
     * will be lost.
     * 
     * Note, we do not provide an async version of resume because it would be
     * wrong for an application thread to proceed under the assumption it had
     * succeeded/happened yet. Ordering of events makes a difference here.
     *
     * @param act the transaction to associate. If this is a nested
     *            transaction, then the thread will be associated with all of
     *            the transactions in the hierarchy.
     *
     * @return <code>true</code> if association is successful,
     *         <code>false</code> otherwise.
     */

    public static final boolean resume (Transaction act)
    {
        return AtomicAction.resume(act._theTransaction);
    }

    private Transaction (AtomicAction act)
    {
        _theTransaction = act;
    }
    
    private AtomicAction _theTransaction;
    private final ExecutorService _threadPool = Executors.newFixedThreadPool(1);
}
