package com.arjuna.webservices11.wsat.client;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wsat.client.WSATClient;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.client.SoapFaultClient;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;
import org.oasis_open.docs.ws_tx.wsat._2006._06.ParticipantPortType;

import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;

/**
 * The Client side of the Participant.
 * @author kevin
 */
public class ParticipantClient
{
    /**
     * The client singleton.
     */
    private static final ParticipantClient CLIENT = new ParticipantClient() ;

    /**
     * The prepare action.
     */
    private AttributedURI prepareAction = null;
    /**
     * The commit action.
     */
    private AttributedURI commitAction = null;
    /**
     * The rollback action.
     */
    private AttributedURI rollbackAction = null;
    /**
     * The SOAP fault action.
     */
    private AttributedURI faultAction;

    /**
     * The coordinator URI for replies.
     */
    private EndpointReference coordinator ;

    /**
     * Construct the completion initiator client.
     */
    {
        final AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        try {
            prepareAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_PREPARE);
            commitAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_COMMIT);
            rollbackAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_ROLLBACK);
            faultAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_FAULT);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
        // final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        // AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        // ClientPolicy.register(handlerRegistry) ;

        final String coordinatorURIString = ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COORDINATOR_SERVICE_NAME);
        try {
            URI coordinatorURI = new URI(coordinatorURIString);
            coordinator = builder.newEndpointReference(coordinatorURI);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }

    }

    /**
     * Send a prepare request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendPrepare(final W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, coordinator, identifier);
        ParticipantPortType port = getPort(endpoint, addressingProperties, prepareAction);
        Notification prepare = new Notification();

        port.prepareOperation(prepare);
    }

    /**
     * Send a commit request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCommit(final W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, coordinator, identifier);
        ParticipantPortType port = getPort(endpoint, addressingProperties, commitAction);
        Notification commit = new Notification();

        port.commitOperation(commit);
    }

    /**
     * Send a rollback request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendRollback(final W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, coordinator, identifier);
        ParticipantPortType port = getPort(endpoint, addressingProperties, rollbackAction);
        Notification rollback = new Notification();

        port.rollbackOperation(rollback);
    }

    /**
     * Send a fault.
     * @param addressingProperties The addressing context.
     * @param soapFault The SOAP fault.
     * @param identifier The arjuna instance identifier.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendSoapFault(final AddressingProperties addressingProperties, final SoapFault soapFault, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        // use the SoapFaultService to format a soap fault and send it back to the faultto or from address
        AddressingHelper.installFrom(addressingProperties, coordinator, identifier);
        SoapFaultClient.sendSoapFault((SoapFault11)soapFault, addressingProperties, faultAction);
    }

    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static ParticipantClient getClient()
    {
        return CLIENT;
    }

    /**
     * obtain a port from the participant endpoint configured with the instance identifier handler and the supplied
     * addressing properties supplemented with the given action
     * @param participant
     * @param addressingProperties
     * @param action
     * @return
     */
    private ParticipantPortType getPort(final W3CEndpointReference participant,
                                                final AddressingProperties addressingProperties,
                                                final AttributedURI action)
    {
        addressingProperties.setFrom(coordinator);
        if (participant != null) {
            return WSATClient.getParticipantPort(participant, action, addressingProperties);
        } else {
            return WSATClient.getParticipantPort(action, addressingProperties);
        }
    }
}
