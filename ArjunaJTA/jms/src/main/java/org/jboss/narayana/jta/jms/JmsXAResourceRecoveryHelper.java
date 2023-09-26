/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms;

import jakarta.jms.XAConnectionFactory;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class JmsXAResourceRecoveryHelper implements XAResourceRecoveryHelper, XAResource {

    private final ConnectionManager connectionManager;

    public JmsXAResourceRecoveryHelper(XAConnectionFactory xaConnectionFactory) {
        this(xaConnectionFactory, null, null);
    }

    public JmsXAResourceRecoveryHelper(XAConnectionFactory xaConnectionFactory, String user, String pass) {
        this(new ConnectionManager(xaConnectionFactory, user, pass));
    }

    public JmsXAResourceRecoveryHelper(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
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
        if (!connectionManager.isConnected()) {
            try {
                connectionManager.connect();
            } catch (XAException ignored) {
                return new XAResource[0];
            }
        }

        return new XAResource[] { this };
    }

    /**
     * Delegates XAResource#recover call to the connected JMS resource. If provided argument is XAResource.TMENDRSCAN, then JMS
     * connection will be closed at the end of the call.
     *
     * @param flag
     * @throws XAException
     */
    @Override
    public Xid[] recover(int flag) throws XAException {
        try {
            return connectionManager.connectAndApply(delegate -> delegate.recover(flag));
        } finally {
            if (flag == XAResource.TMENDRSCAN) {
                connectionManager.disconnect();
            }
        }
    }

    /**
     * Delegates XAResource#start call to the connected JMS resource.
     *
     * @param xid
     * @param flag
     * @throws XAException
     */
    @Override
    public void start(Xid xid, int flag) throws XAException {
        connectionManager.connectAndAccept(delegate -> delegate.start(xid, flag));
    }

    /**
     * Delegates XAResource#end call to the connected JMS resource.
     *
     * @param xid
     * @param flag
     * @throws XAException
     */
    @Override
    public void end(Xid xid, int flag) throws XAException {
        connectionManager.connectAndAccept(delegate -> delegate.end(xid, flag));
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
        return connectionManager.connectAndApply(delegate -> delegate.prepare(xid));
    }

    /**
     * Delegates XAResource#commit call to the connected JMS resource.
     *
     * @param xid
     * @param onePhase
     * @throws XAException
     */
    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        connectionManager.connectAndAccept(delegate -> delegate.commit(xid, onePhase));
    }

    /**
     * Delegates XAResource#rollback call to the connected JMS resource.
     *
     * @param xid
     * @throws XAException
     */
    @Override
    public void rollback(Xid xid) throws XAException {
        connectionManager.connectAndAccept(delegate -> delegate.rollback(xid));
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
        return connectionManager.connectAndApply(delegate -> delegate.isSameRM(xaResource));
    }

    /**
     * Delegates XAResource#forget call to the connected JMS resource.
     *
     * @param xid
     * @throws XAException
     */
    @Override
    public void forget(Xid xid) throws XAException {
        connectionManager.connectAndAccept(delegate -> delegate.forget(xid));
    }

    /**
     * Delegates XAResource#getTransactionTimeout call to the connected JMS resource.
     *
     * @return Transaction timeout value.
     * @throws XAException
     */
    @Override
    public int getTransactionTimeout() throws XAException {
        return connectionManager.connectAndApply(XAResource::getTransactionTimeout);
    }

    /**
     * Delegates XAResource#setTransactionTimeout call to the connected JMS resource.
     *
     * @param seconds
     * @return True if transaction timeout was set, or false if wasn't.
     * @throws XAException
     */
    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return connectionManager.connectAndApply(delegate -> delegate.setTransactionTimeout(seconds));
    }

}