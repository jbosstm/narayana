package com.arjuna.webservices11.wsat.client;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.NativeEndpointReference;
import com.arjuna.webservices11.wsaddr.EndpointHelper;
import org.jboss.ws.api.addressing.MAPEndpoint;
import org.jboss.ws.api.addressing.MAPBuilder;
import org.jboss.ws.api.addressing.MAP;
import org.jboss.ws.api.addressing.MAPBuilderFactory;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;
import org.oasis_open.docs.ws_tx.wsat._2006._06.ParticipantPortType;
import org.xmlsoap.schemas.soap.envelope.Fault;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;

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
    private String prepareAction = null;
    /**
     * The commit action.
     */
    private String commitAction = null;
    /**
     * The rollback action.
     */
    private String rollbackAction = null;
    /**
     * The SOAP fault action.
     */
    private String faultAction;

    /**
     * The coordinator URI for replies.
     */
    private MAPEndpoint coordinator ;

    /**
     * The coordinator URI for secure replies.
     */
    private MAPEndpoint secureCoordinator ;

    /**
     * Construct the completion initiator client.
     */
    {
        final MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
        prepareAction = AtomicTransactionConstants.WSAT_ACTION_PREPARE;
        commitAction = AtomicTransactionConstants.WSAT_ACTION_COMMIT;
        rollbackAction = AtomicTransactionConstants.WSAT_ACTION_ROLLBACK;
        faultAction = AtomicTransactionConstants.WSAT_ACTION_FAULT;

        final String coordinatorURIString = ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COORDINATOR_SERVICE_NAME, false);
        final String secureCoordinatorURIString = ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COORDINATOR_SERVICE_NAME, true);
        coordinator = builder.newEndpoint(coordinatorURIString);
        secureCoordinator = builder.newEndpoint(secureCoordinatorURIString);
    }

    /**
     * Send a prepare request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendPrepare(final W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        ParticipantPortType port = getPort(endpoint, map, prepareAction);
        Notification prepare = new Notification();

        port.prepareOperation(prepare);
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
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        ParticipantPortType port = getPort(endpoint, map, commitAction);
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
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        ParticipantPortType port = getPort(endpoint, map, rollbackAction);
        Notification rollback = new Notification();

        port.rollbackOperation(rollback);
    }

    /**
     * Send a fault.
     * @param map The addressing context.
     * @param soapFault The SOAP fault.
     * @param identifier The arjuna instance identifier.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendSoapFault(final MAP map, final SoapFault soapFault, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        ParticipantPortType port = getPort(null, map, faultAction);
        // convert fault to the wire format and dispatch it to the initiator
        soapFault.setAction(faultAction) ;
        Fault fault = ((SoapFault11)soapFault).toFault();
        port.soapFault(fault);
    }

    /**
     * return a coordinator endpoint appropriate to the type of participant
     * @param endpoint
     * @return either the secure coordinator endpoint or the non-secure endpoint
     */
    MAPEndpoint getCoordinator(W3CEndpointReference endpoint, MAP map)
    {
        String address;
        if (endpoint != null) {
            NativeEndpointReference nativeRef = EndpointHelper.transform(NativeEndpointReference.class, endpoint);
            address = nativeRef.getAddress();
        } else {
            address = map.getTo();
        }
        
        if (address.startsWith("https")) {
            return secureCoordinator;
        } else {
            return coordinator;
        }
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
     * @param map
     * @param action
     * @return
     */
    private ParticipantPortType getPort(final W3CEndpointReference participant,
                                                final MAP map,
                                                final String action)
    {
        AddressingHelper.installNoneReplyTo(map);
        if (participant != null) {
            return WSATClient.getParticipantPort(participant, action, map);
        } else {
            return WSATClient.getParticipantPort(action, map);
        }
    }
}
