/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery.nonuniquexids;

import org.jboss.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/// unit test helper based on a Jonathan Halliday's code
public class XAResourceImpl implements XAResource {

    private static final Logger logger = Logger.getLogger("com.arjuna.test");

    protected final ResourceManager resourceManager;
    protected final String name;
    protected static int errorCount;

    static int getErrorCount() {
        return errorCount;
    }

    static void clearErrorCount() {
        errorCount = 0;
    }

    public XAResourceImpl(ResourceManager resourceManager, String name) {
        this.resourceManager = resourceManager;
        this.name = name;
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        logger.trace("commit "+resourceManager.getName()+"/"+name+"/"+xid);
        if (!resourceManager.isInDoubt(xid)) {
            logger.warnf("commit no such xid: %s/%s", resourceManager.getName(), name, xid);
            errorCount += 1;
        } else {
            resourceManager.removeDoubt(xid);
        }
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
    }

    @Override
    public void forget(Xid xid) throws XAException {
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        XAResourceImpl other = (XAResourceImpl)xaResource;
        return resourceManager.getName().equals(other.resourceManager.getName());
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        logger.trace("prepare "+resourceManager.getName()+"/"+name+"/"+xid);
        resourceManager.addDoubt(xid);
        return 0;
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        logger.trace("recover "+resourceManager.getName()+"/"+name+" "+i);
        return resourceManager.getDoubts();
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        logger.trace("rollback "+resourceManager.getName()+"/"+name+"/"+xid);
        resourceManager.addDoubt(xid);
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        return false;
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
    }
}