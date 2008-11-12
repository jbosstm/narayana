package com.arjuna.wst.stub;

import com.arjuna.wst.*;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;

/**
 * wrapper class allowing an application participant to be registered directly with the local coordinator service.
 * obvioulsy this wrapper cuts out the network hop delivering messgaes from coordinator to participant and vice
 * versa. it also differs from the remote stub in two further respects. firstly, it saves and restores the
 * participant details embedded directly in the coordinator (transaction) log record rather than in a separate
 * participant record. this still requires use of an application registered helper module to recreate the
 * participant from its saved state. secondly, it does not currently propagate the participant manager details
 * to the underlying participant.
 */
public class LocalParticipantCompletionParticipantStub
        implements RecoverableBusinessAgreementWithParticipantCompletionParticipant, PersistableParticipant
{
    public LocalParticipantCompletionParticipantStub(BusinessAgreementWithParticipantCompletionParticipant participant, String id)
    {
        this.participant = participant;
        this.id = id;
    }

    // empty constructor for crash recovery

    public LocalParticipantCompletionParticipantStub()
    {
        this.participant = null;
        this.id = null;
    }

    /**
     * establish  a back channel from the coordinator side protocol engine to the coordinator.
     *
     * @param participantManager a manager which will forward incoming remote participant requests to the coordinator
     */
    public void setParticipantManager(BAParticipantManager participantManager) {
        // currently unimplemented this really needs help from the applicaton helper
    }

    /**
     * The transaction has completed successfully. The participant previously
     * informed the coordinator that it was ready to complete.
     */

    public void close() throws com.arjuna.wst.WrongStateException, com.arjuna.wst.SystemException {
        participant.close();
    }

    /**
     * The transaction has cancelled, and the participant should undo any work.
     * The participant cannot have informed the coordinator that it has
     * completed.
     */

    public void cancel() throws com.arjuna.wst.FaultedException, com.arjuna.wst.WrongStateException, com.arjuna.wst.SystemException {
        participant.cancel();
    }

    /**
     * The transaction has cancelled. The participant previously
     * informed the coordinator that it had finished work but could compensate
     * later if required, so it is now requested to do so.
     *
     * @throws com.arjuna.wst.FaultedException
     *                                        if the participant was unable to
     *                                        perform the required compensation action because of an
     *                                        unrecoverable error. The coordinator is notified of this fault
     *                                        and as a result will stop resending compensation requests.
     * @throws com.arjuna.wst.SystemException if the participant was unable to
     *                                        perform the required compensation action because of a transient
     *                                        fault. The coordinator is not notified of this fault so it
     *                                        will retry the compensate request after a suitable timeout.
     */

    public void compensate() throws FaultedException, com.arjuna.wst.WrongStateException, com.arjuna.wst.SystemException {
        participant.compensate();
    }

    /**
     * @return the status value.
     */

    public String status() throws com.arjuna.wst.SystemException {
        return participant.status();
    }

    /**
     * If the participant enquires as to the status of the transaction it was
     * registered with and that transaction is no longer available (has rolled
     * back) then this operation will be invoked by the coordination service.
     */

    public void unknown() throws com.arjuna.wst.SystemException {
        participant.unknown();
    }

    /**
     * If the participant enquired as to the status of the transaction it was
     * registered with and an error occurs (e.g., the transaction service is
     * unavailable) then this operation will be invoked.
     */

    public void error() throws com.arjuna.wst.SystemException {
        participant.error();
    }

    /**
     * Save the state of the particpant to the specified input object stream.
     *
     * @param oos The output output stream.
     * @return true if persisted, false otherwise.
     */
    public boolean saveState(OutputObjectState oos) {
        // this needs to check if patrticipant is either serializable or a PersistableBAParticipant and
        // convert it to a byte[] as appropriate then save the byte[] array to the stream

        // save id
        // identify and save recreate mode (serialization or recreation)
        // retrieve byte[]
        // save byte[] valid flag
        // if valid flag save byte[]

        return false;
    }

    /**
     * Restore the state of the particpant from the specified input object stream.
     *
     * @param ios The Input object stream.
     * @return true if restored, false otherwise.
     */
    public boolean restoreState(InputObjectState ios) {
        // this needs to retrieve a byte[] from the stream and then locate a regsitered helper to
        // either deserialize the participant or recreate a new one.

        // restore id
        // restore recreate mode (serialization or recreation)
        // restore byte[] valid flag
        // if valid flag restore byte[]
        // iterate over helpers invoking ParticipantCompletion recreate or deserialize helper until
        // one of them returns a participant
        // return true if participant found otherwise false
        
        return false;
    }

    protected BusinessAgreementWithParticipantCompletionParticipant participant;
    protected String id;

}
