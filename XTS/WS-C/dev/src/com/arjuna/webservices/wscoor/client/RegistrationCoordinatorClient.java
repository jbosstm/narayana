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
package com.arjuna.webservices.wscoor.client;

import java.io.IOException;

import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.base.policy.ClientPolicy;
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsaddr.client.BaseWSAddrClient;
import com.arjuna.webservices.wsaddr.policy.AddressingPolicy;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.RegisterType;

/**
 * The Client side of the Registration Coordinator.
 * @author kevin
 */
public class RegistrationCoordinatorClient extends BaseWSAddrClient
{
    /**
     * The client singleton.
     */
    private static final RegistrationCoordinatorClient CLIENT = new RegistrationCoordinatorClient() ;
    
    /**
     * The register action.
     */
    private final AttributedURIType registerAction =
        new AttributedURIType(CoordinationConstants.WSCOOR_ACTION_REGISTER) ;
    
    /**
     * The SOAP service representing the client.
     */
    private final SoapService soapService ;
    /**
     * The registartion requester URI for replies.
     */
    private final EndpointReferenceType registrationRequester ;
    
    /**
     * Construct the registration coordinator client.
     */
    private RegistrationCoordinatorClient()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        ClientPolicy.register(handlerRegistry) ;
        
        soapService = new SoapService(handlerRegistry) ;
        final String registrationRequesterURI = SoapRegistry.getRegistry().getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_REQUESTER) ;
        registrationRequester = new EndpointReferenceType(new AttributedURIType(registrationRequesterURI)) ;
    }
    
    /**
     * Send a register request.
     * @param addressingContext addressing context initialised with to, message ID and endpoint context.
     * @param protocolIdentifier The protocol identifier.
     * @param participantProtocolService The participant protocol service endpoint reference.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendRegister(final AddressingContext addressingContext, final String protocolIdentifier,
        final EndpointReferenceType participantProtocolService)
        throws SoapFault, IOException
    {
        final RegisterType request = new RegisterType() ;
        request.setProtocolIdentifier(new URI(protocolIdentifier)) ;
        request.setParticipantProtocolService(participantProtocolService) ;
        
        sendOneWay(request, addressingContext, soapService, registrationRequester, registrationRequester,
            CoordinationConstants.WSCOOR_ELEMENT_REGISTER_QNAME, registerAction) ;
    }
    
    /**
     * Get the Registration Coordinator client singleton.
     * @return The Registration Coordinator client singleton.
     */
    public static RegistrationCoordinatorClient getClient()
    {
        return CLIENT ;
    }
}
