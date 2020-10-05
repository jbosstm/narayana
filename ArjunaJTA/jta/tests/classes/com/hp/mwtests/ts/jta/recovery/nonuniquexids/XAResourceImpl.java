/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
