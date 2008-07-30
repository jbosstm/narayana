package org.jboss.jbossts.xts11.recovery.participant.at;

import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst11.messaging.engines.ParticipantEngine;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.webservices11.wsat.State;
import com.arjuna.webservices11.wsat.processors.ParticipantProcessor;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

/**
 * recovery record specific to WS-AT 1.1 protocol participants. this implements the behaviours
 * necessary to save and restore a 1.1 participant to or from the TX object store
 */
public class ATParticipantRecoveryRecord extends org.jboss.jbossts.xts.recovery.participant.at.ATParticipantRecoveryRecord {

    /**
     * constructor used during prepare processing to create an AT 1.1 participant record
     * for saving to the object store
     * @param id the id of the application-specific participant
     * @param participant the application-specific participant
     * @param
     */
    public ATParticipantRecoveryRecord(String id, Durable2PCParticipant participant, W3CEndpointReference endpoint)
    {
        super(id, participant);
        this.endpoint = endpoint;
    }

    /**
     * constructor used during recovery processing to create a record whose contents will be
     * recovered from the object store
     */
    public ATParticipantRecoveryRecord()
    {
        super(null, null);
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
        ParticipantEngine engine = new ParticipantEngine(participant, id, State.STATE_PREPARED_SUCCESS, endpoint, true);
        ParticipantProcessor.getProcessor().activateParticipant(engine, getId());
    }

    /**
     * test whether a participant is currently activated with the id of this recovery record.
     *
     * @return true if a participant is currently activated with the id of this recovery record
     */
    public boolean isActive()
    {
        return ParticipantProcessor.getProcessor().isActive(getId());
    }


    private W3CEndpointReference endpoint;
}