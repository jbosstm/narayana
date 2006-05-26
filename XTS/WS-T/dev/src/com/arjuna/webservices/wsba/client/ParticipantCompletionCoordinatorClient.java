/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
import com.arjuna.webservices.wsba.ExceptionType;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.State;
import com.arjuna.webservices.wsba.StatusType;

/**
 * The Client side of the Participant Completion Coordinator.
 * @author kevin
 */
public class ParticipantCompletionCoordinatorClient extends BaseWSAddrClient
{
    /**
     * The client singleton.
     */
    private static final ParticipantCompletionCoordinatorClient CLIENT = new ParticipantCompletionCoordinatorClient() ;
    
    /**
     * The completed action.
     */
    private final AttributedURIType completedAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_COMPLETED) ;
    /**
     * The fault action.
     */
    private final AttributedURIType faultAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_FAULT) ;
    /**
     * The compensated action.
     */
    private final AttributedURIType compensatedAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_COMPENSATED) ;
    /**
     * The closed action.
     */
    private final AttributedURIType closedAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_CLOSED) ;
    /**
     * The cancelled action.
     */
    private final AttributedURIType cancelledAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_CANCELLED) ;
    /**
     * The exit action.
     */
    private final AttributedURIType exitAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_EXIT) ;
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
     * The soapFault action.
     */
    private final AttributedURIType soapFaultAction =
        new AttributedURIType(BusinessActivityConstants.WSBA_ACTION_SOAP_FAULT) ;
    
    /**
     * The SOAP service representing the client.
     */
    private final SoapService soapService ;
    /**
     * The participant completion participant URI for replies.
     */
    private final AttributedURIType participantCompletionParticipant ;
    
    /**
     * Construct the participant completion coordinator client.
     */
    private ParticipantCompletionCoordinatorClient()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        ClientPolicy.register(handlerRegistry) ;
        
        soapService = new SoapService(handlerRegistry) ;
        final String participantCompletionParticipantURI =
            SoapRegistry.getRegistry().getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_PARTICIPANT) ;
        participantCompletionParticipant = new AttributedURIType(participantCompletionParticipantURI) ;
    }
    
    /**
     * Send a completed request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCompleted(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, BusinessActivityConstants.WSBA_ELEMENT_COMPLETED_QNAME,
            completedAction) ;
    }
    
    /**
     * Send a fault request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendFault(final AddressingContext addressingContext, final InstanceIdentifier identifier,
        final String exceptionIdentifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        final String exception = (exceptionIdentifier == null ? identifier.getInstanceIdentifier() : exceptionIdentifier) ;
        sendOneWay(new ExceptionType(exception), addressingContext, soapService, endpointReference,
                endpointReference, BusinessActivityConstants.WSBA_ELEMENT_FAULT_QNAME,
            faultAction) ;
    }
    
    /**
     * Send a compensated request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCompensated(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
            null, BusinessActivityConstants.WSBA_ELEMENT_COMPENSATED_QNAME, compensatedAction) ;
    }
    
    /**
     * Send a closed request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendClosed(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
            null, BusinessActivityConstants.WSBA_ELEMENT_CLOSED_QNAME, closedAction) ;
    }
    
    /**
     * Send a cancelled request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCancelled(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
            null, BusinessActivityConstants.WSBA_ELEMENT_CANCELLED_QNAME, cancelledAction) ;
    }
    
    /**
     * Send an exit request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendExit(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, BusinessActivityConstants.WSBA_ELEMENT_EXIT_QNAME,
            exitAction) ;
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
     * Send a fault.
     * @param addressingContext The addressing context.
     * @param soapFault The SOAP fault.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendSoapFault(final AddressingContext addressingContext, final SoapFault soapFault, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendSoapFault(soapFault, addressingContext, soapService, endpointReference, soapFaultAction) ;
    }
    
    /**
     * Get the endpoint reference for the specified identifier.
     * @param identifier The endpoint reference identifier.
     * @return The endpoint reference.
     */
    private EndpointReferenceType getEndpointReference(final InstanceIdentifier identifier)
    {
        final EndpointReferenceType participantCompletionParticipantEndpoint = new EndpointReferenceType(participantCompletionParticipant) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionParticipantEndpoint, identifier) ;
        return participantCompletionParticipantEndpoint ;
    }
    
    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static ParticipantCompletionCoordinatorClient getClient()
    {
        return CLIENT ;
    }
}
