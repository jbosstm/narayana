/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore.jca;

import java.util.List;

import jakarta.transaction.HeuristicCommitException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.tm.ImportedTransaction;

/**
 * Subordinate transactions are those designed to be driven by a foreign controller,
 * so they expose methods for driving each of the termination phases individually.
 */
public interface SubordinateTransaction extends ImportedTransaction
{
	/**
	 * Drive the subordinate transaction through the prepare phase. Any
	 * enlisted participants will also be prepared as a result.
	 *
	 * @return a TwoPhaseOutcome representing the result.
	 */
    public int doPrepare();

	/**
	 * Drive the subordinate transaction to commit. It must have previously
	 * been prepared.
	 *
	 * @return true, if the transaction was fully committed, false if there was a transient error
	 *
	 * @throws IllegalStateException thrown if the transaction has not been prepared
	 * or is unknown.
	 * @throws HeuristicMixedException thrown if a heuristic mixed outcome occurs
	 * (where some participants committed whilst others rolled back).
	 * @throws HeuristicRollbackException thrown if the transaction rolled back.
	 * @throws SystemException thrown if some other error occurs.
	 */
    public boolean doCommit () throws IllegalStateException,
			HeuristicMixedException, HeuristicRollbackException, HeuristicCommitException,
			SystemException;

	/**
	 * Drive the subordinate transaction to roll back. It need not have been previously
	 * prepared.
	 *
	 * @throws IllegalStateException thrown if the transaction is not known by the
	 * system or has been previously terminated.
	 * @throws HeuristicMixedException thrown if a heuristic mixed outcome occurs
	 * (can only happen if the transaction was previously prepared and then only if
	 * some participants commit whilst others roll back).
	 * @throws HeuristicCommitException thrown if the transaction commits (can only
	 * happen if it was previously prepared).
	 * @throws SystemException thrown if any other error occurs.
	 */
    public void doRollback () throws IllegalStateException,
            HeuristicMixedException, HeuristicCommitException, HeuristicRollbackException, SystemException;

	/**
	 * Drive the transaction to commit. It should not have been previously
	 * prepared and will be the only resource in the global transaction.
	 *
	 * @throws IllegalStateException if the transaction has already terminated
	 * @throws jakarta.transaction.HeuristicRollbackException thrown if the transaction
	 * rolls back.
	 */
    public void doOnePhaseCommit () throws IllegalStateException,
			HeuristicMixedException, SystemException, RollbackException;

	/**
	 * Called to tell the transaction to forget any heuristics.
	 *
	 * @throws IllegalStateException thrown if the transaction cannot
	 * be found.
	 */
    public void doForget () throws IllegalStateException;

    /**
     * Run beforeCompletion on Synchronizations.
     * Note: this will run beforeCompletion even on setRollbackOnly transactions.
     * Users may wish to avoid calling this method in such cases, or prior to calling rollback.
     *
     * @return outcome
     */

    public boolean doBeforeCompletion () throws SystemException;

    public boolean activated();

    public void recover();

    public Xid baseXid();

    public Uid get_uid();

    /**
     * {@inheritDoc}
     */
    public List<Throwable> getDeferredThrowables();

    /**
     * {@inheritDoc}
     */
    public boolean supportsDeferredThrowables();
}