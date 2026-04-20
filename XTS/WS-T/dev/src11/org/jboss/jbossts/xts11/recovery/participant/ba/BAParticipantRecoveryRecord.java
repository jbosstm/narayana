package org.jboss.jbossts.xts11.recovery.participant.ba;

import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine;
import com.arjuna.wst11.messaging.engines.CoordinatorCompletionParticipantEngine;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

/**
 * recovery record specific to WS-AT 1.1 protocol participants. this implements the behaviours
 * necessary to save and restore a 1.1 participant to or from the TX object store
 */
public class BAParticipantRecoveryRecord extends org.jboss.jbossts.xts.recovery.participant.ba.BAParticipantRecoveryRecord {

    /**
     * constructor used during prepare processing to create an AT 1.1 participant record
     * for saving to the object store
     * @param id the id of the application-specific participant
     * @param participant the application-specific participant
     * @param
     */
    public BAParticipantRecoveryRecord(String id, BusinessAgreementWithParticipantCompletionParticipant participant, boolean isParticipantCompletion, W3CEndpointReference endpoint)
    {
        super(id, participant, isParticipantCompletion);
        this.endpoint = endpoint;
    }

    /**
     * constructor used during recovery processing to create a record whose contents will be
     * recovered from the object store
     */
    public BAParticipantRecoveryRecord()
    {
        super(null, null, false);
    }

    /**
     * save the endpoint reference to the coordinator for this participant
     */
    protected void saveEndpointReference(OutputObjectState oos) throws IOException {
        // the toString method will do what we need
        oos.packString(endpoint.toString());
    }

    /**
     * restore the endpoint reference to the coordinator for this participant
     */
    protected void restoreEndpointReference(InputObjectState ios) throws IOException {
        String endpointString = ios.unpackString();
        Source source = new StreamSource(new StringReader(endpointString));
        endpoint = new W3CEndpointReference(source);
    }

    /**
     * create a participant engine to manage commit or rollback processing for the
     * participant and install it in the active participants table
     */
    public void activate() {
        if (isParticipantCompletion) {
            ParticipantCompletionParticipantEngine engine = new ParticipantCompletionParticipantEngine(id, endpoint,  participant, State.STATE_COMPLETED, true);
            ParticipantCompletionParticipantProcessor.getProcessor().activateParticipant(engine, getId());
            engine.recovery();
        } else {
            BusinessAgreementWithCoordinatorCompletionParticipant coordinatorCompletionParticipant = (BusinessAgreementWithCoordinatorCompletionParticipant) participant;
            CoordinatorCompletionParticipantEngine engine = new CoordinatorCompletionParticipantEngine(id, endpoint,  coordinatorCompletionParticipant, State.STATE_COMPLETED, true);
            CoordinatorCompletionParticipantProcessor.getProcessor().activateParticipant(engine, getId());
            engine.recovery();
        }
    }

    /**
     * test whether a participant is currently activated with the id of this recovery record.
     *
     * @return true if a participant is currently activated with the id of this recovery record
     */
    public boolean isActive()
    {
        if (isParticipantCompletion) {
            return ParticipantCompletionParticipantProcessor.getProcessor().isActive(getId());
        } else {
            return CoordinatorCompletionParticipantProcessor.getProcessor().isActive(getId());
        }
    }


    private W3CEndpointReference endpoint;
}