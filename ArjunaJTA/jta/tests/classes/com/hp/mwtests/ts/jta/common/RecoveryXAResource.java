/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.common;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jta.xa.XidImple;

public class RecoveryXAResource implements XAResource
{
    
    public RecoveryXAResource ()
    {
	if (xids == null)
	{
	    xids = new Xid[2];

	    AtomicAction a = new AtomicAction();
	
	    xids[0] = new XidImple(a);

	    String c = com.arjuna.ats.arjuna.coordinator.TxControl.getXANodeName();
	
	    String b = "2";
	
	    com.arjuna.ats.arjuna.coordinator.TxControl.setXANodeName(b);
	
	    xids[1] = new XidImple(new Uid());

	    com.arjuna.ats.arjuna.coordinator.TxControl.setXANodeName(c);
	}
    }
	
    public void commit (Xid xid, boolean onePhase) throws XAException
    {
	System.err.println("**commit "+xid);
    }

    public void end (Xid xid, int flags) throws XAException
    {
    }

    public void forget (Xid xid) throws XAException
    {
    }
    
    public int getTransactionTimeout () throws XAException
    {
	return 0;
    }
    
    public int prepare (Xid xid) throws XAException
    {
	return XAResource.XA_OK;
    }

    public Xid[] recover (int flag) throws XAException
    {
	System.err.println("**returning "+xids[0]+" and "+xids[1]);
	
	return xids;
    }

    public void rollback (Xid xid) throws XAException
    {
	System.err.println("**rollback "+xid);
    }

    public boolean setTransactionTimeout (int seconds) throws XAException
    {
	return true;
    }

    public void start (Xid xid, int flags) throws XAException
    {
    }

    public boolean isSameRM (XAResource xares) throws XAException
    {
	return (xares == this);
    }

    private static Xid[] xids = null;
    
}