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
package org.jboss.narayana.jta.jms;

import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class JmsXAResourceRecoveryHelper implements XAResourceRecoveryHelper, XAResource {

    private static final Logger LOGGER = Logger.getLogger(JmsXAResourceRecoveryHelper.class);

    private final XAConnectionFactory xaConnectionFactory;

    private final String user;

    private final String pass;

    private XAConnection xaConnection;

    private XASession xaSession;

    private XAResource delegate;

    public JmsXAResourceRecoveryHelper(XAConnectionFactory xaConnectionFactory) {
        this(xaConnectionFactory, null, null);
    }

    public JmsXAResourceRecoveryHelper(XAConnectionFactory xaConnectionFactory, String user, String pass) {
        this.xaConnectionFactory = xaConnectionFactory;
        this.user = user;
        this.pass = pass;
    }

    @Override
    public boolean initialise(String properties) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Initialise with properties=" + properties);
        }

        return true;
    }

    @Override
    public XAResource[] getXAResources() {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Returning XA resource: " + this);
        }

        return new XAResource[] { this };
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        if (i == XAResource.TMSTARTRSCAN) {
            connect();
        }

        assert delegate != null : "Recovery scan has to be started";

        try {
            return delegate.recover(i);
        } finally {
            if (i == XAResource.TMENDRSCAN) {
                disconnect();
            }
        }
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
        assert delegate != null : "Recovery scan has to be started";

        delegate.start(xid, i);
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
        assert delegate != null : "Recovery scan has to be started";

        delegate.end(xid, i);
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        assert delegate != null : "Recovery scan has to be started";

        return delegate.prepare(xid);
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        assert delegate != null : "Recovery scan has to be started";

        delegate.commit(xid, b);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        assert delegate != null : "Recovery scan has to be started";

        delegate.rollback(xid);
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        assert delegate != null : "Recovery scan has to be started";

        return delegate.isSameRM(xaResource);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        assert delegate != null : "Recovery scan has to be started";

        delegate.forget(xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        assert delegate != null : "Recovery scan has to be started";

        return delegate.getTransactionTimeout();
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        assert delegate != null : "Recovery scan has to be started";

        return delegate.setTransactionTimeout(i);
    }

    private void connect() throws XAException {
        if (delegate != null) {
            return;
        }

        try {
            xaConnection = createXAConnection();
            xaSession = xaConnection.createXASession();
            delegate = xaSession.getXAResource();
        } catch (JMSException e) {
            LOGGER.warn("Failed to create connection", e);
            throw new XAException(e.getMessage());
        }
    }

    private void disconnect() throws XAException {
        try {
            xaConnection.close();
        } catch (JMSException e) {
            LOGGER.warn("Failed to close connection", e);
            throw new XAException(e.getMessage());
        } finally {
            xaConnection = null;
            xaSession = null;
            delegate = null;
        }
    }

    private XAConnection createXAConnection() throws JMSException {
        if (user == null && pass == null) {
            return xaConnectionFactory.createXAConnection();
        }

        return xaConnectionFactory.createXAConnection(user, pass);
    }

}
