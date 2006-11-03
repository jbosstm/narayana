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
package com.hp.mwtests.ts.jta.lastresource;

import java.io.Serializable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.resources.LastResourceCommitOptimisation;

public class LastOnePhaseResource implements XAResource, Serializable, LastResourceCommitOptimisation
{
    private static final long serialVersionUID = -2677829642809706893L ;
    
    public static final int INITIAL = 0 ;
    public static final int COMMIT = 1 ;
    public static final int ROLLBACK = 2 ;
    
    private int status = INITIAL ;
    
    public int prepare(final Xid xid)
        throws XAException
    {
        throw new XAException("Prepare called on LastOnePhaseResource") ;
    }
    
    public void commit(final Xid xid, final boolean onePhaseCommit)
        throws XAException
    {
        if (!onePhaseCommit)
        {
            throw new XAException("commit called with onePhaseCommit false") ;
        }
        status = COMMIT ;
    }

    public void rollback(final Xid xid)
        throws XAException
    {
        status = ROLLBACK ;
    }
    
    public int getStatus()
    {
        return status ;
    }

    public boolean isSameRM(final XAResource xaResource)
        throws XAException
    {
        return this.equals(xaResource) ;
    }

    public void start(final Xid xid, final int flags)
        throws XAException
    {
        System.out.println("start called on LastOnePhaseResource for xid: " + xid + " with flags 0x" + Integer.toHexString(flags)) ;
    }

    public void end(final Xid xid, final int flags)
        throws XAException
    {
        System.out.println("end called on LastOnePhaseResource for xid: " + xid + " with flags 0x" + Integer.toHexString(flags)) ;
    }

    public Xid[] recover(final int flags)
        throws XAException
    {
        System.out.println("recover called on LastOnePhaseResource with flags 0x" + Integer.toHexString(flags)) ;
        return null ;
    }

    public void forget(final Xid xid)
        throws XAException
    {
        System.out.println("forget called on LastOnePhaseResource for xid: " + xid) ;
    }

    public boolean setTransactionTimeout(final int timeout)
        throws XAException
    {
        // don't support it
        return false ;
    }

    public int getTransactionTimeout()
        throws XAException
    {
        return 60 ;
    }
}
