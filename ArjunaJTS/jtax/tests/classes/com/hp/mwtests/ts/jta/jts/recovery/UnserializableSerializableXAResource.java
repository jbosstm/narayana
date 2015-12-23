package com.hp.mwtests.ts.jta.jts.recovery;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class UnserializableSerializableXAResource implements XAResource, Serializable {

    private transient boolean precrash;
    private transient Xid xid;

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        throw new ClassNotFoundException();
    }

    public UnserializableSerializableXAResource(boolean precrash) {
        this.precrash = precrash;
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        // TODO Auto-generated method stub

    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        // TODO Auto-generated method stub

    }

    @Override
    public int prepare(Xid xid) throws XAException {
        this.xid = xid;
        return 0;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        if (precrash) {
            throw new XAException(XAException.XA_RETRY);
        }

    }

    @Override
    public void rollback(Xid xid) throws XAException {
        // TODO Auto-generated method stub

    }

    @Override
    public void forget(Xid xid) throws XAException {
        // TODO Auto-generated method stub

    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSameRM(XAResource xaRes) throws XAException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        // TODO Auto-generated method stub
        return false;
    }

    public Xid getXid() {
        return xid;
    }

}
