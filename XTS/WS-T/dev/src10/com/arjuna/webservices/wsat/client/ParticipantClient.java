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
 * The Client side of the Participant.
 * @author kevin
 */
public class ParticipantClient extends BaseWSAddrClient
{
    /**
     * The client singleton.
     */
    private static final ParticipantClient CLIENT = new ParticipantClient() ;
    
    /**
     * The prepare action.
     */
    private final AttributedURIType prepareAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_PREPARE) ;
    /**
     * The commit action.
     */
    private final AttributedURIType commitAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_COMMIT) ;
    /**
     * The rollback action.
     */
    private final AttributedURIType rollbackAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_ROLLBACK) ;
    /**
     * The SOAP fault action.
     */
    private final AttributedURIType faultAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_FAULT) ;
    
    /**
     * The SOAP service representing the client.
     */
    private final SoapService soapService ;
    /**
     * The coordinator URI for replies.
     */
    private final AttributedURIType coordinator ;
    
    /**
     * Construct the completion initiator client.
     */
    private ParticipantClient()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        ClientPolicy.register(handlerRegistry) ;
        
        soapService = new SoapService(handlerRegistry) ;
        final String coordinatorURI =
            SoapRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.SERVICE_COORDINATOR) ;
        coordinator = new AttributedURIType(coordinatorURI) ;
    }
    
    /**
     * Send a prepare request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendPrepare(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, AtomicTransactionConstants.WSAT_ELEMENT_PREPARE_QNAME,
            prepareAction) ;
    }
    
    /**
     * Send a commit request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCommit(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, AtomicTransactionConstants.WSAT_ELEMENT_COMMIT_QNAME,
            commitAction) ;
    }
    
    /**
     * Send a rollback request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendRollback(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, AtomicTransactionConstants.WSAT_ELEMENT_ROLLBACK_QNAME,
            rollbackAction) ;
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
        final EndpointReferenceType coordinatorEndpoint = new EndpointReferenceType(coordinator) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorEndpoint, identifier) ;
        return coordinatorEndpoint ;
    }
    
    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static ParticipantClient getClient()
    {
        return CLIENT ;
    }
}
