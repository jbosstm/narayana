package org.jboss.narayana.rest.integration.api;

import java.io.ObjectInputStream;


/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public interface ParticipantDeserializer {

    /**
     * Deserializes serializable participants.
     *
     *
     * @param objectInputStream
     * @return Participant instance if participant can be deserialized or null if not.
     */
    Participant deserialize(ObjectInputStream objectInputStream);

    /**
     * Recreates participant from byte array.
     *
     * @param recoveryState
     * @return Participant instance if participant can be recreated or null if not.
     */
    Participant recreate(byte[] recoveryState);

}
