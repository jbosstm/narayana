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
package com.arjuna.wsc;

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wscoor.AttributedUnsignedIntType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.webservices.wscoor.CreateCoordinationContextResponseType;
import com.arjuna.webservices.wscoor.client.ActivationCoordinatorClient;
import com.arjuna.webservices.wscoor.processors.ActivationRequesterCallback;
import com.arjuna.webservices.wscoor.processors.ActivationRequesterProcessor;

/**
 * Wrapper around low level Activation Coordinator messaging.
 * @author kevin
 */
public class ActivationCoordinator
{
    /**
     * Create the coordination context.
     * @param activationCoordinatorURI The URI of the activation coordinator.
     * @param messageID The messageID to use.
     * @param coordinationTypeURI The coordination type.
     * @param expires The expiry time or null.
     * @param currentContext The currnt context or null.
     * @return The coordination context.
     * @throws InvalidCreateParametersException if the create parameters are invalid.
     * @throws SoapFault for errors during processing.
     * 
     * @message com.arjuna.wsc.ActivationCoordinator_1 [com.arjuna.wsc.ActivationCoordinator_1] - Callback execution failed
     * @message com.arjuna.wsc.ActivationCoordinator_2 [com.arjuna.wsc.ActivationCoordinator_2] - Callback wasn't triggered
     */
    public static CoordinationContextType createCoordinationContext(final String activationCoordinatorURI,
        final String messageID, final String coordinationTypeURI, final Long expires,
        final CoordinationContextType currentContext)
        throws InvalidCreateParametersException, SoapFault
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(activationCoordinatorURI, messageID) ;
        
        final AttributedUnsignedIntType expiresValue = (expires == null ? null : new AttributedUnsignedIntType(expires.longValue())) ;
        final RequestCallback callback = new RequestCallback() ;
        final ActivationRequesterProcessor activationRequester = ActivationRequesterProcessor.getRequester() ;
        activationRequester.registerCallback(messageID, callback) ;
        try
        {
            ActivationCoordinatorClient.getClient().sendCreateCoordination(addressingContext,
                    coordinationTypeURI, expiresValue, currentContext) ;
            callback.waitUntilTriggered() ;
        }
        catch (final IOException ioe)
        {
            throw new SoapFault10(ioe) ;
        }
        finally
        {
            activationRequester.removeCallback(messageID) ;
        }
        
        if (callback.hasFailed())
        {
            throw new SoapFault10(SoapFaultType.FAULT_RECEIVER, null, WSCLogger.log_mesg.getString("com.arjuna.wsc.ActivationCoordinator_1")) ;
        }
        else if (!callback.hasTriggered())
        {
            throw new SoapFault10(SoapFaultType.FAULT_RECEIVER, null, WSCLogger.log_mesg.getString("com.arjuna.wsc.ActivationCoordinator_2")) ;
        }
        
        final CreateCoordinationContextResponseType response = callback.getCreateCoordinationContextResponse() ;
        if (response != null)
        {
            return response.getCoordinationContext() ;
        }
        final SoapFault soapFault = callback.getSoapFault() ;
        if (CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME.equals(soapFault.getSubcode()))
        {
            throw new InvalidCreateParametersException(soapFault.getReason()) ;
        }
        throw soapFault ;
    }
    
    /**
     * The request callback.
     * @author kevin
     */
    private static final class RequestCallback extends ActivationRequesterCallback
    {
        /**
         * The response.
         */
        private CreateCoordinationContextResponseType createCoordinationContextResponse ;
        /**
         * The SOAP fault.
         */
        private SoapFault soapFault ;
        
        /**
         * A create coordination context response.
         * @param createCoordinationContextResponse The response.
         * @param addressingContext The current addressing context.
         */
        public void createCoordinationContextResponse(final CreateCoordinationContextResponseType createCoordinationContextResponse,
            final AddressingContext addressingContext)
        {
            this.createCoordinationContextResponse = createCoordinationContextResponse ;
        }

        /**
         * A SOAP fault response.
         * @param soapFault The SOAP fault response.
         * @param addressingContext The current addressing context.
         */
        public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext)
        {
            this.soapFault = soapFault ;
        }
        
        /**
         * Get the create coordination context response.
         * @return The create coordination context response.
         */
        CreateCoordinationContextResponseType getCreateCoordinationContextResponse()
        {
            return createCoordinationContextResponse ;
        }
        
        /**
         * Get the SOAP fault.
         * @return The SOAP fault or null.
         */
        SoapFault getSoapFault()
        {
            return soapFault ;
        }
    }
}
