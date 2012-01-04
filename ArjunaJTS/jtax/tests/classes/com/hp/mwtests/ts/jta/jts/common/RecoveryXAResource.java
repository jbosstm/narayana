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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryXAResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.common;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jta.xa.XidImple;

public class RecoveryXAResource implements XAResource
{
    
    public RecoveryXAResource ()
    {
	if (xids == null)
	{
	    xids = new Xid[2];

	    AtomicAction a = new AtomicAction();
	
	    xids[0] = new XidImple(a);

	    String c = com.arjuna.ats.arjuna.coordinator.TxControl.getXANodeName();
	
	    String b = "2";
	
	    com.arjuna.ats.arjuna.coordinator.TxControl.setXANodeName(b);
	
	    xids[1] = new XidImple(new Uid());

	    com.arjuna.ats.arjuna.coordinator.TxControl.setXANodeName(c);
	}
    }
	
    public void commit (Xid xid, boolean onePhase) throws XAException
    {
	System.err.println("**commit "+xid);
    }

    public void end (Xid xid, int flags) throws XAException
    {
    }

    public void forget (Xid xid) throws XAException
    {
    }
    
    public int getTransactionTimeout () throws XAException
    {
	return 0;
    }
    
    public int prepare (Xid xid) throws XAException
    {
	return XAResource.XA_OK;
    }

    public Xid[] recover (int flag) throws XAException
    {
	System.err.println("**returning "+xids[0]+" and "+xids[1]);
	
	return xids;
    }

    public void rollback (Xid xid) throws XAException
    {
	System.err.println("**rollback "+xid);
    }

    public boolean setTransactionTimeout (int seconds) throws XAException
    {
	return true;
    }

    public void start (Xid xid, int flags) throws XAException
    {
    }

    public boolean isSameRM (XAResource xares) throws XAException
    {
	return (xares == this);
    }

    private static Xid[] xids = null;
    
}
