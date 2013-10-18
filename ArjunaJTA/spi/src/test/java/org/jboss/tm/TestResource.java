/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.tm;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.List;

public class TestResource implements XAResource
{
    List<TestResource> prepareOrder = null;
    List<TestResource> endOrder = null;

    public int prepare (Xid xid) throws XAException {
        prepareOrder.add(this);
        return XA_OK;
    }

    public TestResource(List<TestResource> prepareOrder, List<TestResource> endOrder) {
        this.prepareOrder = prepareOrder;
        this.endOrder = endOrder;
    }

    public void commit (Xid id, boolean onePhase) throws XAException {
        endOrder.add(this);
    }

    public void rollback (Xid xid) throws XAException {
        endOrder.add(this);
    }

    public void end (Xid xid, int flags) throws XAException {
    }

    public void forget (Xid xid) throws XAException {
    }

    public int getTransactionTimeout () throws XAException {
        return _timeout;
    }

    public boolean isSameRM (XAResource xares) throws XAException {
        return false;
    }

    public Xid[] recover (int flag) throws XAException {
        return null;
    }

    public boolean setTransactionTimeout (int seconds) throws XAException {
        _timeout = seconds;
        return true;
    }

    public void start (Xid xid, int flags) throws XAException {
    }

    protected int _timeout = 0;
}
