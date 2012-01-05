package org.jboss.jbossts.xts.recovery.participant.at;

import com.arjuna.wst.Durable2PCParticipant;

import java.io.ObjectInputStream;

/**
 * an interface implemented by applications which wish to be involved in recovering
 * saved participants after a crash.
 */
public interface XTSATRecoveryModule
{
    /**
     * called during recovery processing to allow an application to identify a participant id
     * belonging to one of its participants and recreate the participant by deserializing
     * it from the supplied object input stream. n.b. this is only appropriate in case the
     * participant was originally saved using serialization.
     * @param id the id used when the participant was created
     * @param stream a stream from which the application should deserialise the participant
     * if it recognises that the id belongs to the module's application
     * @return
     * @throws Exception if an error occurs deserializing the durable participant
     */
    public Durable2PCParticipant deserialize(String id, ObjectInputStream stream) throws Exception;

    /**
     * called during recovery processing to allow an application to identify a participant id
     * belonging to one of its participants and use the saved recovery state to recreate the
     * participant. n.b. this is only appropriate in case the participant was originally saved
     * after being converted to a byte array using the PersistibleATParticipant interface.
     * @param id the id used when the participant was created
     * @param recoveryState a byte array returned form the original participant via a call to
     * method getRecoveryState of interface PersistableATParticipant
     * @return
     * @throws Exception if an error occurs converting the recoveryState back to a
     * durable participant
     */
    public Durable2PCParticipant recreate(String id, byte[] recoveryState) throws Exception;

    /**
     * participant recovery modules may need to perform special processing when the a recovery scan has
     * completed. in particular it is only after the first recovery scan has completed they can identify
     * whether locally prepared changes are accompanied by a recreated participant and roll back changes
     * for those with no such participant.
     */
    public void endScan();
}
