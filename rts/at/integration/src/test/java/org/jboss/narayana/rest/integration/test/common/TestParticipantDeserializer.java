/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.rest.integration.test.common;

import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.ParticipantDeserializer;

import java.io.ObjectInputStream;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestParticipantDeserializer implements ParticipantDeserializer {

    @Override
    public Participant deserialize(ObjectInputStream objectInputStream) {
        Object object = null;

        try {
            object = objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (object instanceof Participant) {
            return (Participant) object;
        }

        return null;
    }

    @Override
    public Participant recreate(byte[] recoveryState) {
        return null;
    }

}
