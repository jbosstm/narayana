/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jts.interposition.ServerControlWrapper;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;

/**
 * A subordinate JTA transaction; used when importing another
 * transaction context.
 * 
 * @author mcl
 */

public class SubordinateAtomicTransaction extends com.arjuna.ats.internal.jta.transaction.jts.subordinate.SubordinateAtomicTransaction
{

	public SubordinateAtomicTransaction (Uid actId, Xid xid, int timeout)
	{
		super(new ServerControlWrapper(new ServerControl(new ServerTransaction(actId, xid))));
		
		// add this transaction to the reaper list.
		
		if (timeout > 0)
		{
			TransactionReaper reaper = TransactionReaper.transactionReaper();
			
			reaper.insert(super.getControlWrapper(), timeout);
		}
	}
	
	/**
	 * Failure recovery constructor.
	 * 
	 * @param actId transaction to be recovered.
	 */
	
	public SubordinateAtomicTransaction (Uid actId)
	{
		super(new ServerControlWrapper(new ServerControl(new ServerTransaction(actId))));
	}
	
	public final Xid getXid ()
	{
		try
		{
			ServerTransaction tx = (ServerTransaction) super._theAction.getImple().getImplHandle();

            // could be null if activation failed.
            if (tx != null) {
                return tx.getXid();
            } else {
                return null;
            }
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}

}