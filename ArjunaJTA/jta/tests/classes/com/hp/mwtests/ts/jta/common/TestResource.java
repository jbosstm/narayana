/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.common;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class TestResource implements XAResource
{
    public TestResource ()
    {
        this(false, true);
    }

    public TestResource (boolean readonly)
    {
        this(readonly, true);
    }
    
    public TestResource (boolean readonly, boolean print)
    {
        _readonly = readonly;
        _doPrint = print;
    }

    public void commit (Xid id, boolean onePhase) throws XAException
    {
        if (_doPrint)
            System.out.println("XA_COMMIT[" + id + "]");
    }

    public void end (Xid xid, int flags) throws XAException
    {
        if (_doPrint)
            System.out.println("XA_END[" + xid + "] Flags=" + flags);
    }

    public void forget (Xid xid) throws XAException
    {
        if (_doPrint)
            System.out.println("XA_FORGET[" + xid + "]");
    }

    public int getTransactionTimeout () throws XAException
    {
        return (_timeout);
    }

    public boolean isSameRM (XAResource xares) throws XAException
    {
        return (xares.equals(this));
    }

    public int prepare (Xid xid) throws XAException
    {
        if (_doPrint)
            System.out.println("XA_PREPARE[" + xid + "]");

        if (_readonly)
            return XA_RDONLY;
        else
            return XA_OK;

        // throw new XAException();
    }

    public Xid[] recover (int flag) throws XAException
    {
        if (_doPrint)
            System.out.println("RECOVER[" + flag + "]");
        
        return (null);
    }

    public void rollback (Xid xid) throws XAException
    {
        if (_doPrint)
            System.out.println("XA_ROLLBACK[" + xid + "]");
    }

    public boolean setTransactionTimeout (int seconds) throws XAException
    {
        _timeout = seconds;
        return (true);
    }

    public void start (Xid xid, int flags) throws XAException
    {
        if (_doPrint)
            System.out.println("XA_START[" + xid + "] Flags=" + flags);
    }

    protected int _timeout = 0;
    
    protected boolean _doPrint = false;

    private boolean _readonly = false;
}