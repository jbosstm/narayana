/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms;

import com.arjuna.ats.jta.logging.jtaLogger;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionConsumer;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import jakarta.jms.ServerSessionPool;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.jms.XAConnection;
import jakarta.jms.XASession;
import jakarta.transaction.Synchronization;

/**
 * Proxy connection to wrap around provided {@link XAConnection}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ConnectionProxy implements Connection {

    private final XAConnection xaConnection;

    private final TransactionHelper transactionHelper;
    private boolean connectionCloseScheduled;

    /**
     * @param xaConnection XA connection which needs to be proxied.
     * @param transactionHelper utility to make transaction resources registration easier.
     */
    public ConnectionProxy(XAConnection xaConnection, TransactionHelper transactionHelper) {
        this.xaConnection = xaConnection;
        this.transactionHelper = transactionHelper;
    }

    /**
     * Simply create a session with an XA connection if there is no active transaction. Or create a proxied session and register
     * it with an active transaction.
     *
     * @see SessionProxy
     * @see Connection#createSession(boolean, int)
     */
    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        if (transactionHelper.isTransactionAvailable()) {
            return createAndRegisterSession();
        }

        return xaConnection.createSession(transacted, acknowledgeMode);
    }

    @Override
    public Session createSession(int sessionMode) throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        if (transactionHelper.isTransactionAvailable()) {
            return createAndRegisterSession();
        }

        return xaConnection.createSession(sessionMode);
    }

    @Override
    public Session createSession() throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        if (transactionHelper.isTransactionAvailable()) {
            return createAndRegisterSession();
        }

        return xaConnection.createSession();
    }

    /**
     * Simply close the proxied connection if there is no active transaction. Or register a
     * {@link ConnectionClosingSynchronization} if active transaction exists.
     * 
     * @throws JMSException if transaction service has failed (in unexpected way) to obtain transaction status,
     *   or if synchronization registration, or connection closing has failed.
     */
    @Override
    public void close() throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        if (transactionHelper.isTransactionAvailable()) {
            connectionCloseScheduled = true;
            Synchronization synchronization = new ConnectionClosingSynchronization(xaConnection);
            transactionHelper.registerSynchronization(synchronization);

            if (jtaLogger.logger.isTraceEnabled()) {
                jtaLogger.logger.trace("Registered synchronization to close the connection: " + synchronization);
            }
        } else {
            xaConnection.close();
        }
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#getClientID()
     */
    @Override
    public String getClientID() throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        return xaConnection.getClientID();
    }

    /**
     * @see Connection#setClientID(String)
     */
    @Override
    public void setClientID(String clientID) throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        xaConnection.setClientID(clientID);
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#getMetaData()
     */
    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        return xaConnection.getMetaData();
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#getExceptionListener()
     */
    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        return xaConnection.getExceptionListener();
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#setExceptionListener(ExceptionListener)
     */
    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        xaConnection.setExceptionListener(listener);
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#start()
     */
    @Override
    public void start() throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        xaConnection.start();
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#stop()
     */
    @Override
    public void stop() throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        xaConnection.stop();
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#createConnectionConsumer(Destination, String, ServerSessionPool, int)
     */
    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        return xaConnection.createConnectionConsumer(destination, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        return xaConnection.createSharedConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
    }

    /**
     * Delegate to {@link #xaConnection}.
     *
     * @see Connection#createDurableConnectionConsumer(Topic, String, String, ServerSessionPool, int)
     */
    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        return xaConnection.createDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        if (connectionCloseScheduled) {
            throw new RuntimeException("Connection is already scheduled to be closed");
        }
        return xaConnection.createSharedDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
    }

    /**
     * Create a proxied XA session and enlist its XA resource to the transaction.
     * <p>
     * If session's XA resource cannot be enlisted to the transaction, session is closed.
     *
     * @return XA session wrapped with {@link SessionProxy}.
     * @throws JMSException if failure occurred creating XA session or registering its XA resource.
     */
    private Session createAndRegisterSession() throws JMSException {
        XASession xaSession = xaConnection.createXASession();
        Session session = new SessionProxy(xaSession, transactionHelper);

        try {
            transactionHelper.registerXAResource(xaSession.getXAResource());
        } catch (JMSException e) {
            xaSession.close();
            throw e;
        }

        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Created new proxied session: " + session);
        }

        return session;
    }

}