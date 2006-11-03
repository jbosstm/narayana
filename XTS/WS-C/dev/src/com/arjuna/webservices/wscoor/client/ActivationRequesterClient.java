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
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.webservices.wscoor.CreateCoordinationContextResponseType;

/**
 * The Client side of the Activation Requester.
 * @author kevin
 */
public class ActivationRequesterClient extends BaseWSAddrClient
{
    /**
     * The client singleton.
     */
    private static final ActivationRequesterClient CLIENT = new ActivationRequesterClient() ;
    
    /**
     * The create coordination context response action.
     */
    private final AttributedURIType createCoordinationContextResponseAction =
        new AttributedURIType(CoordinationConstants.WSCOOR_ACTION_CREATE_COORDINATION_CONTEXT_RESPONSE) ;
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
     * The activation coordinator URI for replies.
     */
    private final EndpointReferenceType activationCoordinator ;
    
    /**
     * Construct the activition requester client.
     */
    private ActivationRequesterClient()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        ClientPolicy.register(handlerRegistry) ;
        
        soapService = new SoapService(handlerRegistry) ;
        final String activationCoordinatorURI = SoapRegistry.getRegistry().getServiceURI(CoordinationConstants.SERVICE_ACTIVATION_COORDINATOR) ;
        activationCoordinator = new EndpointReferenceType(new AttributedURIType(activationCoordinatorURI)) ;
    }

    /**
     * Send a create coordination response.
     * @param addressingContext The addressing context initialised with to, message ID and relates to.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCreateCoordinationResponse(final AddressingContext addressingContext, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final CreateCoordinationContextResponseType response = new CreateCoordinationContextResponseType() ;
        response.setCoordinationContext(coordinationContext) ;
        
        sendOneWay(response, addressingContext, soapService, activationCoordinator, null,
            CoordinationConstants.WSCOOR_ELEMENT_CREATE_COORDINATION_CONTEXT_RESPONSE_QNAME,
            createCoordinationContextResponseAction) ;
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
        sendSoapFault(soapFault, addressingContext, soapService, activationCoordinator, faultAction) ;
    }

    /**
     * Get the Activation Requester client singleton.
     * @return The Activation Requester client singleton.
     */
    public static ActivationRequesterClient getClient()
    {
        return CLIENT ;
    }
}
