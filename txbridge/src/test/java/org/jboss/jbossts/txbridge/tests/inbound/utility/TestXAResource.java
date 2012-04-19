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
package org.jboss.jbossts.txbridge.tests.inbound.utility;

import org.jboss.logging.Logger;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

/**
 * Implementation of XAResource for use in tx test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
public class TestXAResource extends TestXAResourceCommon implements XAResource {
    private static Logger log = Logger.getLogger(TestXAResource.class);

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        super.commit(xid, b);
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
        super.end(xid, i);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        super.forget(xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return super.getTransactionTimeout();
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return super.isSameRM(xaResource);
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return super.prepare(xid);
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        return super.recover(i);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        super.rollback(xid);
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        return super.setTransactionTimeout(i);
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
        super.start(xid, i);
    }

    @Override
    public String toString() {
        return new String("TestXAResource(" + super.toString() + ")");
    }
}
