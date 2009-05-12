/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices11.wsba.client;

import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsba.client.WSBAClient;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.NativeEndpointReference;
import com.arjuna.webservices11.wsaddr.EndpointHelper;
import com.arjuna.webservices11.wsaddr.map.MAPEndpoint;
import com.arjuna.webservices11.wsaddr.map.MAPBuilder;
import com.arjuna.webservices11.wsaddr.map.MAP;
import org.oasis_open.docs.ws_tx.wsba._2006._06.BusinessAgreementWithParticipantCompletionCoordinatorPortType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;

/**
 * The Client side of the Participant Completion Coordinator.
 * @author kevin
 */
public class ParticipantCompletionCoordinatorClient
{
    /**
     * The client singleton.
     */
    private static final ParticipantCompletionCoordinatorClient CLIENT = new ParticipantCompletionCoordinatorClient() ;

    /**
     * The completed action.
     */
    private String completedAction = null;
    /**
     * The fault action.
     */
    private String failAction = null;
    /**
     * The compensated action.
     */
    private String compensatedAction = null;
    /**
     * The closed action.
     */
    private String closedAction = null;
    /**
     * The cancelled action.
     */
    private String cancelledAction = null;
    /**
     * The exit action.
     */
    private String exitAction = null;
    /**
     * The cannot complete action.
     */
    private String cannotCompleteAction = null;
    /**
     * The get status action.
     */
    private String getStatusAction = null;
    /**
     * The status action.
     */
    private String statusAction = null;

    /**
     * The participant completion participant URI for replies.
     */
    private MAPEndpoint participantCompletionParticipant = null;

    /**
     * The participant completion participant URI for secure replies.
     */
    private MAPEndpoint secureParticipantCompletionParticipant = null;

    /**
     * Construct the participant completion coordinator client.
     */
    private ParticipantCompletionCoordinatorClient()
    {
        final MAPBuilder builder = MAPBuilder.getBuilder();
        completedAction = BusinessActivityConstants.WSBA_ACTION_COMPLETED;
        failAction = BusinessActivityConstants.WSBA_ACTION_FAIL;
        compensatedAction = BusinessActivityConstants.WSBA_ACTION_COMPENSATED;
        closedAction = BusinessActivityConstants.WSBA_ACTION_CLOSED;
        cancelledAction = BusinessActivityConstants.WSBA_ACTION_CANCELLED;
        exitAction = BusinessActivityConstants.WSBA_ACTION_EXIT;
        cannotCompleteAction = BusinessActivityConstants.WSBA_ACTION_CANNOT_COMPLETE;
        getStatusAction = BusinessActivityConstants.WSBA_ACTION_GET_STATUS;
        statusAction = BusinessActivityConstants.WSBA_ACTION_STATUS;

        final String participantCompletionParticipantURIString =
            ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.PARTICIPANT_COMPLETION_PARTICIPANT_SERVICE_NAME, false) ;
        final String secureParticipantCompletionParticipantURIString =
            ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.PARTICIPANT_COMPLETION_PARTICIPANT_SERVICE_NAME, true) ;
        participantCompletionParticipant = builder.newEndpoint(participantCompletionParticipantURIString);
        secureParticipantCompletionParticipant = builder.newEndpoint(secureParticipantCompletionParticipantURIString);
    }

    /**
     * Send a completed request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCompleted(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFromFaultTo(map, participant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, map, completedAction);
        NotificationType completed = new NotificationType();

        port.completedOperation(completed);
    }

    /**
     * Send a fault request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendFail(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier,
        final QName exceptionIdentifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFromFaultTo(map, participant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, map, failAction);
        ExceptionType fail = new ExceptionType();
        fail.setExceptionIdentifier(exceptionIdentifier);

        port.failOperation(fail);
    }

    /**
     * Send a compensated request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCompensated(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFaultTo(map, participant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, map, compensatedAction);
        NotificationType compensated = new NotificationType();

        port.compensatedOperation(compensated);
    }

    /**
     * Send a closed request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendClosed(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFaultTo(map, participant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, map, closedAction);
        NotificationType closed = new NotificationType();

        port.closedOperation(closed);
    }

    /**
     * Send a cancelled request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCancelled(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFaultTo(map, participant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, map, cancelledAction);
        NotificationType cancelled = new NotificationType();

        port.canceledOperation(cancelled);
    }

    /**
     * Send an exit request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendExit(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFromFaultTo(map, participant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, map, exitAction);
        NotificationType exit = new NotificationType();

        port.exitOperation(exit);
    }

    /**
     * Send a cannot complete request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCannotComplete(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFromFaultTo(map, participant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, map, cannotCompleteAction);
        NotificationType cannotComplete = new NotificationType();

        port.cannotComplete(cannotComplete);
    }

    /**
     * Send a get status request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendGetStatus(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFromFaultTo(map, participant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, map, getStatusAction);
        NotificationType getStatus = new NotificationType();

        port.getStatusOperation(getStatus);
    }

    /**
     * Send a status request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendStatus(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier,
        final QName state)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(endpoint, map);
        AddressingHelper.installFromFaultTo(map, participant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, map, statusAction);
        StatusType status = new StatusType();
        status.setState(state);

        port.statusOperation(status);
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
            return secureParticipantCompletionParticipant;
        } else {
            return participantCompletionParticipant;
        }
    }

    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static ParticipantCompletionCoordinatorClient getClient()
    {
        return CLIENT;
    }

    /**
     * obtain a port from the coordinator endpoint configured with the instance identifier handler and the supplied
     * addressing properties supplemented with the given action
     * @param participant
     * @param map
     * @param action
     * @return
     */
    private BusinessAgreementWithParticipantCompletionCoordinatorPortType
    getPort(final W3CEndpointReference participant, final MAP map, final String action)
    {
        AddressingHelper.installNoneReplyTo(map);
        if (participant != null) {
            return WSBAClient.getParticipantCompletionCoordinatorPort(participant, action, map);
        } else {
            return WSBAClient.getParticipantCompletionCoordinatorPort(action, map);
        }
    }
}