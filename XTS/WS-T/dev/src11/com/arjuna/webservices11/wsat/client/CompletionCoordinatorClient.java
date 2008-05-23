package com.arjuna.webservices11.wsat.client;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wsat.client.WSATClient;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.oasis_open.docs.ws_tx.wsat._2006._06.CompletionCoordinatorPortType;
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
 * The Client side of the Completion Coordinator.
 * @author kevin
 */
public class CompletionCoordinatorClient
{
    /**
     * The client singleton.
     */
    private static final CompletionCoordinatorClient CLIENT = new CompletionCoordinatorClient() ;

    /**
     * The commit action.
     */
    private AttributedURI commitAction = null;
    /**
     * The rollback action.
     */
    private AttributedURI rollbackAction = null;

    /**
     * The completion initiator URI for replies.
     */
    private EndpointReference completionInitiator ;

    /**
     * Construct the completion coordinator client.
     */
    private CompletionCoordinatorClient()
    {
        final AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        try {
            commitAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_COMMIT);
            rollbackAction = builder.newURI(AtomicTransactionConstants.WSAT_ACTION_ROLLBACK);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
        // final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        // AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        // ClientPolicy.register(handlerRegistry) ;

        final String completionInitiatorURIString =
            ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_NAME) ;
        try {
            URI completionInitiatorURI = new URI(completionInitiatorURIString) ;
            completionInitiator = builder.newEndpointReference(completionInitiatorURI);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
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
        AddressingHelper.installFromFaultTo(addressingProperties, completionInitiator, identifier);
        CompletionCoordinatorPortType port = getPort(endpoint, addressingProperties, commitAction);
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
        AddressingHelper.installFromFaultTo(addressingProperties, completionInitiator, identifier);
        CompletionCoordinatorPortType port = getPort(endpoint, addressingProperties, rollbackAction);
        Notification rollback = new Notification();
                
        port.rollbackOperation(rollback);
    }

    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static CompletionCoordinatorClient getClient()
    {
        return CLIENT;
    }

    /**
     * obtain a port from the completion coordinator endpoint configured with the instance identifier handler and the supplied
     * addressing properties supplemented with the given action
     * @param endpoint
     * @param addressingProperties
     * @param action
     * @return
     */
    private CompletionCoordinatorPortType getPort(final W3CEndpointReference endpoint,
                                                  final AddressingProperties addressingProperties,
                                                  final AttributedURI action)
    {
        AddressingHelper.installNoneReplyTo(addressingProperties);
        return WSATClient.getCompletionCoordinatorPort(endpoint, action, addressingProperties);
    }
}
