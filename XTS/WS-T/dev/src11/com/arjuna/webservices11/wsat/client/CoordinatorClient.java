package com.arjuna.webservices11.wsat.client;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.NativeEndpointReference;
import com.arjuna.webservices11.wsaddr.EndpointHelper;
import org.jboss.wsf.common.addressing.MAPEndpoint;
import org.jboss.wsf.common.addressing.MAPBuilder;
import org.jboss.wsf.common.addressing.MAP;
import org.jboss.wsf.common.addressing.MAPBuilderFactory;
import org.oasis_open.docs.ws_tx.wsat._2006._06.CoordinatorPortType;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;
import org.xmlsoap.schemas.soap.envelope.Fault;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;

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
    private String preparedAction = null;
    /**
     * The aborted action.
     */
    private String abortedAction = null;
    /**
     * The read only action.
     */
    private String readOnlyAction = null;
    /**
     * The committed action.
     */
    private String committedAction = null;
    /**
     * The fault action.
     */
    private String faultAction = null;

    /**
     * The participant URI for replies.
     */
    private MAPEndpoint participant ;

    /**
     * The participant URI for secure replies.
     */
    private MAPEndpoint secureParticipant ;

    /**
     * Construct the coordinator client.
     */
    private CoordinatorClient()
    {
        final MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
        preparedAction = AtomicTransactionConstants.WSAT_ACTION_PREPARED;
        abortedAction = AtomicTransactionConstants.WSAT_ACTION_ABORTED;
        readOnlyAction = AtomicTransactionConstants.WSAT_ACTION_READ_ONLY;
        committedAction = AtomicTransactionConstants.WSAT_ACTION_COMMITTED;
        faultAction = AtomicTransactionConstants.WSAT_ACTION_FAULT;

        final String participantURIString =
            ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.PARTICIPANT_SERVICE_NAME, false);
        final String secureParticipantURIString =
            ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.PARTICIPANT_SERVICE_NAME, true);
        participant = builder.newEndpoint(participantURIString);
        secureParticipant = builder.newEndpoint(secureParticipantURIString);
    }

    /**
     * Send a prepared request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any SOAP errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendPrepared(final W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFromFaultTo(map, participant, identifier);
        CoordinatorPortType port = getPort(endpoint, map, preparedAction);
        Notification prepared = new Notification();

        port.preparedOperation(prepared);
    }

    /**
     * Send an aborted request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any SOAP errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendAborted(final W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFaultTo(map, participant, identifier);
        CoordinatorPortType port = getPort(endpoint, map, abortedAction);
        Notification aborted = new Notification();

        port.abortedOperation(aborted);
    }

    /**
     * Send a read only request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any SOAP errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendReadOnly(final W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFaultTo(map, participant, identifier);
        CoordinatorPortType port = getPort(endpoint, map, readOnlyAction);
        Notification readOnly = new Notification();

        port.readOnlyOperation(readOnly);
    }

    /**
     * Send a committed request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any SOAP errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCommitted(final W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFaultTo(map, participant, identifier);
        CoordinatorPortType port = getPort(endpoint, map, committedAction);
        Notification committed = new Notification();

        port.committedOperation(committed);
    }

    /**
     * Send a fault.
     * @param map The addressing context.
     * @param soapFault The SOAP fault.
     * @param identifier The arjuna instance identifier.
     * @throws com.arjuna.webservices.SoapFault For any SOAP errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendSoapFault(final W3CEndpointReference endpoint, final MAP map, final SoapFault soapFault, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        CoordinatorPortType port = getPort(endpoint, map, faultAction);
        // convert fault to the wire format and dispatch it to the initiator
        soapFault.setAction(faultAction) ;
        Fault fault = ((SoapFault11)soapFault).toFault();
        port.soapFault(fault);
    }

    /**
     * return a participant endpoint appropriate to the type of coordinator
     * @param endpoint
     * @return either the secure participant endpoint or the non-secure endpoint
     */
    MAPEndpoint getParticipant(W3CEndpointReference endpoint, MAP map)
    {
        String address;
        if (endpoint != null) {
            NativeEndpointReference nativeRef = EndpointHelper.transform(NativeEndpointReference.class, endpoint);
            address = nativeRef.getAddress();
        } else {
            address = map.getTo();
        }

        if (address.startsWith("https")) {
            return secureParticipant;
        } else {
            return participant;
        }
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
     * @param map
     * @param action
     * @return
     */
    private CoordinatorPortType getPort(final W3CEndpointReference endpoint,
                                                final MAP map,
                                                final String action)
    {
        AddressingHelper.installNoneReplyTo(map);
        if (endpoint != null) {
            return WSATClient.getCoordinatorPort(endpoint, action, map);
        } else {
            return WSATClient.getCoordinatorPort(action, map);
        }
    }
}
