/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.narayana.tools.perf;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import java.io.Serializable;

/**
 * Dummy implementation of the XAResource interface for test purposes.
 *
 *  @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-03
 */
public class XAResourceImpl implements XAResource, Serializable {
	static final long serialVersionUID = 1L;

    private String name;
    private int fault;
    private int txTimeout;
    private Xid currentXid;

    public XAResourceImpl(String name, int fault) {
        this.name = name;
        this.fault = fault;
    }

    public void commit(Xid xid, boolean b) throws XAException {
        //System.out.println("XAResourceImpl.commit(Xid="+xid+", b="+b+")");

/*
        if(!xid.equals(currentXid)) {
            System.out.println("XAResourceImpl.commit - wrong Xid!");
        }
*/

        currentXid = null;

//        System.out.println("XAResourceImpl.commit: " + xid + " FAULT: " + fault);
        switch (fault) {
        case 1:
            fault = 0;
            throw new XAException(XAException.XA_RBROLLBACK);
        case 2:
            break;
        case 3:
            break;
        default:
            break;
        }
    }

    public void end(Xid xid, int i) throws XAException {
        //System.out.println("XAResourceImpl.end(Xid="+xid+", b="+i+")");
    }

    public void forget(Xid xid) throws XAException {
        //System.out.println("XAResourceImpl.forget(Xid="+xid+")");
        if(!xid.equals(currentXid)) {
            System.out.println("XAResourceImpl.forget - wrong Xid!");
        }
        currentXid = null;
    }

    public int getTransactionTimeout() throws XAException {
//        System.out.println("XAResourceImpl.getTransactionTimeout() [returning "+txTimeout+"]");
        return txTimeout;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
//        System.out.println("XAResourceImpl.isSameRM(xaResource="+xaResource+")");
        return false;
    }

    public int prepare(Xid xid) throws XAException {
        //System.out.println("XAResourceImpl.prepare(Xid="+xid+")");
        return XAResource.XA_OK;
    }

    public Xid[] recover(int i) throws XAException {
//        System.out.println("XAResourceImpl.recover(i="+i+")");
        return new Xid[0];
    }
    public void rollback(Xid xid) throws XAException {
//        System.out.println("XAResourceImpl.rollback(Xid="+xid+")");
/*        if(!xid.equals(currentXid)) {
            System.out.println("XAResourceImpl.rollback - wrong Xid!");
        }*/
        currentXid = null;
    }

    public boolean setTransactionTimeout(int i) throws XAException {
        //System.out.println("XAResourceImpl.setTransactionTimeout(i="+i+")");
        txTimeout= i;
        return true;
    }

    public void start(Xid xid, int i) throws XAException {
        //System.out.println("XAResourceImpl.start(Xid="+xid+", i="+i+")");
/*        if(currentXid != null) {
            System.out.println("XAResourceImpl.start - wrong Xid!");
        }*/
        currentXid = xid;
    }

    public String toString() {
        return name;
    }
}



