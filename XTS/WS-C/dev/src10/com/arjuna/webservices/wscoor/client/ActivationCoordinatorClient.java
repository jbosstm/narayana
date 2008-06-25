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
import com.arjuna.webservices.wscoor.AttributedUnsignedIntType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.webservices.wscoor.CreateCoordinationContextType;

/**
 * The Client side of the Activation Coordinator.
 * @author kevin
 */
public class ActivationCoordinatorClient extends BaseWSAddrClient
{
    /**
     * The client singleton.
     */
    private static final ActivationCoordinatorClient CLIENT = new ActivationCoordinatorClient() ;
    
    /**
     * The create coordination context action.
     */
    private final AttributedURIType createCoordinationContextAction =
        new AttributedURIType(CoordinationConstants.WSCOOR_ACTION_CREATE_COORDINATION_CONTEXT) ;
    
    /**
     * The SOAP service representing the client.
     */
    private final SoapService soapService ;
    /**
     * The activation requester URI for replies.
     */
    private final EndpointReferenceType activationRequester ;
    
    /**
     * Construct the activation coordinator client.
     */
    private ActivationCoordinatorClient()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        ClientPolicy.register(handlerRegistry) ;
        
        soapService = new SoapService(handlerRegistry) ;
        final String activationRequesterURI = SoapRegistry.getRegistry().getServiceURI(CoordinationConstants.SERVICE_ACTIVATION_REQUESTER) ;
        activationRequester = new EndpointReferenceType(new AttributedURIType(activationRequesterURI)) ;
    }
    
    /**
     * Send a create coordination request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param coordinationType The type of the coordination.
     * @param expires The expiry interval of the context.
     * @param currentContext The current coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCreateCoordination(final AddressingContext addressingContext, final String coordinationType,
        final AttributedUnsignedIntType expires, final CoordinationContextType currentContext)
        throws SoapFault, IOException
    {
        final CreateCoordinationContextType request = new CreateCoordinationContextType() ;
        request.setCoordinationType(new URI(coordinationType)) ;
        request.setCurrentContext(currentContext) ;
        request.setExpires(expires) ;
        
        sendOneWay(request, addressingContext, soapService, activationRequester, activationRequester,
            CoordinationConstants.WSCOOR_ELEMENT_CREATE_COORDINATION_CONTEXT_QNAME,
            createCoordinationContextAction) ;
    }
    
    /**
     * Get the Activation Coordinator client singleton.
     * @return The Activation Coordinator client singleton.
     */
    public static ActivationCoordinatorClient getClient()
    {
        return CLIENT ;
    }
}
