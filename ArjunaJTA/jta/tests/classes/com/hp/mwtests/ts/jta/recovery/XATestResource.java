/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.hp.mwtests.ts.jta.recovery;

import org.jboss.tm.XAResourceWrapper;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.Set;

/**
 * A wrapped test resource that can be configured to throw an exception from recover.
 *
 * The wrapper is so that it can be used for recovery using
 * {@link XATestResourceXARecovery} and the JNDI name is so that
 * it can be reported in the contacted/uncontacted lists during recovery via
 * {@link com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule#getUncontactedResourceNames(Set)}
 */
public class XATestResource implements XAResourceWrapper {
    static final String OK_JNDI_NAME = "jndiName1";
    static final String FAULTY_JNDI_NAME = "jndiName2";

    private static ArrayList<Xid> xids = new ArrayList<>();

    private final boolean failRecover;
    private final String jndiName;

    XATestResource(final String jndiName, boolean failRecover) {
        this.jndiName = jndiName;
        this.failRecover = failRecover;
    }

    public int prepare(Xid xid) throws XAException {
        xids.add(xid);

        return XAResource.XA_OK;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        if (!xids.remove(xid)) {
            throw new XAException("unknown xid: " + xid);
        }
    }

    public void rollback(Xid xid) throws XAException {
        if (!xids.remove(xid)) {
            throw new XAException("unknown xid: " + xid);
        }
    }

    public Xid[] recover(int flag) throws XAException {
        if (failRecover) {
            throw new XAException();
        }

        return xids.toArray(new Xid[0]);
    }

    public void end(Xid xid, int flags) throws XAException {
    }

    public void forget(Xid xid) throws XAException {
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        return true;
    }

    public void start(Xid xid, int flags) throws XAException {
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        return (xares == this);
    }

    @Override
    public XAResource getResource() {
        return this;
    }

    @Override
    public String getProductName() {
        return "p";
    }

    @Override
    public String getProductVersion() {
        return "v";
    }

    @Override
    public String getJndiName() {
        return jndiName;
    }
}
