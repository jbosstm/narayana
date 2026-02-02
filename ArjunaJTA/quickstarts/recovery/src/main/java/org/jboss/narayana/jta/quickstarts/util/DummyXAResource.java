/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package org.jboss.narayana.jta.quickstarts.util;

import java.io.Serializable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class DummyXAResource implements XAResource, Serializable {
    static final long serialVersionUID = 1;

    public enum faultType {HALT, EX, NONE}

    private transient faultType fault = faultType.NONE;

    private static int commitRequests = 0;

    private Xid[] recoveryXids;

    public boolean startCalled;
    public boolean endCalled;
    public boolean prepareCalled;
    public boolean commitCalled;
    public boolean rollbackCalled;
    public boolean forgetCalled;
    public boolean recoverCalled;

    public DummyXAResource()
    {
        this(faultType.NONE);
    }

    public DummyXAResource(faultType fault)
    {
        this.fault = fault;
    }

    public void commit(final Xid xid, final boolean arg1) throws XAException
    {
        System.out.println("DummyXAResource commit() called, fault: " + fault + " xid: " + xid);
        commitCalled = true;
        commitRequests += 1;

        if (fault != null) {
            if (fault.equals(faultType.EX)) {
                throw new XAException(XAException.XA_RBTRANSIENT);
            } else if (fault.equals(faultType.HALT)) {
                recoveryXids = new Xid[1];
                recoveryXids[0] = xid;
                Runtime.getRuntime().halt(1);
            }
        }
    }

    public void end(final Xid xid, final int arg1) throws XAException
    {
        endCalled = true;
    }

    public void forget(final Xid xid) throws XAException
    {
        forgetCalled = true;
    }

    public int getTransactionTimeout() throws XAException
    {
        return 0;
    }
    public boolean isSameRM(final XAResource arg0) throws XAException
    {
        return this.equals(arg0);
    }

    public int prepare(final Xid xid) throws XAException
    {
        prepareCalled = true;
        return XAResource.XA_OK;
    }

    public Xid[] recover(final int arg0) throws XAException
    {
        recoverCalled = true;
        return recoveryXids;
    }

    public void rollback(final Xid xid) throws XAException
    {
        rollbackCalled = true;
    }

    public void start(final Xid xid, final int arg1) throws XAException
    {
        startCalled = true;
    }

    public boolean setTransactionTimeout(final int arg0) throws XAException
    {
        return false;
    }


    public static int getCommitRequests() {
        return commitRequests;
    }
}
