/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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