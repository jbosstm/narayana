/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.common;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.tm.LastResource;

public class LastXAResource implements XAResource, LastResource
{
    public void commit(Xid id, boolean onePhase) throws XAException
    {
        System.out.println("LastXAResource XA_COMMIT[" + id + "]");
    }

    public void end(Xid xid, int flags) throws XAException
    {
        System.out.println("LastXAResource XA_END[" + xid + "] Flags=" + flags);
    }

    public void forget(Xid xid) throws XAException
    {
        System.out.println("LastXAResource XA_FORGET[" + xid + "]");
    }

    public int getTransactionTimeout() throws XAException
    {
        return 0;
    }

    public boolean isSameRM(XAResource xares) throws XAException
    {
        return xares.equals(this);
    }

    public int prepare(Xid xid) throws XAException
    {
        System.out.println("LastXAResource XA_PREPARE[" + xid + "]");

        return XA_OK;
    }

    public Xid[] recover(int flag) throws XAException
    {
        System.out.println("LastXAResource RECOVER["+ flag +"]");

        return null;
    }

    public void rollback(Xid xid) throws XAException
    {
        System.out.println("LastXAResource XA_ROLLBACK[" + xid + "]");
    }

    public boolean setTransactionTimeout(int seconds) throws XAException
    {
        return true;
    }

    public void start(Xid xid, int flags) throws XAException
    {
        System.out.println("LastXAResource XA_START[" + xid + "] Flags=" + flags);
    }
    
}