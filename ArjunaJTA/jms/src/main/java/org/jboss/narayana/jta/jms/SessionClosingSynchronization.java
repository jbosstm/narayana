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

import javax.jms.JMSException;
import javax.jms.Session;
import javax.transaction.Synchronization;

/**
 * Synchronization to close JMS session at the end of the transaction.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class SessionClosingSynchronization implements Synchronization {

    private final Session session;

    /**
     * @param session session to be closed.
     */
    public SessionClosingSynchronization(Session session) {
        this.session = session;
    }

    @Override
    public void beforeCompletion() {
        // Nothing to do
    }

    /**
     * Close the session despite the status of the transaction.
     *
     * @param status
     */
    @Override
    public void afterCompletion(int status) {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Closing session " + session);
        }

        try {
            session.close();
        } catch (JMSException e) {
            jtaLogger.i18NLogger.warn_failed_to_close_jms_session(session.toString(), e);
        }
    }

}
