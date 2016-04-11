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

import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class JmsXAResourceRecoveryHelper implements XAResourceRecoveryHelper, XAResource {

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

    /**
     * Nothing to initialise.
     *
     * @param properties
     * @return Always returns true
     */
    @Override
    public boolean initialise(String properties) {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Initialise with properties=" + properties);
        }

        return true;
    }

    /**
     * If JMS connection was created successfully, returns an array with one instance of JmsXAResourceRecoveryHelper. Otherwise,
     * returns an empty array.
     *
     * @return Array with one instance of JmsXAResourceRecoveryHelper or an empty array
     */
    @Override
    public XAResource[] getXAResources() {
        if (connect()) {
            if (jtaLogger.logger.isTraceEnabled()) {
                jtaLogger.logger.trace("Returning XA resource: " + this);
            }

            return new XAResource[] { this };
        }

        return new XAResource[0];
    }

    /**
     * Delegates XAResource#recover call to the connected JMS resource. If provided argument is XAResource.TMENDRSCAN, then JMS
     * connection will be closed at the end of the call.
     *
     * @param i
     * @throws XAException
     */
    @Override
    public Xid[] recover(int i) throws XAException {
        try {
            return delegate.recover(i);
        } finally {
            if (i == XAResource.TMENDRSCAN) {
                disconnect();
            }
        }
    }

    /**
     * Delegates XAResource#start call to the connected JMS resource.
     *
     * @param xid
     * @param i
     * @throws XAException
     */
    @Override
    public void start(Xid xid, int i) throws XAException {
        delegate.start(xid, i);
    }

    /**
     * Delegates XAResource#end call to the connected JMS resource.
     *
     * @param xid
     * @param i
     * @throws XAException
     */
    @Override
    public void end(Xid xid, int i) throws XAException {
        delegate.end(xid, i);
    }

    /**
     * Delegates XAResource#prepare call to the connected JMS resource.
     *
     * @param xid
     * @return Prepare outcome
     * @throws XAException
     */
    @Override
    public int prepare(Xid xid) throws XAException {
        return delegate.prepare(xid);
    }

    /**
     * Delegates XAResource#commit call to the connected JMS resource.
     *
     * @param xid
     * @param b
     * @throws XAException
     */
    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        delegate.commit(xid, b);
    }

    /**
     * Delegates XAResource#rollback call to the connected JMS resource.
     *
     * @param xid
     * @throws XAException
     */
    @Override
    public void rollback(Xid xid) throws XAException {
        delegate.rollback(xid);
    }

    /**
     * Delegates XAResource#isSameRM call to the connected JMS resource.
     *
     * @param xaResource
     * @return True if is same resource manager or false if not.
     * @throws XAException
     */
    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return delegate.isSameRM(xaResource);
    }

    /**
     * Delegates XAResource#forget call to the connected JMS resource.
     *
     * @param xid
     * @throws XAException
     */
    @Override
    public void forget(Xid xid) throws XAException {
        delegate.forget(xid);
    }

    /**
     * Delegates XAResource#getTransactionTimeout call to the connected JMS resource.
     *
     * @return Transaction timeout value.
     * @throws XAException
     */
    @Override
    public int getTransactionTimeout() throws XAException {
        return delegate.getTransactionTimeout();
    }

    /**
     * Delegates XAResource#setTransactionTimeout call to the connected JMS resource.
     *
     * @param i
     * @return True if transaction timeout was set, or false if wasn't.
     * @throws XAException
     */
    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        return delegate.setTransactionTimeout(i);
    }

    private boolean connect() {
        if (delegate != null) {
            return true;
        }

        try {
            xaConnection = createXAConnection();
            xaSession = xaConnection.createXASession();
            delegate = xaSession.getXAResource();
        } catch (JMSException e) {
            jtaLogger.i18NLogger.warn_failed_to_create_jms_connection(e);
            return false;
        }

        return true;
    }

    private void disconnect() {
        try {
            xaConnection.close();
        } catch (JMSException e) {
            jtaLogger.i18NLogger.warn_failed_to_close_jms_connection(xaConnection.toString(), e);
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
