package com.arjuna.wst.stub;

import com.arjuna.wst.*;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;

/**
 * wrapper class allowing an application participant to be registered directly with the local coordinator service.
 * this extends the local participant completion wrapper to also provide the complete method.
 */
public class LocalCoordinatorCompletionParticipantStub
        extends LocalParticipantCompletionParticipantStub
        implements BusinessAgreementWithCoordinatorCompletionParticipant, PersistableParticipant {

    public LocalCoordinatorCompletionParticipantStub(BusinessAgreementWithCoordinatorCompletionParticipant participant, String id) {
        super(participant, id);
        // keep a handle on the participant as a coordinatorCompletion participant so we can call its
        // complete method without having to cast it
        
        this.coordinatorCompletionParticipant = participant;
    }

    // empty constructor for crash recovery

    public LocalCoordinatorCompletionParticipantStub()
    {
        super();
        this.coordinatorCompletionParticipant = null;
    }

    /**
     * The coordinator is informing the participant that all work it needs to
     * do within the scope of this business activity has been received.
     */

    public void complete() throws WrongStateException, SystemException {
        coordinatorCompletionParticipant.complete();
    }

    /**
     * Save the state of the particpant to the specified input object stream.
     *
     * @param oos The output output stream.
     * @return true if persisted, false otherwise.
     */
    public boolean saveState(OutputObjectState oos) {

        return super.saveState(oos);
    }

    /**
     * Restore the state of the particpant from the specified input object stream.
     *
     * @param ios The Input object stream.
     * @return true if restored, false otherwise.
     */
    public boolean restoreState(InputObjectState ios) {

        boolean result = super.restoreState(ios);

        coordinatorCompletionParticipant = (BusinessAgreementWithCoordinatorCompletionParticipant) participant;
        
        return false;
    }

    private BusinessAgreementWithCoordinatorCompletionParticipant coordinatorCompletionParticipant;
}