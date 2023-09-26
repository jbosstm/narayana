/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.arjunacore;

import com.arjuna.ats.arjuna.common.Uid;

public class AtomicAction extends com.arjuna.ats.arjuna.AtomicAction
{

	public AtomicAction ()
	{
		super();
	}

	/**
	 * Only used by failure recovery to recreate an inflight transaction.
	 * 
	 * @param actId the transaction to recreate.
	 */
	
	protected AtomicAction (Uid actId)
	{
		super(actId);
	}
	
	/**
	 * By default the BasicAction class only allows the termination of a
	 * transaction if it's the one currently associated with the thread. We
	 * override this here.
	 * 
	 * @return <code>false</code> to indicate that this transaction can only
	 *         be terminated by the right thread.
	 */

	protected boolean checkForCurrent ()
	{
		return false;
	}

}