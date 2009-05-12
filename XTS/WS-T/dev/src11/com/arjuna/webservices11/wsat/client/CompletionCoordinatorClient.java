package com.arjuna.webservices11.wsat.client;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.NativeEndpointReference;
import com.arjuna.webservices11.wsaddr.EndpointHelper;
import com.arjuna.webservices11.wsaddr.map.MAPEndpoint;
import com.arjuna.webservices11.wsaddr.map.MAPBuilder;
import com.arjuna.webservices11.wsaddr.map.MAP;
import org.oasis_open.docs.ws_tx.wsat._2006._06.CompletionCoordinatorPortType;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;

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
    private String commitAction = null;
    /**
     * The rollback action.
     */
    private String rollbackAction = null;

    /**
     * The completion initiator URI for replies.
     */
    private MAPEndpoint completionInitiator ;

    /**
     * The completion initiator URI for secure replies.
     */
    private MAPEndpoint secureCompletionInitiator ;

    /**
     * Construct the completion coordinator client.
     */
    private CompletionCoordinatorClient()
    {
        final MAPBuilder builder = MAPBuilder.getBuilder();
            commitAction = AtomicTransactionConstants.WSAT_ACTION_COMMIT;
            rollbackAction = AtomicTransactionConstants.WSAT_ACTION_ROLLBACK;
        // final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        // AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        // ClientPolicy.register(handlerRegistry) ;

        final String completionInitiatorURIString =
            ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_NAME, false) ;
        final String secureCompletionInitiatorURIString =
            ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_NAME, true) ;
        completionInitiator = builder.newEndpoint(completionInitiatorURIString);
        secureCompletionInitiator = builder.newEndpoint(secureCompletionInitiatorURIString);
    }

    /**
     * Send a commit request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCommit(final W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint initiator = getCompletionInitiator(endpoint);
        AddressingHelper.installFromFaultTo(map, initiator, identifier);
        CompletionCoordinatorPortType port = getPort(endpoint, map, commitAction);
        Notification commit = new Notification();

        port.commitOperation(commit);
    }

    /**
     * Send a rollback request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendRollback(final W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint initiator = getCompletionInitiator(endpoint);
        AddressingHelper.installFromFaultTo(map, initiator, identifier);
        CompletionCoordinatorPortType port = getPort(endpoint, map, rollbackAction);
        Notification rollback = new Notification();
                
        port.rollbackOperation(rollback);
    }

    /**
     * return a completion initiator endpoint appropriate to the type of completion coordinator
     * @param participant
     * @return either the secure terminaton participant endpoint or the non-secure endpoint
     */
    MAPEndpoint getCompletionInitiator(W3CEndpointReference participant)
    {
        NativeEndpointReference nativeRef = EndpointHelper.transform(NativeEndpointReference.class, participant);
        String address = nativeRef.getAddress();
        if (address.startsWith("https")) {
            return secureCompletionInitiator;
        } else {
            return completionInitiator;
        }
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
     * @param map
     * @param action
     * @return
     */
    private CompletionCoordinatorPortType getPort(final W3CEndpointReference endpoint,
                                                  final MAP map,
                                                  final String action)
    {
        AddressingHelper.installNoneReplyTo(map);
        return WSATClient.getCompletionCoordinatorPort(endpoint, action, map);
    }
}
