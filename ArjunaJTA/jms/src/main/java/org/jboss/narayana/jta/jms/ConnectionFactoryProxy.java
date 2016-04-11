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
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.XAConnectionFactory;

/**
 * Proxy connection factory to wrap around provided {@link XAConnectionFactory}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ConnectionFactoryProxy implements ConnectionFactory {

    private final XAConnectionFactory xaConnectionFactory;

    private final TransactionHelper transactionHelper;

    public ConnectionFactoryProxy(XAConnectionFactory xaConnectionFactory, TransactionHelper transactionHelper) {
        this.xaConnectionFactory = xaConnectionFactory;
        this.transactionHelper = transactionHelper;
    }

    @Override
    public Connection createConnection() throws JMSException {
        Connection connection = new ConnectionProxy(xaConnectionFactory.createXAConnection(), transactionHelper);

        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Created new proxied connection: " + connection);
        }

        return connection;
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        Connection connection = new ConnectionProxy(xaConnectionFactory.createXAConnection(userName, password),
                transactionHelper);

        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Created new proxied connection: " + connection);
        }

        return connection;
    }

}
