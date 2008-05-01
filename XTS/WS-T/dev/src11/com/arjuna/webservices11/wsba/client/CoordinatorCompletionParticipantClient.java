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
import org.oasis_open.docs.ws_tx.wsba._2006._06.BusinessAgreementWithCoordinatorCompletionParticipantPortType;
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
 * The Client side of the Coordinator Participant Coordinator.
 * @author kevin
 */
public class CoordinatorCompletionParticipantClient
{
    /**
     * The client singleton.
     */
    private static final CoordinatorCompletionParticipantClient CLIENT = new CoordinatorCompletionParticipantClient() ;

    /**
     * The complete action.
     */
    private AttributedURI completeAction = null;
    /**
     * The close action.
     */
    private AttributedURI closeAction = null;
    /**
     * The cancel action.
     */
    private AttributedURI cancelAction = null;
    /**
     * The compensat action.
     */
    private AttributedURI compensateAction = null;
    /**
     * The faulted action.
     */
    private AttributedURI failedAction = null;
    /**
     * The exited action.
     */
    private AttributedURI exitedAction = null;
    /**
     * The not completed action.
     */
    private AttributedURI notCompletedAction = null;
    /**
     * The get status action.
     */
    private AttributedURI getStatusAction = null;
    /**
     * The status action.
     */
    private AttributedURI statusAction = null;

    /**
     * The coordinator completion coordinator URI for replies.
     */
    private EndpointReference coordinatorCompletionCoordinator = null;

    /**
     * Construct the participant completion coordinator client.
     */
    private CoordinatorCompletionParticipantClient()
    {
        final AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        try {
            completeAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_COMPLETE);
            closeAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_CLOSE);
            cancelAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_CANCEL);
            compensateAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_COMPENSATE);
            failedAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_FAILED);
            exitedAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_EXITED);
            notCompletedAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_NOT_COMPLETED);
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

        final String coordinatorCompletionCoordinatorURIString =
            ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_SERVICE_NAME) ;
        try {
            URI coordinatorCompletionCoordinatorURI = new URI(coordinatorCompletionCoordinatorURIString) ;
            coordinatorCompletionCoordinator = builder.newEndpointReference(coordinatorCompletionCoordinatorURI);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
    }

    /**
     * Send a complete request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendComplete(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, coordinatorCompletionCoordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, addressingProperties, completeAction);
        NotificationType complete = new NotificationType();

        port.completeOperation(complete);
    }

    /**
     * Send a close request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendClose(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, coordinatorCompletionCoordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, addressingProperties, closeAction);
        NotificationType close = new NotificationType();

        port.closeOperation(close);
    }

    /**
     * Send a cancel request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCancel(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, coordinatorCompletionCoordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, addressingProperties, cancelAction);
        NotificationType cancel = new NotificationType();

        port.cancelOperation(cancel);
    }

    /**
     * Send a compensate request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCompensate(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, coordinatorCompletionCoordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, addressingProperties, compensateAction);
        NotificationType compensate = new NotificationType();

        port.compensateOperation(compensate);
    }

    /**
     * Send a failed request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendFailed(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFrom(addressingProperties, coordinatorCompletionCoordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, addressingProperties, failedAction);
        NotificationType failed = new NotificationType();

        port.failedOperation(failed);
    }

    /**
     * Send an exited request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendExited(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFrom(addressingProperties, coordinatorCompletionCoordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, addressingProperties, exitedAction);
        NotificationType exit = new NotificationType();

        port.exitedOperation(exit);
    }

    /**
     * Send an exited request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendNotCompleted(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFrom(addressingProperties, coordinatorCompletionCoordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, addressingProperties, notCompletedAction);
        NotificationType notCompleted = new NotificationType();

        port.notCompleted(notCompleted);
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
        AddressingHelper.installFrom(addressingProperties, coordinatorCompletionCoordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
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
        AddressingHelper.installFrom(addressingProperties, coordinatorCompletionCoordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, addressingProperties, statusAction);
        StatusType status = new StatusType();
        status.setState(state);

        port.statusOperation(status);
    }

    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static CoordinatorCompletionParticipantClient getClient()
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
    private BusinessAgreementWithCoordinatorCompletionParticipantPortType
    getPort(final W3CEndpointReference participant, final AddressingProperties addressingProperties, final AttributedURI action)
    {
        addressingProperties.setFrom(coordinatorCompletionCoordinator);
        if (participant != null) {
            return WSBAClient.getCoordinatorCompletionParticipantPort(participant, action, addressingProperties);
        } else {
            return WSBAClient.getCoordinatorCompletionParticipantPort(action, addressingProperties);
        }
    }
}