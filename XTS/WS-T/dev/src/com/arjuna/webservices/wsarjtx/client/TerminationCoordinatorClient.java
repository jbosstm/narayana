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
package com.arjuna.webservices.wsarjtx.client;

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
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsarjtx.NotificationType;

/**
 * The Client side of the Terminator Participant.
 * @author kevin
 */
public class TerminationCoordinatorClient extends BaseWSAddrClient
{
    /**
     * The client singleton.
     */
    private static final TerminationCoordinatorClient CLIENT = new TerminationCoordinatorClient() ;
    
    /**
     * The complete action.
     */
    private final AttributedURIType completeAction =
        new AttributedURIType(ArjunaTXConstants.WSARJTX_ACTION_COMPLETE) ;
    /**
     * The close action.
     */
    private final AttributedURIType closeAction =
        new AttributedURIType(ArjunaTXConstants.WSARJTX_ACTION_CLOSE) ;
    /**
     * The cancel action.
     */
    private final AttributedURIType cancelAction =
        new AttributedURIType(ArjunaTXConstants.WSARJTX_ACTION_CANCEL) ;
    /**
     * The SOAP fault action.
     */
    private final AttributedURIType soapFaultAction =
        new AttributedURIType(ArjunaTXConstants.WSARJTX_ACTION_SOAP_FAULT) ;
    
    /**
     * The SOAP service representing the client.
     */
    private final SoapService soapService ;
    /**
     * The termination participant URI for replies.
     */
    private final AttributedURIType terminationParticipant ;
    
    /**
     * Construct the terminator participant client.
     */
    private TerminationCoordinatorClient()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        ClientPolicy.register(handlerRegistry) ;
        
        soapService = new SoapService(handlerRegistry) ;
        final String terminationParticipantURI =
            SoapRegistry.getRegistry().getServiceURI(ArjunaTXConstants.SERVICE_TERMINATION_PARTICIPANT) ;
        terminationParticipant = new AttributedURIType(terminationParticipantURI) ;
    }
    
    /**
     * Send a complete request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendComplete(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, ArjunaTXConstants.WSARJTX_ELEMENT_COMPLETE_QNAME,
            completeAction) ;
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
                endpointReference, ArjunaTXConstants.WSARJTX_ELEMENT_CLOSE_QNAME,
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
                endpointReference, ArjunaTXConstants.WSARJTX_ELEMENT_CANCEL_QNAME,
            cancelAction) ;
    }

    /**
     * Send a fault.
     * @param addressingContext The addressing context.
     * @param soapFault The SOAP fault.
     * @param identifier The arjuna instance identifier.
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
        final EndpointReferenceType terminationParticipantEndpoint = new EndpointReferenceType(terminationParticipant) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(terminationParticipantEndpoint, identifier) ;
        return terminationParticipantEndpoint ;
    }
    
    /**
     * Get the Terminator Participant client singleton.
     * @return The Terminator Participant client singleton.
     */
    public static TerminationCoordinatorClient getClient()
    {
        return CLIENT ;
    }
}
