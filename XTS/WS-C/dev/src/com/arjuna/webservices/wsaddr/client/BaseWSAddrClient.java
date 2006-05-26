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
package com.arjuna.webservices.wsaddr.client;

import java.io.IOException;

import javax.xml.namespace.QName;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapBody;
import com.arjuna.webservices.SoapClient;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapMessage;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.soap.SoapBodyMessage;
import com.arjuna.webservices.soap.SoapFaultMessage;
import com.arjuna.webservices.stax.ElementContent;
import com.arjuna.webservices.stax.NamedElement;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;

/**
 * Base client.
 * @author kevin
 */
public class BaseWSAddrClient
{
    /**
     * Send a request.
     * @param request The request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param soapService The client SOAP service.
     * @param from The from endpoint reference.
     * @param replyTo The replyTo endpoint reference.
     * @param bodyName The body name for the request.
     * @param action The action URI for the request.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    protected SoapMessage send(final ElementContent request,
        final AddressingContext addressingContext, final SoapService soapService,
        final EndpointReferenceType from, final EndpointReferenceType replyTo,
        final QName bodyName, final AttributedURIType action)
        throws SoapFault, IOException
    {
        // Initialise From, ReplyTo and Action.  FaultTo should be blank.
        addressingContext.setFrom(from) ;
        addressingContext.setReplyTo(replyTo) ;
        addressingContext.setAction(action) ;
        final String actionValue = (action == null ? null : action.getValue()) ;
        
        final String toURL = addressingContext.getTo().getValue() ;
        
        final MessageContext messageContext = new MessageContext() ;
        AddressingContext.setContext(messageContext, addressingContext) ;

        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        
        final String scheme = soapRegistry.getScheme(toURL) ;
        final NamedElement contents = new NamedElement(bodyName, request) ;
        final SoapBody soapBody = new SoapBody(contents, actionValue) ;
        final SoapBodyMessage soapBodyMessage = new SoapBodyMessage(soapBody, null, soapService, messageContext) ; 
        
        final SoapClient client = soapRegistry.getSoapClient(scheme) ;
        return client.invoke(soapBodyMessage, toURL) ;
    }
    
    /**
     * Send a one way request.
     * @param request The request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param soapService The client SOAP service.
     * @param from The from endpoint reference.
     * @param replyTo The replyTo endpoint reference.
     * @param bodyName The body name for the request.
     * @param action The action URI for the request.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    protected void sendOneWay(final ElementContent request,
        final AddressingContext addressingContext, final SoapService soapService,
        final EndpointReferenceType from, final EndpointReferenceType replyTo,
        final QName bodyName, final AttributedURIType action)
        throws SoapFault, IOException
    {
        // Initialise From, ReplyTo and Action.  FaultTo should be blank.
        addressingContext.setFrom(from) ;
        addressingContext.setReplyTo(replyTo) ;
        addressingContext.setAction(action) ;
        final String actionValue = (action == null ? null : action.getValue()) ;
        
        final String toURL = addressingContext.getTo().getValue() ;
        
        final MessageContext messageContext = new MessageContext() ;
        AddressingContext.setContext(messageContext, addressingContext) ;

        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        
        final String scheme = soapRegistry.getScheme(toURL) ;
        final NamedElement contents = new NamedElement(bodyName, request) ;
        final SoapBody soapBody = new SoapBody(contents, actionValue) ;
        final SoapBodyMessage soapBodyMessage = new SoapBodyMessage(soapBody, null, soapService, messageContext) ; 
        
        final SoapClient client = soapRegistry.getSoapClient(scheme) ;
        client.invokeOneWay(soapBodyMessage, toURL) ;
    }
    
    /**
     * Send a fault.
     * @param soapFault The SOAP fault.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param soapService The client SOAP service.
     * @param from The from endpoint reference.
     * @param action The action URI for the request.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    protected void sendSoapFault(final SoapFault soapFault,
        final AddressingContext addressingContext, final SoapService soapService,
        final EndpointReferenceType from, final AttributedURIType action)
        throws SoapFault, IOException
    {
        // Initialise From and Action.  ReplyTo and FaultTo should be blank.
        addressingContext.setFrom(from) ;
        addressingContext.setAction(action) ;
        
        final String toURL = addressingContext.getTo().getValue() ;
        
        final MessageContext messageContext = new MessageContext() ;
        if (action != null)
        {
            soapFault.setAction(action.getValue()) ;
        }
        AddressingContext.setContext(messageContext, addressingContext) ;

        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        
        final String scheme = soapRegistry.getScheme(toURL) ;
        final SoapFaultMessage fault = new SoapFaultMessage(soapFault, null, soapService, messageContext) ;
        
        final SoapClient client = soapRegistry.getSoapClient(scheme) ;
        client.invokeOneWay(fault, toURL) ;
    }
}
