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
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsaddr.client.BaseWSAddrClient;
import com.arjuna.webservices.wsaddr.policy.AddressingPolicy;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.RegisterResponseType;

/**
 * The Client side of the Registration Requester.
 * @author kevin
 */
public class RegistrationRequesterClient extends BaseWSAddrClient
{
    /**
     * The client singleton.
     */
    private static final RegistrationRequesterClient CLIENT = new RegistrationRequesterClient() ;
    
    /**
     * The register response action.
     */
    private final AttributedURIType registerResponseAction =
        new AttributedURIType(CoordinationConstants.WSCOOR_ACTION_REGISTER_RESPONSE) ;
    /**
     * The fault action.
     */
    private final AttributedURIType faultAction =
        new AttributedURIType(CoordinationConstants.WSCOOR_ACTION_FAULT) ;
    
    /**
     * The SOAP service representing the client.
     */
    private final SoapService soapService ;
    /**
     * The registration coordinator URI for replies.
     */
    private final EndpointReferenceType registrationCoordinator ;
    
    /**
     * Construct the activition requester client.
     */
    private RegistrationRequesterClient()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        ClientPolicy.register(handlerRegistry) ;
        
        soapService = new SoapService(handlerRegistry) ;
        final String registrationCoordinatorURI = SoapRegistry.getRegistry().getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR) ;
        registrationCoordinator = new EndpointReferenceType(new AttributedURIType(registrationCoordinatorURI)) ;
    }

    /**
     * Send a create coordination response.
     * @param addressingContext The addressing context initialised with to, message ID and relates to.
     * @param coordinationProtocolService The coordination protocol service endpoint reference.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendRegisterResponse(final AddressingContext addressingContext, final EndpointReferenceType coordinationProtocolService)
        throws SoapFault, IOException
    {
        final RegisterResponseType response = new RegisterResponseType() ;
        response.setCoordinatorProtocolService(coordinationProtocolService) ;
        
        sendOneWay(response, addressingContext, soapService, registrationCoordinator, null,
            CoordinationConstants.WSCOOR_ELEMENT_REGISTER_RESPONSE_QNAME, registerResponseAction) ;
    }

    /**
     * Send a fault.
     * @param addressingContext The addressing context.
     * @param soapFault The SOAP fault.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendSoapFault(final AddressingContext addressingContext, final SoapFault soapFault)
        throws SoapFault, IOException
    {
        sendSoapFault(soapFault, addressingContext, soapService, registrationCoordinator, faultAction) ;
    }
    
    /**
     * Get the Activation Requester client singleton.
     * @return The Activation Requester client singleton.
     */
    public static RegistrationRequesterClient getClient()
    {
        return CLIENT ;
    }
}
