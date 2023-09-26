/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms;

import com.arjuna.ats.jta.logging.jtaLogger;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.XAConnectionFactory;
import jakarta.jms.XAJMSContext;

/**
 * Proxy connection factory to wrap around provided {@link XAConnectionFactory}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ConnectionFactoryProxy implements ConnectionFactory {

    private final XAConnectionFactory xaConnectionFactory;

    private final TransactionHelper transactionHelper;

    /**
     * @param xaConnectionFactory factory to get XA connection instances, not null.
     * @param transactionHelper utility to make transaction resources registration easier.
     */
    public ConnectionFactoryProxy(XAConnectionFactory xaConnectionFactory, TransactionHelper transactionHelper) {
        this.xaConnectionFactory = xaConnectionFactory;
        this.transactionHelper = transactionHelper;
    }

    /**
     * Get XA connection from the provided factory and wrap it with {@link ConnectionProxy}.
     *
     * @return XA connection wrapped with {@link ConnectionProxy}.
     * @throws JMSException if failure occurred creating XA connection.
     */
    @Override
    public Connection createConnection() throws JMSException {
        Connection connection = new ConnectionProxy(xaConnectionFactory.createXAConnection(), transactionHelper);

        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Created new proxied connection: " + connection);
        }

        return connection;
    }

    /**
     * Get XA connection from the provided factory with credentials and wrap it with {@link ConnectionProxy}.
     * 
     * @param userName
     * @param password
     * @return XA connection wrapped with {@link ConnectionProxy}.
     * @throws JMSException if failure occurred creating XA connection.
     */
    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        Connection connection = new ConnectionProxy(xaConnectionFactory.createXAConnection(userName, password),
                transactionHelper);

        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Created new proxied connection: " + connection);
        }

        return connection;
    }

    @Override
    public JMSContext createContext() {
        return JMSContextProxy.wrapContext(xaConnectionFactory.createXAContext(), transactionHelper);
    }

    @Override
    public JMSContext createContext(String userName, String password) {
        return JMSContextProxy.wrapContext(xaConnectionFactory.createXAContext(userName, password), transactionHelper);
    }

    @Override
    public JMSContext createContext(String userName, String password, int sessionMode) {
        return JMSContextProxy.wrapContext((XAJMSContext) xaConnectionFactory.createXAContext(userName, password).createContext(sessionMode), transactionHelper);
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        return JMSContextProxy.wrapContext((XAJMSContext) xaConnectionFactory.createXAContext().createContext(sessionMode), transactionHelper);
    }

}