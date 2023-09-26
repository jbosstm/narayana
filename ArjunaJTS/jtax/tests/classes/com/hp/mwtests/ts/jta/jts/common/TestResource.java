/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.common;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class TestResource implements XAResource
{
    protected int   _timeout = 0;

    public void commit(Xid id, boolean onePhase) throws XAException
    {
        System.out.println("XA_COMMIT[" + id + "]");
    }

    public void end(Xid xid, int flags) throws XAException
    {
        System.out.println("XA_END[" + xid + "] Flags=" + flags);
    }

    public void forget(Xid xid) throws XAException
    {
        System.out.println("XA_FORGET[" + xid + "]");
    }

    public int getTransactionTimeout() throws XAException
    {
        return (_timeout);
    }

    public boolean isSameRM(XAResource xares) throws XAException
    {
        return (xares.equals(this));
    }

    public int prepare(Xid xid) throws XAException
    {
        System.out.println("XA_PREPARE[" + xid + "]");

        return (XA_OK);
    }

    public Xid[] recover(int flag) throws XAException
    {
        System.out.println("RECOVER["+ flag +"]");
        return (null);
    }

    public void rollback(Xid xid) throws XAException
    {
        System.out.println("XA_ROLLBACK[" + xid + "]");
    }

    public boolean setTransactionTimeout(int seconds) throws XAException
    {
        _timeout = seconds;
        return (true);
    }

    public void start(Xid xid, int flags) throws XAException
    {
        System.out.println("XA_START[" + xid + "] Flags=" + flags);
    }
}