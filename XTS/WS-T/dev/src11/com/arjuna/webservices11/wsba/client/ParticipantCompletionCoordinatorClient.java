/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
import org.oasis_open.docs.ws_tx.wsba._2006._06.BusinessAgreementWithParticipantCompletionCoordinatorPortType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;

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
    private AttributedURI completedAction = null;
    /**
     * The fault action.
     */
    private AttributedURI failAction = null;
    /**
     * The compensated action.
     */
    private AttributedURI compensatedAction = null;
    /**
     * The closed action.
     */
    private AttributedURI closedAction = null;
    /**
     * The cancelled action.
     */
    private AttributedURI cancelledAction = null;
    /**
     * The exit action.
     */
    private AttributedURI exitAction = null;
    /**
     * The cannot complete action.
     */
    private AttributedURI cannotCompleteAction = null;
    /**
     * The get status action.
     */
    private AttributedURI getStatusAction = null;
    /**
     * The status action.
     */
    private AttributedURI statusAction = null;

    /**
     * The participant completion participant URI for replies.
     */
    private EndpointReference participantCompletionParticipant = null;

    /**
     * Construct the participant completion coordinator client.
     */
    private ParticipantCompletionCoordinatorClient()
    {
        final AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        try {
            completedAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_COMPLETED);
            failAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_FAIL);
            compensatedAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_COMPENSATED);
            closedAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_CLOSED);
            cancelledAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_CANCELLED);
            exitAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_EXIT);
            cannotCompleteAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_CANNOT_COMPLETE);
            getStatusAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_GET_STATUS);
            statusAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_STATUS);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
        // final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        // AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        // ClientPolicy.register(handlerRegistry) ;

        final String participantCompletionParticipantURIString =
            ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.PARTICIPANT_COMPLETION_PARTICIPANT_SERVICE_NAME) ;
        try {
            URI participantCompletionParticipantURI = new URI(participantCompletionParticipantURIString) ;
            participantCompletionParticipant = builder.newEndpointReference(participantCompletionParticipantURI);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
    }

    /**
     * Send a completed request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCompleted(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromFaultTo(addressingProperties, participantCompletionParticipant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, completedAction);
        NotificationType completed = new NotificationType();

        port.completedOperation(completed);
    }

    /**
     * Send a fault request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendFail(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier,
        final QName exceptionIdentifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromFaultTo(addressingProperties, participantCompletionParticipant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, failAction);
        ExceptionType fail = new ExceptionType();
        fail.setExceptionIdentifier(exceptionIdentifier);

        port.failOperation(fail);
    }

    /**
     * Send a compensated request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCompensated(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFaultTo(addressingProperties, participantCompletionParticipant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, compensatedAction);
        NotificationType compensated = new NotificationType();

        port.compensatedOperation(compensated);
    }

    /**
     * Send a closed request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendClosed(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFaultTo(addressingProperties, participantCompletionParticipant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, closedAction);
        NotificationType closed = new NotificationType();

        port.closedOperation(closed);
    }

    /**
     * Send a cancelled request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCancelled(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFaultTo(addressingProperties, participantCompletionParticipant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, cancelledAction);
        NotificationType cancelled = new NotificationType();

        port.canceledOperation(cancelled);
    }

    /**
     * Send an exit request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendExit(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromFaultTo(addressingProperties, participantCompletionParticipant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, exitAction);
        NotificationType exit = new NotificationType();

        port.exitOperation(exit);
    }

    /**
     * Send a cannot complete request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCannotComplete(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromFaultTo(addressingProperties, participantCompletionParticipant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, cannotCompleteAction);
        NotificationType cannotComplete = new NotificationType();

        port.cannotComplete(cannotComplete);
    }

    /**
     * Send a get status request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendGetStatus(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromFaultTo(addressingProperties, participantCompletionParticipant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, getStatusAction);
        NotificationType getStatus = new NotificationType();

        port.getStatusOperation(getStatus);
    }

    /**
     * Send a status request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendStatus(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier,
        final QName state)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromFaultTo(addressingProperties, participantCompletionParticipant, identifier);
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, statusAction);
        StatusType status = new StatusType();
        status.setState(state);

        port.statusOperation(status);
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
     * @param addressingProperties
     * @param action
     * @return
     */
    private BusinessAgreementWithParticipantCompletionCoordinatorPortType
    getPort(final W3CEndpointReference participant, final AddressingProperties addressingProperties, final AttributedURI action)
    {
        AddressingHelper.installNoneReplyTo(addressingProperties);
        if (participant != null) {
            return WSBAClient.getParticipantCompletionCoordinatorPort(participant, action, addressingProperties);
        } else {
            return WSBAClient.getParticipantCompletionCoordinatorPort(action, addressingProperties);
        }
    }
}