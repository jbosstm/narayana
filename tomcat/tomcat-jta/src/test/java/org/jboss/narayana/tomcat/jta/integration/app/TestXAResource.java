/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.tomcat.jta.integration.app;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestXAResource implements XAResource, XAResourceRecoveryHelper {

    private static final List<String> METHOD_CALLS = new LinkedList<>();

    private Xid xid;

    public static List<String> getMethodCalls() {
        return Collections.unmodifiableList(METHOD_CALLS);
    }

    public static void reset() {
        METHOD_CALLS.clear();
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
        METHOD_CALLS.add("start");
        this.xid = xid;
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        METHOD_CALLS.add("commit");
        this.xid = null;
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        METHOD_CALLS.add("rollback");
        this.xid = null;
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
        METHOD_CALLS.add("end");
    }

    @Override
    public void forget(Xid xid) throws XAException {
        METHOD_CALLS.add("forget");
        this.xid = null;
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return xaResource instanceof TestXAResource;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        METHOD_CALLS.add("prepare");
        return 0;
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        if (xid == null) {
            return new Xid[0];
        }

        return new Xid[]{xid};
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        return true;
    }

    @Override
    public boolean initialise(String p) throws Exception {
        return true;
    }

    @Override
    public XAResource[] getXAResources() {
        return new XAResource[]{this};
    }
}
