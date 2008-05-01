package com.arjuna.webservices11.wsat.client;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wsat.client.WSATClient;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.client.SoapFaultClient;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.oasis_open.docs.ws_tx.wsat._2006._06.CoordinatorPortType;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;

/**
 * The Client side of the Coordinator.
 * @author kevin
 */
public class CoordinatorClient
{
    /**
     * The client singleton.
     */
    private static final CoordinatorClient CLIENT = new CoordinatorClient() ;

    /**
     * The prepared action.
     */
    private AttributedURI preparedAction = null;
    /**
     * The aborted action.
     */
    private AttributedURI abortedAction = null;
    /**
     * The read only action.
     */
    private AttributedURI readOnlyAction = null;
    /**
     * The committed action.
     */
    private AttributedURI committedAction = null;
    /**
     * The fault action.
     */
    private AttributedURI faultAction = null;

    /**
     * The participant URI for replies.
     */
    private EndpointReference participant ;

    /**
     * Construct the coordinator client.
     */
    private CoordinatorClient()
    {
        final AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        try {
            preparedAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_PREPARED);
            abortedAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_ABORTED);
            readOnlyAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_READ_ONLY);
            committedAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_COMMITTED);
            faultAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_FAULT);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
        // final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        // AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        // ClientPolicy.register(handlerRegistry) ;

        final String participantURIString =
            ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.PARTICIPANT_SERVICE_NAME);
        try {
            URI participantURI = new URI(participantURIString);
            participant = builder.newEndpointReference(participantURI);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
    }

    /**
     * Send a prepared request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any SOAP errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendPrepared(final W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, participant, identifier);
        CoordinatorPortType port = getPort(endpoint, addressingProperties, preparedAction);
        Notification prepared = new Notification();

        port.preparedOperation(prepared);
    }

    /**
     * Send an aborted request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any SOAP errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendAborted(final W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFrom(addressingProperties, participant, identifier);
        CoordinatorPortType port = getPort(endpoint, addressingProperties, abortedAction);
        Notification aborted = new Notification();

        port.abortedOperation(aborted);
    }

    /**
     * Send a read only request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any SOAP errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendReadOnly(final W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFrom(addressingProperties, participant, identifier);
        CoordinatorPortType port = getPort(endpoint, addressingProperties, readOnlyAction);
        Notification readOnly = new Notification();

        port.readOnlyOperation(readOnly);
    }

    /**
     * Send a committed request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any SOAP errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCommitted(final W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFrom(addressingProperties, participant, identifier);
        CoordinatorPortType port = getPort(endpoint, addressingProperties, committedAction);
        Notification committed = new Notification();

        port.committedOperation(committed);
    }

    /**
     * Send a fault.
     * @param addressingProperties The addressing context.
     * @param soapFault The SOAP fault.
     * @param identifier The arjuna instance identifier.
     * @throws com.arjuna.webservices.SoapFault For any SOAP errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendSoapFault(final W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final SoapFault soapFault, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFrom(addressingProperties, participant, identifier);
        // use the SoapFaultService to format a soap fault and send it back to the faultto or from address
        SoapFaultClient.sendSoapFault((SoapFault11)soapFault, identifier, addressingProperties, faultAction);
    }

    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static CoordinatorClient getClient()
    {
        return CLIENT;
    }

    /**
     * obtain a port from the coordinator endpoint configured with the instance identifier handler and the supplied
     * addressing properties supplemented with the given action
     * @param endpoint
     * @param addressingProperties
     * @param action
     * @return
     */
    private CoordinatorPortType getPort(final W3CEndpointReference endpoint,
                                                final AddressingProperties addressingProperties,
                                                final AttributedURI action)
    {
        return WSATClient.getCoordinatorPort(endpoint, action, addressingProperties);
    }
}
