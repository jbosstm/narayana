/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca;

import java.util.List;

import jakarta.transaction.HeuristicCommitException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.recovery.transactions.TransactionCache;

public class TransactionImple extends
		com.arjuna.ats.internal.jta.transaction.jts.subordinate.TransactionImple implements SubordinateTransaction
{

	/**
	 * Create a new transaction with the specified timeout.
	 */

	public TransactionImple (int timeout)
	{
		this(timeout, null);
	}
	
	public TransactionImple (int timeout, Xid importedXid)
	{
		super(new SubordinateAtomicTransaction(new Uid(), importedXid, timeout));

		TransactionImple.putTransaction(this);
	}

	/**
	 * For crash recovery purposes.
	 * 
	 * @param actId the transaction to recover.
	 */
	
	public TransactionImple (Uid actId)
	{
		super(new SubordinateAtomicTransaction(actId));
	}
	
	/**
	 * Only to be used by crash recovery. Should not be called directly by any
	 * other classes.
	 */
	
	public final void recordTransaction ()
	{
		TransactionImple.putTransaction(this);
	}
	
	public String toString ()
	{
		if (super._theTransaction == null)
			return "TransactionImple < jca-subordinate, NoTransaction >";
		else
		{
			return "TransactionImple < jca-subordinate, "
					+ super._theTransaction + " >";
		}
	}

	/**
	 * If this is an imported transaction (via JCA) then this will be the Xid
	 * we are pretending to be. Otherwise, it will be null.
	 * 
	 * @return null if we are a local transaction, a valid Xid if we have been
	 * imported.
	 */
	
	public Xid baseXid ()
	{
		return ((SubordinateAtomicTransaction) _theTransaction).getXid();
	}

    @Override
    public Object getId() {
        return get_uid();
    }

    public void recover() {
        ArjunaTransactionImple implHandle = getControlWrapper().getImple().getImplHandle();
        if (implHandle != null) {
            implHandle.activate();
        }
    }
    
    public boolean activated() {
        return true; // TODO: more sensible implementation.
    }

    /**
     * JTS implementation does not work with deferred throwables
     * as ORB protocol does not support adding a deferred throwable
     * under the base throwable.
     */
    public List<Throwable> getDeferredThrowables() {
        throw new UnsupportedOperationException();
    }

    /**
     * Method {@link #getDeferredThrowables()} will throw {@link UnsupportedOperationException}.
     *
     * @return false
     */
    public boolean supportsDeferredThrowables() {
        return false;
    }

	@Override
	public boolean doCommit() throws IllegalStateException, HeuristicMixedException, HeuristicRollbackException, HeuristicCommitException, SystemException {
		if (getStatus() == Status.STATUS_COMMITTING || getStatus() == Status.STATUS_COMMITTED) {
			TransactionCache.ReplayPhaseReturnStatus replayStatus = TransactionCache.replayPhase2(get_uid(), ServerTransaction.getType());
			// either the replay does not fully committed or moved under assumed completed state
			// returning that we haven't finished yet
			return replayStatus == TransactionCache.ReplayPhaseReturnStatus.ASSUME_COMPLETED;
		} else {
			return super.doCommit();
		}
	}
}