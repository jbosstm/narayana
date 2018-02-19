/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

package com.hp.mwtests.ts.jta.recovery;

import java.io.Serializable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

public class TestXAResourceRmFail extends TestXAResource implements Serializable {
    private static int commitCount;
    private static int rollbackCount;
    
    private static final long serialVersionUID = 1L;
    private boolean wasThrown = false;

    @Override
    public void commit(Xid id, boolean onePhase) throws XAException {
        if(!wasThrown) {
            wasThrown = true;
            throw new XAException(XAException.XAER_RMFAIL);
        }
        TestXAResourceRmFail.commitCount++;
        super.commit(id, onePhase);
    }

    public void rollback(Xid xid) throws XAException {
        TestXAResourceRmFail.rollbackCount++;
        super.rollback(xid);
    }

    public int commitCount() {
        return TestXAResourceRmFail.commitCount;
    }

    public int rollbackCount() {
        return TestXAResourceRmFail.rollbackCount;
    }

    public TestXAResourceRmFail clearCounters() {
        commitCount = 0;
        rollbackCount = 0;
        return this;
    }
}
