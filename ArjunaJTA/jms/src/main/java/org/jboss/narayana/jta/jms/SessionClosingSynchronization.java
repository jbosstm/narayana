/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms;

import com.arjuna.ats.jta.logging.jtaLogger;

import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.transaction.Synchronization;

/**
 * Synchronization to close JMS session at the end of the transaction.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class SessionClosingSynchronization implements Synchronization {

    private final AutoCloseable session;

    /**
     * @param session session to be closed.
     */
    public SessionClosingSynchronization(AutoCloseable session) {
        this.session = session;
    }

    @Override
    public void beforeCompletion() {
        // Nothing to do
    }

    /**
     * Close the session no matter what the status of the transaction is.
     *
     * @param status the status of the completed transaction
     */
    @Override
    public void afterCompletion(int status) {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Closing session " + session);
        }

        try {
            session.close();
        } catch (Exception e) {
            jtaLogger.i18NLogger.warn_failed_to_close_jms_session(session.toString(), e);
        }
    }

}