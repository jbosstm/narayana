/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DummyXA.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.common;

import javax.transaction.xa.*;
import java.io.*;

/*
 * Currently XAResources must be serializable so we can
 * recreate them in the event of a failure. It is likely
 * that other mechanisms will be added later to remove
 * this necessity, although serialization will still be
 * supported.
 */

public class DummyXA implements XAResource, Serializable
{
    
    public DummyXA (boolean print)
    {
	_timeout = 0;  // no timeout
	_print = print;
    }
    
    public void commit (Xid xid, boolean onePhase) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.commit called");
    }

    public void end (Xid xid, int flags) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.end called");
    }

    public void forget (Xid xid) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.forget called");
    }
    
    public int getTransactionTimeout () throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.getTransactionTimeout called");

	return _timeout;
    }
    
    public int prepare (Xid xid) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.prepare called");

	return XAResource.XA_OK;
    }

    public Xid[] recover (int flag) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.recover called");

	return null;
    }

    public void rollback (Xid xid) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.rollback called");
    }

    public boolean setTransactionTimeout (int seconds) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.setTransactionTimeout called");

	_timeout = seconds;

	return true;
    }

    public void start (Xid xid, int flags) throws XAException
    {
	if (_print)
	    System.out.println("DummyXA.start called");
    }

    public boolean isSameRM (XAResource xares) throws XAException
   {
       if (_print)
	   System.out.println("DummyXA.isSameRM called");
       
       return (xares == this);
   }

    private int _timeout;
    private boolean _print;
    
}
