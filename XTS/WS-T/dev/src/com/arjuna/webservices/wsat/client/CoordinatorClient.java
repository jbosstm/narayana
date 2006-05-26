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
package com.arjuna.webservices.wsat.client;

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
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.NotificationType;

/**
 * The Client side of the Coordinator.
 * @author kevin
 */
public class CoordinatorClient extends BaseWSAddrClient
{
    /**
     * The client singleton.
     */
    private static final CoordinatorClient CLIENT = new CoordinatorClient() ;
    
    /**
     * The prepared action.
     */
    private final AttributedURIType preparedAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_PREPARED) ;
    /**
     * The aborted action.
     */
    private final AttributedURIType abortedAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_ABORTED) ;
    /**
     * The read only action.
     */
    private final AttributedURIType readOnlyAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_READ_ONLY) ;
    /**
     * The committed action.
     */
    private final AttributedURIType committedAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_COMMITTED) ;
    /**
     * The replay action.
     */
    private final AttributedURIType replayAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_REPLAY) ;
    /**
     * The fault action.
     */
    private final AttributedURIType faultAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_FAULT) ;
    
    /**
     * The SOAP service representing the client.
     */
    private final SoapService soapService ;
    /**
     * The participant URI for replies.
     */
    private final AttributedURIType participant ;
    
    /**
     * Construct the coordinator client.
     */
    private CoordinatorClient()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        ClientPolicy.register(handlerRegistry) ;
        
        soapService = new SoapService(handlerRegistry) ;
        final String participantURI =
            SoapRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.SERVICE_PARTICIPANT) ;
        participant = new AttributedURIType(participantURI) ;
    }
    
    /**
     * Send a prepared request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendPrepared(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, AtomicTransactionConstants.WSAT_ELEMENT_PREPARED_QNAME,
            preparedAction) ;
    }
    
    /**
     * Send an aborted request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendAborted(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
            null, AtomicTransactionConstants.WSAT_ELEMENT_ABORTED_QNAME, abortedAction) ;
    }
    
    /**
     * Send a read only request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendReadOnly(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
            null, AtomicTransactionConstants.WSAT_ELEMENT_READ_ONLY_QNAME, readOnlyAction) ;
    }
    
    /**
     * Send a committed request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCommitted(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
            null, AtomicTransactionConstants.WSAT_ELEMENT_COMMITTED_QNAME, committedAction) ;
    }
    
    /**
     * Send a replay request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendReplay(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, AtomicTransactionConstants.WSAT_ELEMENT_REPLAY_QNAME,
            replayAction) ;
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
        sendSoapFault(soapFault, addressingContext, soapService, endpointReference, faultAction) ;
    }
    
    /**
     * Get the endpoint reference for the specified identifier.
     * @param identifier The endpoint reference identifier.
     * @return The endpoint reference.
     */
    private EndpointReferenceType getEndpointReference(final InstanceIdentifier identifier)
    {
        final EndpointReferenceType participantEndpoint = new EndpointReferenceType(participant) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantEndpoint, identifier) ;
        return participantEndpoint ;
    }
    
    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static CoordinatorClient getClient()
    {
        return CLIENT ;
    }
}
