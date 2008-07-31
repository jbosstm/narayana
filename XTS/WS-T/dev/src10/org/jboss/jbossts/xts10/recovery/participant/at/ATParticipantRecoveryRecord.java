package org.jboss.jbossts.xts10.recovery.participant.at;

import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.messaging.engines.ParticipantEngine;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsat.State;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices.util.StreamHelper;

import javax.xml.stream.*;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.io.IOException;
import java.io.StringReader;

/**
 * recovery record specific to WS-AT 1.0 protocol participants. this implements the behaviours
 * necessary to save and restore a 1.0 participant to or from the TX object store
 */
public class ATParticipantRecoveryRecord extends org.jboss.jbossts.xts.recovery.participant.at.ATParticipantRecoveryRecord {

    /**
     * constructor used during prepare processing to create an AT 1.0 participant record
     * for saving to the object store
     * @param id the id of the application-specific participant
     * @param participant the application-specific participant
     * @param
     */
    public ATParticipantRecoveryRecord(String id, Durable2PCParticipant participant, EndpointReferenceType endpoint)
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
    protected void saveEndpointReference(OutputObjectState oos) throws IOException, XMLStreamException{
        // save an XML representation of the endpoint
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(stringWriter);
        StreamHelper.writeStartElement(writer, QNAME_TWO_PC_COORDINATOR) ;
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
        StreamHelper.checkNextStartTag(reader, QNAME_TWO_PC_COORDINATOR) ;
        endpoint = new EndpointReferenceType(reader);
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

    private static final QName QNAME_TWO_PC_COORDINATOR = new QName("twoPCCoordinator") ;

    private EndpointReferenceType endpoint;
}
