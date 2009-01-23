package org.jboss.jbossts.xts10.recovery.participant.ba;

import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine;
import com.arjuna.wst.messaging.engines.CoordinatorCompletionParticipantEngine;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsba.State;
import com.arjuna.webservices.util.StreamHelper;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionParticipantProcessor;

import javax.xml.stream.*;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.io.IOException;
import java.io.StringReader;

/**
 * recovery record specific to WS-BA 1.0 protocol participants. this implements the behaviours
 * necessary to save and restore a 1.0 participant to or from the TX object store
 */
public class BAParticipantRecoveryRecord extends org.jboss.jbossts.xts.recovery.participant.ba.BAParticipantRecoveryRecord {

    /**
     * constructor used during prepare processing to create an AT 1.0 participant record
     * for saving to the object store
     * @param id the id of the application-specific participant
     * @param participant the application-specific participant
     * @param
     */
    public BAParticipantRecoveryRecord(String id, BusinessAgreementWithParticipantCompletionParticipant participant, boolean isParticipantCompletion, EndpointReferenceType endpoint)
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
    protected void saveEndpointReference(OutputObjectState oos) throws IOException, XMLStreamException{
        // save an XML representation of the endpoint
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringWriter);
        StreamHelper.writeStartElement(writer, QNAME_SAGAS_COORDINATOR) ;
        endpoint.writeContent(writer);
        StreamHelper.writeEndElement(writer, null, null) ;
        writer.close();
        oos.packString(stringWriter.toString());
    }

    /**
     * restore the endpoint reference to the coordinator for this participant
     */
    protected void restoreEndpointReference(InputObjectState ios) throws IOException, XMLStreamException{
        // restore and parse the XML representation of the endpoint
        String xmlEndpoint = ios.unpackString();
        StringReader stringReader = new StringReader(xmlEndpoint);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(stringReader);
        StreamHelper.checkNextStartTag(reader, QNAME_SAGAS_COORDINATOR) ;
        endpoint = new EndpointReferenceType(reader);
    }

    /**
     * create a participant engine to manage commit or rollback processing for the
     * participant and install it in the active participants table
     */
    public void activate() {
        if (isParticipantCompletion) {
            ParticipantCompletionParticipantEngine engine = new ParticipantCompletionParticipantEngine(id, endpoint, participant, State.STATE_COMPLETED, true);
            ParticipantCompletionParticipantProcessor.getProcessor().activateParticipant(engine, getId());
            engine.recovery();
        } else {
            BusinessAgreementWithCoordinatorCompletionParticipant coordinatorCompletionParticipant = (BusinessAgreementWithCoordinatorCompletionParticipant) participant;
            CoordinatorCompletionParticipantEngine engine = new CoordinatorCompletionParticipantEngine(id, endpoint, coordinatorCompletionParticipant, State.STATE_COMPLETED, true);
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

    private static final QName QNAME_SAGAS_COORDINATOR = new QName("sagasCoordinator") ;

    private EndpointReferenceType endpoint;
}