/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jtax.tests.resources;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;



public class ExampleXAResource implements javax.transaction.xa.XAResource
{
    private int _timeout = 0;

    public void commit(Xid xid, boolean b) throws XAException
    {
        System.out.println("XAResource.commit");
    }

    public void end(Xid xid, int i) throws XAException
    {
        System.out.println("XAResource.end");
    }

    public void forget(Xid xid) throws XAException
    {
    }

    public int getTransactionTimeout() throws XAException
    {
        return _timeout;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException
    {
        return false;
    }

    public int prepare(Xid xid) throws XAException
    {
        System.out.println("XAResource.prepare");
        return XA_OK;
    }

    public Xid[] recover(int i) throws XAException
    {
        return null;
    }

    public void rollback(Xid xid) throws XAException
    {
        System.out.println("XAResource.rollback");
    }

    public boolean setTransactionTimeout(int i) throws XAException
    {
        _timeout = i;
        return true;
    }

    public void start(Xid xid, int i) throws XAException
    {
        System.out.println("XAResource.start");
    }
}