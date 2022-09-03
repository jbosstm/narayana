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

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.transaction.Synchronization;

/**
 * Synchronization to close JMS connection at the end of the transaction.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ConnectionClosingSynchronization implements Synchronization {

    private final Connection connection;

    /**
     * @param connection connection to be closed.
     */
    public ConnectionClosingSynchronization(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void beforeCompletion() {
        // Nothing to do
    }

    /**
     * Close the connection no matter what the status of the transaction is.
     *
     * @param status the status of the completed transaction
     */
    @Override
    public void afterCompletion(int status) {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Closing connection " + connection);
        }

        try {
            connection.close();
        } catch (JMSException e) {
            jtaLogger.i18NLogger.warn_failed_to_close_jms_connection(connection.toString(), e);
        }
    }

}
