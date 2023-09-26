/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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