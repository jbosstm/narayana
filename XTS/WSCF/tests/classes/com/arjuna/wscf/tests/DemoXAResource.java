/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DemoXAResource.java,v 1.1 2003/01/07 10:37:17 nmcl Exp $
 */

package com.arjuna.wscf.tests;

import javax.transaction.xa.*;

public class DemoXAResource implements XAResource
{
    
    public DemoXAResource ()
    {
    }
    
    public void commit (Xid xid, boolean onePhase) throws XAException
    {
	System.out.println("DemoXAResource.commit "+xid);
    }

    public void end (Xid xid, int flags) throws XAException
    {
	System.out.println("DemoXAResource.end "+xid);
    }

    public void forget (Xid xid) throws XAException
    {
	System.out.println("DemoXAResource.forget "+xid);
    }
    
    public int getTransactionTimeout () throws XAException
    {
	System.out.println("DemoXAResource.getTransactionTimeout");

	return 0;
    }
    
    public int prepare (Xid xid) throws XAException
    {
	System.out.println("DemoXAResource.prepare "+xid);

	return XAResource.XA_OK;
    }

    public Xid[] recover (int flag) throws XAException
    {
	System.out.println("DemoXAResource.recover "+flag);

	return null;
    }

    public void rollback (Xid xid) throws XAException
    {
	System.out.println("DemoXAResource.rollback "+xid);
    }

    public boolean setTransactionTimeout (int seconds) throws XAException
    {
	System.out.println("DemoXAResource.setTransactionTimeout "+seconds);

	return true;
    }

    public void start (Xid xid, int flags) throws XAException
    {
	System.out.println("DemoXAResource.start "+xid);
    }

    public boolean isSameRM (XAResource xares) throws XAException
    {
	System.out.println("DemoXAResource.isSameRM "+xares);
       
	return (xares == this);
    }
    
}
