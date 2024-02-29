package com.hp.mwtests.ts.jta.jts.xa;


import com.arjuna.ats.jta.xa.XidImple;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.Serializable;

/**
 * The key things about this class are that:
 * * It is serializable
 * * It will return XAER_RMFAIL out of rollback during recovery
 */
public class RMFAILOutofRollbackResource implements XAResource, Serializable {
    private XidImple xid;

    public RMFAILOutofRollbackResource(XidImple xid) {
        this.xid = xid;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        return new Xid[]{xid};
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        throw new XAException(XAException.XAER_RMFAIL);
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {

    }

    @Override
    public void end(Xid xid, int flags) throws XAException {

    }

    @Override
    public void forget(Xid xid) throws XAException {

    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    @Override
    public boolean isSameRM(XAResource xares) throws XAException {
        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return 0;
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {

    }
}