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

import javax.xml.namespace.QName;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.webservices.wscoor.RegisterResponseType;
import com.arjuna.webservices.wscoor.client.RegistrationCoordinatorClient;
import com.arjuna.webservices.wscoor.processors.RegistrationRequesterCallback;
import com.arjuna.webservices.wscoor.processors.RegistrationRequesterProcessor;


/**
 * Wrapper around low level Registration Coordinator messaging.
 * @author kevin
 */
public class RegistrationCoordinator
{
    /**
     * Register the participant in the protocol.
     * @param coordinationContext The current coordination context
     * @param messageID The messageID to use.
     * @param participantProtocolService The participant protocol service.
     * @param protocolIdentifier The protocol identifier.
     * @return The endpoint reference of the coordinator protocol service.
     * @throws AlreadyRegisteredException If the participant is already registered. 
     * @throws InvalidProtocolException If the protocol is unsupported.
     * @throws InvalidStateException If the state is invalid
     * @throws NoActivityException If there is to activity context active.
     * @throws SoapFault for errors during processing.
     * 
     * @message com.arjuna.wsc.RegistrationCoordinator_1 [com.arjuna.wsc.RegistrationCoordinator_1] - Callback execution failed
     * @message com.arjuna.wsc.RegistrationCoordinator_2 [com.arjuna.wsc.RegistrationCoordinator_2] - Callback wasn't triggered
     */
    public static EndpointReferenceType register(final CoordinationContextType coordinationContext,
        final String messageID, final EndpointReferenceType participantProtocolService,
        final String protocolIdentifier)
        throws AlreadyRegisteredException, InvalidProtocolException,
            InvalidStateException, NoActivityException, SoapFault
    {
        final EndpointReferenceType endpointReference = coordinationContext.getRegistrationService() ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(endpointReference, messageID) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final RegistrationRequesterProcessor registrationRequester = RegistrationRequesterProcessor.getRequester() ;
        registrationRequester.registerCallback(messageID, callback) ;
        try
        {
            RegistrationCoordinatorClient.getClient().sendRegister(addressingContext, protocolIdentifier, participantProtocolService) ;
            callback.waitUntilTriggered() ;
        }
        catch (final IOException ioe)
        {
            throw new SoapFault10(ioe) ;
        }
        finally
        {
            registrationRequester.removeCallback(messageID) ;
        }
        
        if (callback.hasFailed())
        {
            throw new SoapFault10(SoapFaultType.FAULT_RECEIVER, null, WSCLogger.arjLoggerI18N.getString("com.arjuna.wsc.RegistrationCoordinator_1")) ;
        }
        else if (!callback.hasTriggered())
        {
            throw new SoapFault10(SoapFaultType.FAULT_RECEIVER, null, WSCLogger.arjLoggerI18N.getString("com.arjuna.wsc.RegistrationCoordinator_2")) ;
        }
        
        final RegisterResponseType response = callback.getRegisterResponse() ;
        if (response != null)
        {
            return response.getCoordinatorProtocolService() ;
        }
        final SoapFault soapFault = callback.getSoapFault() ;
        final QName subcode = soapFault.getSubcode() ;
        if (CoordinationConstants.WSCOOR_ERROR_CODE_ALREADY_REGISTERED_QNAME.equals(subcode))
        {
            throw new AlreadyRegisteredException(soapFault.getReason()) ;
        }
        else if (CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PROTOCOL_QNAME.equals(subcode))
        {
            throw new InvalidProtocolException(soapFault.getReason()) ;
        }
        else if (CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME.equals(subcode))
        {
            throw new InvalidStateException(soapFault.getReason()) ;
        }
        else if (CoordinationConstants.WSCOOR_ERROR_CODE_NO_ACTIVITY_QNAME.equals(subcode))
        {
            throw new NoActivityException(soapFault.getReason()) ;
        }
        throw soapFault ;
    }
    
    /**
     * The request callback.
     * @author kevin
     */
    private static final class RequestCallback extends RegistrationRequesterCallback
    {
        /**
         * The response.
         */
        private RegisterResponseType registerResponse ;
        /**
         * The SOAP fault.
         */
        private SoapFault soapFault ;
        
        /**
         * A register response.
         * @param registerResponse The response.
         * @param addressingContext The current addressing context.
         */
        public void registerResponse(final RegisterResponseType registerResponse,
            final AddressingContext addressingContext)
        {
            this.registerResponse = registerResponse ;
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
        RegisterResponseType getRegisterResponse()
        {
            return registerResponse ;
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
