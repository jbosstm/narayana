package com.hp.mwtests.ts.jta.jts;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Created by tom on 01/11/2016.
 */
public abstract class TestXAResource implements XAResource {

    protected Xid xid;

    public void end(Xid var1, int var2) throws XAException {

    }

    public void forget(Xid var1) throws XAException {

    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean isSameRM(XAResource var1) throws XAException {
        return false;
    }

    public boolean setTransactionTimeout(int var1) throws XAException {
        return true;
    }

    public void start(Xid var1, int var2) throws XAException {
        xid = var1;
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        if (xid != null) {
            return new Xid[]{xid};
        }
        return new Xid[0];
    }
}
