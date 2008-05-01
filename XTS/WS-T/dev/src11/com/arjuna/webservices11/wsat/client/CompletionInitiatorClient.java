package com.arjuna.webservices11.wsat.client;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wsat.client.WSATClient;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.client.SoapFaultClient;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.oasis_open.docs.ws_tx.wsat._2006._06.CompletionInitiatorPortType;
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
 * The Client side of the Completion Initiator.
 * @author kevin
 */
public class CompletionInitiatorClient
{
    /**
     * The client singleton.
     */
    private static final CompletionInitiatorClient CLIENT = new CompletionInitiatorClient() ;

    /**
     * The committed action.
     */
    private AttributedURI committedAction = null;
    /**
     * The aborted action.
     */
    private AttributedURI abortedAction = null;
    /**
     * The fault action.
     */
    private AttributedURI faultAction = null;

    /**
     * The completion coordinator URI for replies.
     */
    private EndpointReference completionCoordinator ;

    /**
     * Construct the completion initiator client.
     */
    private CompletionInitiatorClient()
    {
        final AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        try {
            committedAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_COMMITTED);
            abortedAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_ABORTED);
            faultAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_FAULT);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
        // final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        // AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        // ClientPolicy.register(handlerRegistry) ;

        final String completionCoordinatorURIString =
            ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COMPLETION_COORDINATOR_SERVICE_NAME) ;
        try {
            URI completionCoordinatorURI = new URI(completionCoordinatorURIString) ;
            completionCoordinator = builder.newEndpointReference(completionCoordinatorURI);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
    }

    /**
     * Send a committed request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCommitted(final W3CEndpointReference participant, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFrom(addressingProperties, completionCoordinator, identifier);
        CompletionInitiatorPortType port = getPort(participant, addressingProperties, committedAction);
        Notification commited = new Notification();

        port.committedOperation(commited);
    }

    /**
     * Send an aborted request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendAborted(final W3CEndpointReference participant, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFrom(addressingProperties, completionCoordinator, identifier);
        CompletionInitiatorPortType port = getPort(participant, addressingProperties, abortedAction);
        Notification aborted = new Notification();

        port.abortedOperation(aborted);
    }

    /**
     * Send a fault.
     * @param addressingProperties The addressing context.
     * @param soapFault The SOAP fault.
     * @param identifier The arjuna instance identifier.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendSoapFault(final W3CEndpointReference participant, final AddressingProperties addressingProperties, final SoapFault soapFault, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFrom(addressingProperties, completionCoordinator, identifier);
        // use the SoapFaultService to format a soap fault and send it back to the faultto or from address
        SoapFaultClient.sendSoapFault((SoapFault11)soapFault, identifier, addressingProperties, faultAction);
    }

    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static CompletionInitiatorClient getClient()
    {
        return CLIENT;
    }

    /**
     * obtain a port from the completion participant endpoint configured with the instance identifier handler and the supplied
     * addressing properties supplemented with the given action
     * @param participant
     * @param addressingProperties
     * @param action
     * @return
     */
    private CompletionInitiatorPortType getPort(final W3CEndpointReference participant,
                                                final AddressingProperties addressingProperties,
                                                final AttributedURI action)
    {
        addressingProperties.setFrom(completionCoordinator);
        return WSATClient.getCompletionInitiatorPort(participant, action, addressingProperties);
    }
}
