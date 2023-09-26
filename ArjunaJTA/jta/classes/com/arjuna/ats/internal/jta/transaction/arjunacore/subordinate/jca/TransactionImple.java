/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;

public class TransactionImple
		extends
		com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.TransactionImple implements SubordinateTransaction
{

	/**
	 * Create a new transaction with the specified timeout.
	 * 
	 * @deprecated Only used by tests
	 */

	public TransactionImple(int timeout)
	{
		this(timeout, null);
	}

	public TransactionImple(int timeout, Xid importedXid)
	{
		super(new SubordinateAtomicAction(timeout, importedXid));

		TransactionImple.putTransaction(this);
	}

	/**
	 * Used for failure recovery.
	 * 
	 * @param actId
	 *            the transaction state to recover.
	 */

	public TransactionImple(Uid actId)
	{
		super(new SubordinateAtomicAction(actId));

		// don't put it into list here: it may already be there!
	}
	
	public String getParentNodeName() {
		return ((SubordinateAtomicAction)_theTransaction).getParentNodeName();
	}

	public final void recordTransaction()
	{
		TransactionImple.putTransaction(this);
	}

	public String toString()
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
	 * If this is an imported transaction (via JCA) then this will be the Xid we
	 * are pretending to be. Otherwise, it will be null.
	 * 
	 * @return null if we are a local transaction, a valid Xid if we have been
	 *         imported.
	 */

	public final Xid baseXid()
	{
		return ((SubordinateAtomicAction) _theTransaction).getXid();
	}

    @Override
    public Object getId() {
        return get_uid();
    }

    /**
	 * Force this transaction to try to recover itself again.
	 */

	public void recover()
	{
		_theTransaction.activate();
	}

	/**
	 * Has the transaction been activated successfully? If not, we wait and try
	 * again later.
	 */

	public boolean activated()
	{
		return ((SubordinateAtomicAction) _theTransaction).activated();
	}
}