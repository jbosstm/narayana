/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.narayana.rest.bridge.inbound;

import org.jboss.jbossts.star.logging.RESTATLogger;
import org.jboss.logging.Logger;
import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.ParticipantDeserializer;

import java.io.ObjectInputStream;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class InboundBridgeParticipantDeserializer implements ParticipantDeserializer {

    private static final Logger LOG = Logger.getLogger(InboundBridgeParticipantDeserializer.class);

    @Override
    public Participant deserialize(ObjectInputStream objectInputStream) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeParticipantDeserializer.deserialize");
        }

        Object object = null;

        try {
            object = objectInputStream.readObject();
        } catch (Exception e) {
            RESTATLogger.atI18NLogger.warn_deserializeInboundBridgeParticipantDeserializer(e.getMessage(), e);
            return null;
        }

        if (object instanceof Participant) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("InboundBridgeParticipantDeserializer.deserialize: participant was successfully deserialized.");
            }

            return (Participant) object;
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeParticipantDeserializer.deserialize: participant was not deserialized.");
        }

        return null;
    }

    @Override
    public Participant recreate(byte[] recoveryState) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeParticipantDeserializer.recreate");
        }

        return null;
    }

}