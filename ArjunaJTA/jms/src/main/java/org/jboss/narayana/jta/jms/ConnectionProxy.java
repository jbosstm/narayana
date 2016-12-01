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

import com.arjuna.ats.jta.logging.jtaLogger;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.XAConnection;
import javax.jms.XASession;
import javax.transaction.Synchronization;

/**
 * Proxy connection to wrap around provided {@link XAConnection}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ConnectionProxy implements Connection {

    private final XAConnection xaConnection;

    private final TransactionHelper transactionHelper;

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
        if (transactionHelper.isTransactionAvailable()) {
            return createAndRegisterSession();
        }

        return xaConnection.createSession(transacted, acknowledgeMode);
    }

    /**
     * Simply close the proxied connection if there is no active transaction. Or register a
     * {@link ConnectionClosingSynchronization} if active transaction exists.
     * 
     * @throws JMSException if transaction status check, synchronization registration, or connection closing has failed.
     */
    @Override
    public void close() throws JMSException {
        if (transactionHelper.isTransactionAvailable()) {
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
        return xaConnection.getClientID();
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#setClientID(String)
     */
    @Override
    public void setClientID(String clientID) throws JMSException {
        xaConnection.setClientID(clientID);
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#getMetaData()
     */
    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return xaConnection.getMetaData();
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#getExceptionListener()
     */
    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return xaConnection.getExceptionListener();
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#setExceptionListener(ExceptionListener)
     */
    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        xaConnection.setExceptionListener(listener);
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#start()
     */
    @Override
    public void start() throws JMSException {
        xaConnection.start();
    }

    /**
     * Delegate to {@link #xaConnection}
     *
     * @see Connection#stop()
     */
    @Override
    public void stop() throws JMSException {
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
        return xaConnection.createConnectionConsumer(destination, messageSelector, sessionPool, maxMessages);
    }

    /**
     * Delegate to {@link #xaConnection}.
     *
     * @see Connection#createDurableConnectionConsumer(Topic, String, String, ServerSessionPool, int)
     */
    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return xaConnection.createDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
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
