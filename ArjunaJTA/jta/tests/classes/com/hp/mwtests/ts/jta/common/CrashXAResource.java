/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.common;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class CrashXAResource implements XAResource
{

    public void commit(Xid id, boolean onePhase) throws XAException
    {
	System.err.println("**sleeping**");
	
	try
	{
	    Thread.sleep(10000);
	}
	catch (Exception ex)
	{
	}
	
	System.err.println("**committed**");
    }

    public void end(Xid xid, int flags) throws XAException
    {
    }

    public void forget(Xid xid) throws XAException
    {
    }

    public int getTransactionTimeout() throws XAException
    {
	return 0;
    }

    public boolean isSameRM(XAResource xares) throws XAException
    {
	return false;
    }

    public int prepare(Xid xid) throws XAException
    {
        return XA_OK;
    }

    public Xid[] recover(int flag) throws XAException
    {
        return null;
    }

    public void rollback(Xid xid) throws XAException
    {
    }

    public boolean setTransactionTimeout(int seconds) throws XAException
    {
        return true;
    }

    public void start(Xid xid, int flags) throws XAException
    {
    }

}