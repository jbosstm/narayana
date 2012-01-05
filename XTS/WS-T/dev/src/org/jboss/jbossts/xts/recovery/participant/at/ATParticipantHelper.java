package org.jboss.jbossts.xts.recovery.participant.at;

import com.arjuna.wst.Durable2PCParticipant;

import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 *  helper to support retrieval of durable AT participant recovery state from participant
 */
public class ATParticipantHelper
{
    /**
     * obtain a byte array containing the recovery state associated with the supplied
     * participant.
     * @param useSerialization true if the object should be converted to a byte array using
     * serialization otherwise it will be converted by casting to the PersistableATParticipant
     * interface and employing the getRecoveryState method.
     * @param participant the participant whose recovery state is to be obtained
     * @return the state to be saved ro null if no state needs to be saved
     * @throws Exception an exception occurred generating the required recoverable state
     */
    public static byte[] getRecoveryState(boolean useSerialization, Durable2PCParticipant participant)
            throws Exception
    {
        if (useSerialization) {
            // serialize the object to a byte array via an object output stream

            final ByteArrayOutputStream baos = new ByteArrayOutputStream() ;
            final ObjectOutputStream oos = new ObjectOutputStream(baos) ;
            oos.writeObject(participant) ;
            oos.flush() ;
            return baos.toByteArray();
        } else {
            PersistableATParticipant persistableParticipant = (PersistableATParticipant) participant;
            return persistableParticipant.getRecoveryState();
        }
    }

    /**
     * return true if the object can be saved and restored using serialization otherwise
     * return false
     * @param participant
     * @return
     */
    public static boolean isSerializable(Durable2PCParticipant participant)
    {
        if (participant instanceof Serializable) {
            return true;
        }

        return false;
    }
}
