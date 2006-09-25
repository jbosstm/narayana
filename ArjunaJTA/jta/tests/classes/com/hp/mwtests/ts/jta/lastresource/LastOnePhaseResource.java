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
