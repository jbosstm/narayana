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
package com.arjuna.webservices.wsba.client;

import java.io.IOException;

import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.base.policy.ClientPolicy;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsaddr.client.BaseWSAddrClient;
import com.arjuna.webservices.wsaddr.policy.AddressingPolicy;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.State;
import com.arjuna.webservices.wsba.StatusType;

/**
 * The Client side of the Participant Participant Coordinator.
 * @author kevin
 */
public class ParticipantCompletionParticipantClient extends BaseWSAddrClient
{
    /**
     * The client singleton.
     */
    private static final ParticipantCompletionParticipantClient CLIENT = new ParticipantCompletionParticipantClient() ;
    
    /**
     * The close action.
     */
    private final AttributedURIType closeAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_CLOSE) ;
    /**
     * The cancel action.
     */
    private final AttributedURIType cancelAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_CANCEL) ;
    /**
     * The compensat action.
     */
    private final AttributedURIType compensateAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_COMPENSATE) ;
    /**
     * The faulted action.
     */
    private final AttributedURIType faultedAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_FAULTED) ;
    /**
     * The exited action.
     */
    private final AttributedURIType exitedAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_EXITED) ;
    /**
     * The get status action.
     */
    private final AttributedURIType getStatusAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_GET_STATUS) ;
    /**
     * The status action.
     */
    private final AttributedURIType statusAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_STATUS) ;
    
    /**
     * The SOAP service representing the client.
     */
    private final SoapService soapService ;
    /**
     * The participant completion coordinator URI for replies.
     */
    private final AttributedURIType participantCompletionCoordinator ;
    
    /**
     * Construct the participant completion coordinator client.
     */
    private ParticipantCompletionParticipantClient()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        ClientPolicy.register(handlerRegistry) ;
        
        soapService = new SoapService(handlerRegistry) ;
        final String participantCompletionCoordinatorURI =
            SoapRegistry.getRegistry().getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_COORDINATOR) ;
        participantCompletionCoordinator = new AttributedURIType(participantCompletionCoordinatorURI) ;
    }
    
    /**
     * Send a close request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendClose(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, BusinessActivityConstants.WSBA_ELEMENT_CLOSE_QNAME,
            closeAction) ;
    }
    
    /**
     * Send a cancel request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCancel(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, BusinessActivityConstants.WSBA_ELEMENT_CANCEL_QNAME,
            cancelAction) ;
    }
    
    /**
     * Send a compensate request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCompensate(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, BusinessActivityConstants.WSBA_ELEMENT_COMPENSATE_QNAME,
            compensateAction) ;
    }
    
    /**
     * Send a faulted request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendFaulted(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
            null, BusinessActivityConstants.WSBA_ELEMENT_FAULTED_QNAME, faultedAction) ;
    }
    
    /**
     * Send an exited request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendExited(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
            null, BusinessActivityConstants.WSBA_ELEMENT_EXITED_QNAME, exitedAction) ;
    }
    
    /**
     * Send a get status request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendGetStatus(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, BusinessActivityConstants.WSBA_ELEMENT_GET_STATUS_QNAME,
            getStatusAction) ;
    }
    
    /**
     * Send a status request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendStatus(final AddressingContext addressingContext, final InstanceIdentifier identifier,
        final State state)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new StatusType(state), addressingContext, soapService, endpointReference,
                endpointReference, BusinessActivityConstants.WSBA_ELEMENT_STATUS_QNAME,
            statusAction) ;
    }
    
    /**
     * Get the endpoint reference for the specified identifier.
     * @param identifier The endpoint reference identifier.
     * @return The endpoint reference.
     */
    private EndpointReferenceType getEndpointReference(final InstanceIdentifier identifier)
    {
        final EndpointReferenceType participantCompletionCoordinatorEndpoint = new EndpointReferenceType(participantCompletionCoordinator) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionCoordinatorEndpoint, identifier) ;
        return participantCompletionCoordinatorEndpoint ;
    }
    
    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static ParticipantCompletionParticipantClient getClient()
    {
        return CLIENT ;
    }
}
