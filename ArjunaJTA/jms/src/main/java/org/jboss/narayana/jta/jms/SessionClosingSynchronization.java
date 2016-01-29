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
import javax.jms.Session;
import javax.transaction.Synchronization;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class SessionClosingSynchronization implements Synchronization {

    private static final Logger LOGGER = Logger.getLogger(SessionClosingSynchronization.class.getName());

    private final Session session;

    public SessionClosingSynchronization(Session session) {
        this.session = session;
    }

    @Override
    public void beforeCompletion() {
        // Nothing to do
    }

    @Override
    public void afterCompletion(int status) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Closing session " + session);
        }

        try {
            session.close();
        } catch (JMSException e) {
            LOGGER.warning("Failed to close session " + session + ": " + e.getMessage());
        }
    }

    /**
     * Should only be used for testing.
     */
    Session getSession() {
        return session;
    }

}
