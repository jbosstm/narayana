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
package com.arjuna.webservices.wsaddr2005.client;

import java.io.IOException;
import java.text.MessageFormat;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapBody;
import com.arjuna.webservices.SoapClient;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapMessage;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.soap.SoapBodyMessage;
import com.arjuna.webservices.soap.SoapDetails;
import com.arjuna.webservices.soap.SoapFaultMessage;
import com.arjuna.webservices.wsaddr2005.AddressingConstants;
import com.arjuna.webservices.wsaddr2005.AddressingContext;
import com.arjuna.webservices.wsaddr2005.AttributedURIType;
import com.arjuna.webservices.wsaddr2005.EndpointReferenceType;

/**
 * Base WS-Addressing 2005 client.
 * @author kevin
 * 
 * @message com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_1 [com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_1] - Addressing context does not specify destination.
 * @message com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_2 [com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_2] - Invalid destination specified in addressing context.
 * @message com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_3 [com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_3] - No SOAP client registered for scheme: {0}.
 * @message com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_4 [com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_4] - Invalid replyTo specified in addressing context.
 * @message com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_5 [com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_5] - Unexpected SOAP message type returned.
 */
public class WSAddr2005Client
{
    /**
     * Send a request.
     * @param soapBody The body element to send.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param soapDetails The SOAP details.
     * @param soapService The client SOAP service.
     * @return The returned SOAP message or null if a one way request.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public static SoapBody send(final SoapBody soapBody,
        final AddressingContext addressingContext, final SoapDetails soapDetails,
        final SoapService soapService)
        throws SoapFault, IOException
    {
        final String toURI = getDestinationURI(addressingContext) ;
        final MessageContext messageContext = createMessageContext(addressingContext) ;
        final SoapClient client = getSoapClient(toURI) ;
        
        final SoapBodyMessage soapBodyMessage = new SoapBodyMessage(soapBody, soapDetails, soapService, messageContext) ; 
        
        final EndpointReferenceType replyTo = addressingContext.getReplyTo() ;
        if (replyTo != null)
        {
            if (!replyTo.isValid())
            {
                throw new IOException(WSCLogger.log_mesg.getString("com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_4")) ;
            }
            final AttributedURIType address = replyTo.getAddress() ;
            if (!AddressingConstants.WSA_ADDRESS_ANONYMOUS.equals(address.getValue()))
            {
                client.invokeOneWay(soapBodyMessage, toURI) ;
                return null ;
            }
        }
        
        final SoapMessage response = client.invoke(soapBodyMessage, toURI) ;
        if (response instanceof SoapBodyMessage)
        {
            return ((SoapBodyMessage)response).getSoapBody() ;
        }
        throw new IOException(WSCLogger.log_mesg.getString("com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_5")) ;
    }
    
    /**
     * Send a one way request.
     * @param soapBody The body element to send.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param soapDetails The SOAP details.
     * @param soapService The client SOAP service.
     * @param replyTo The replyTo endpoint reference.
     * @param action The action URI for the request.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public static void sendOneWay(final SoapBody soapBody,
        final AddressingContext addressingContext, final SoapDetails soapDetails,
        final SoapService soapService)
        throws SoapFault, IOException
    {
        final String toURI = getDestinationURI(addressingContext) ;
        final MessageContext messageContext = createMessageContext(addressingContext) ;
        final SoapClient client = getSoapClient(toURI) ;
        
        final SoapBodyMessage soapBodyMessage = new SoapBodyMessage(soapBody, soapDetails, soapService, messageContext) ; 
        
        client.invokeOneWay(soapBodyMessage, toURI) ;
    }
    
    /**
     * Send a fault.
     * @param soapFault The SOAP fault.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param soapDetails The SOAP details.
     * @param soapService The client SOAP service.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public static void sendSoapFault(final SoapFault soapFault,
        final AddressingContext addressingContext, final SoapDetails soapDetails,
        final SoapService soapService)
        throws SoapFault, IOException
    {
        final String toURI = getDestinationURI(addressingContext) ;
        final MessageContext messageContext = createMessageContext(addressingContext) ;
        final SoapClient client = getSoapClient(toURI) ;
        
        final SoapFaultMessage fault = new SoapFaultMessage(soapFault, soapDetails, soapService, messageContext) ;
        client.invokeOneWay(fault, toURI) ;
    }
    
    /**
     * Get the destination URI from the addressing context.
     * @param addressingContext The current addressing context.
     * @return The destination URI.
     * @throws IOException For invalid destination.
     */
    private static String getDestinationURI(final AddressingContext addressingContext)
        throws IOException
    {
        final AttributedURIType to = addressingContext.getTo() ;
        if (to == null)
        {
            throw new IOException(WSCLogger.log_mesg.getString("com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_1")) ;
        }
        if (!to.isValid())
        {
            throw new IOException(WSCLogger.log_mesg.getString("com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_2")) ;
        }
        return to.getValue() ;
    }
    
    /**
     * Create a message context using the specified addressing context.
     * @param addressingContext The current addressing context.
     * @return The message context.
     */
    private static MessageContext createMessageContext(final AddressingContext addressingContext)
    {
        final MessageContext messageContext = new MessageContext() ;
        AddressingContext.setContext(messageContext, addressingContext) ;
        return messageContext ;
    }
    
    /**
     * Get the SOAP client corresponding to the specified destination URI.
     * @param destinationURI The destination URI.
     * @return The SOAP client.
     * @throws IOException for unsupported schemes.
     */
    private static SoapClient getSoapClient(final String destinationURI)
        throws IOException
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String scheme = soapRegistry.getScheme(destinationURI) ;
        final SoapClient client = soapRegistry.getSoapClient(scheme) ;
        
        if (client == null)
        {
            final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client_3") ;
            final String message = MessageFormat.format(pattern, new Object[] {scheme}) ;
            throw new IOException(message) ;
        }
        
        return client ;
    }
}
