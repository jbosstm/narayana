/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.common;

import java.io.Serializable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/*
 * Currently XAResources must be serializable so we can
 * recreate them in the event of a failure. It is likely
 * that other mechanisms will be added later to remove
 * this necessity, although serialization will still be
 * supported.
 */

public class DummyXA implements XAResource, Serializable
{
    
    public DummyXA (boolean print)
    {
	_timeout = 0;  // no timeout
	_print = print;
    }
    
    public void commit (Xid xid, boolean onePhase) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.commit called");
    }

    public void end (Xid xid, int flags) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.end called");
    }

    public void forget (Xid xid) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.forget called");
    }
    
    public int getTransactionTimeout () throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.getTransactionTimeout called");

	return _timeout;
    }
    
    public int prepare (Xid xid) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.prepare called");

	return XAResource.XA_OK;
    }

    public Xid[] recover (int flag) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.recover called");

	return null;
    }

    public void rollback (Xid xid) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.rollback called");
    }

    public boolean setTransactionTimeout (int seconds) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.setTransactionTimeout called");

	_timeout = seconds;

	return true;
    }

    public void start (Xid xid, int flags) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.start called");
    }

    public boolean isSameRM (XAResource xares) throws XAException
   {
       if (_print)
	   System.out.println("DummyXA.isSameRM called");
       
       return (xares == this);
   }

    private int _timeout;
    private boolean _print;
    
}