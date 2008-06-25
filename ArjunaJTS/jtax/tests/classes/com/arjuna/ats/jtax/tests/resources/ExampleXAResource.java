/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.ats.jtax.tests.resources;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ExampleXAResource.java 2342 2006-03-30 13:06:17Z  $
 */

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
