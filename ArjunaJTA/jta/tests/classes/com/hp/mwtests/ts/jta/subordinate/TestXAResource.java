/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2008,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.subordinate;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

/**
 * Created by IntelliJ IDEA.
 * User: jhalli
 * Date: Apr 4, 2008
 * Time: 3:45:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestXAResource implements XAResource
{
    private int txTimeout;

    private Xid currentXid;

    private int prepareReturnValue = XAResource.XA_OK;
    private XAException commitException = null;

    public int getPrepareReturnValue()
    {
        return prepareReturnValue;
    }

    public void setPrepareReturnValue(int prepareReturnValue)
    {
        this.prepareReturnValue = prepareReturnValue;
    }

    public XAException getCommitException()
    {
        return commitException;
    }

    public void setCommitException(XAException commitException)
    {
        this.commitException = commitException;
    }

    public void commit(Xid xid, boolean b) throws XAException
    {
        System.out.println("XAResourceImpl.commit(Xid="+xid+", b="+b+")");
        if(!xid.equals(currentXid)) {
            System.out.println("XAResourceImpl.commit - wrong Xid!");
        }

        if(commitException != null) {
            throw commitException;
        }

        currentXid = null;
    }

    public void end(Xid xid, int i) throws XAException {
        System.out.println("XAResourceImpl.end(Xid="+xid+", b="+i+")");
    }

    public void forget(Xid xid) throws XAException {
        System.out.println("XAResourceImpl.forget(Xid="+xid+")");
        if(!xid.equals(currentXid)) {
            System.out.println("XAResourceImpl.forget - wrong Xid!");
        }
        currentXid = null;
    }

    public int getTransactionTimeout() throws XAException {
        System.out.println("XAResourceImpl.getTransactionTimeout() [returning "+txTimeout+"]");
        return txTimeout;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
        System.out.println("XAResourceImpl.isSameRM(xaResource="+xaResource+")");
        return false;
    }

    public int prepare(Xid xid) throws XAException {
        System.out.println("XAResourceImpl.prepare(Xid="+xid+") returning "+prepareReturnValue);
        return prepareReturnValue;
    }

    public Xid[] recover(int i) throws XAException {
        System.out.println("XAResourceImpl.recover(i="+i+")");
        return new Xid[0];
    }

    public void rollback(Xid xid) throws XAException {
        System.out.println("XAResourceImpl.rollback(Xid="+xid+")");
        if(!xid.equals(currentXid)) {
            System.out.println("XAResourceImpl.rollback - wrong Xid!");
        }
        currentXid = null;
    }

    public boolean setTransactionTimeout(int i) throws XAException {
        System.out.println("XAResourceImpl.setTransactionTimeout(i="+i+")");
        txTimeout= i;
        return true;
    }

    public void start(Xid xid, int i) throws XAException {
        System.out.println("XAResourceImpl.start(Xid="+xid+", i="+i+")");
        if(currentXid != null) {
            System.out.println("XAResourceImpl.start - wrong Xid!");
        }
        currentXid = xid;
    }

    public String toString() {
        return new String("XAResourceImple("+txTimeout+", "+currentXid+")");
    }
}
