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
package com.arjuna.webservices11.wscoor.client;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wscoor.client.WSCOORClient;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.*;

import javax.xml.ws.addressing.AddressingProperties;
import java.io.IOException;

/**
 * The Client side of the Activation Coordinator.
 * @author kevin
 */
public class ActivationCoordinatorClient
{
    /**
     * The client singleton.
     */
    private static final ActivationCoordinatorClient CLIENT = new ActivationCoordinatorClient() ;

    /**
     * Construct the activation coordinator client.
     */
    private ActivationCoordinatorClient()
    {
    }
    
    /**
     * Send a create coordination request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param coordinationType The type of the coordination.
     * @param expires The expiry interval of the context.
     * @param currentContext The current coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public CreateCoordinationContextResponseType
    sendCreateCoordination(final AddressingProperties addressingProperties,
        final String coordinationType, final Expires expires, final CoordinationContext currentContext)
        throws SoapFault, IOException
    {
        final CreateCoordinationContextType request = new CreateCoordinationContextType() ;
        request.setCoordinationType(coordinationType) ;
        request.setExpires(expires) ;
        if (currentContext != null) {
            // structurally a CreateCoordinationContextType.CurrentContext and a CoordinationContext are the same i.e.
            // they are a CoordinationContextType extended with an Any list. but the schema does not use one to define
            // the other so, until we can generate them as the same type we have to interconvert here (and elsewhere)

            CreateCoordinationContextType.CurrentContext current = new CreateCoordinationContextType.CurrentContext();
            current.setCoordinationType(currentContext.getCoordinationType());
            current.setExpires(currentContext.getExpires());
            current.setIdentifier(currentContext.getIdentifier());
            current.setRegistrationService(currentContext.getRegistrationService());
            current.getAny().addAll(currentContext.getAny());
            request.setCurrentContext(current);
        } else {
            request.setCurrentContext(null) ;
        }

        // get proxy with required message id and end point address
        ActivationPortType port = WSCOORClient.getActivationPort(addressingProperties, CoordinationConstants.WSCOOR_ACTION_CREATE_COORDINATION_CONTEXT);

        // invoke remote method
        return port.createCoordinationContextOperation(request);
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
