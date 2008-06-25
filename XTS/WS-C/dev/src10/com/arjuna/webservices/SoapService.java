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
package com.arjuna.webservices;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.arjuna.webservices.soap.Soap11Details;
import com.arjuna.webservices.soap.Soap12Details;
import com.arjuna.webservices.soap.SoapDetails;
import com.arjuna.webservices.soap.SoapParser;
import com.arjuna.webservices.soap.SoapProcessorImpl;

/**
 * Class responsible for handling SOAP services.
 * @author kevin
 */
public class SoapService
{
    /**
     * The soap 1.1 processor.
     */
    private final SoapProcessor soap11Processor = new SoapProcessorImpl(this, new Soap11Details()) ;
    /**
     * The soap 1.2 processor.
     */
    private final SoapProcessor soap12Processor = new SoapProcessorImpl(this, new Soap12Details()) ;
    
    /**
     * The registry of handlers associated with this service.
     */
    private final HandlerRegistry handlerRegistry ;
    
    /**
     * Initialise the SOAP service.
     * @param handlerRegistry The handler registry for this service.
     */
    public SoapService(final HandlerRegistry handlerRegistry)
    {
        this.handlerRegistry = handlerRegistry ;
    }

    /**
     * Get the header handler associated with the specified header name.
     * @param headerName The name of the header.
     * @return The header handler or null if not recognised.
     */
    public HeaderHandler getHeaderHandler(final QName headerName)
    {
        return handlerRegistry.getHeaderHandler(headerName) ;
    }
    
    /**
     * Get the header handlers.
     * @return The header handlers.
     */
    public Map getHeaderHandlers()
    {
        return handlerRegistry.getHeaderHandlers() ;
    }

    /**
     * Get the body handler associated with the specified body name.
     * @param bodyName The name of the body.
     * @return The body handler or null if not recognised.
     */
    public BodyHandler getBodyHandler(final QName bodyName)
    {
        return handlerRegistry.getBodyHandler(bodyName) ;
    }

    /**
     * Get the fault handler for the service.
     * @return The fault handler for the service.
     */
    public BodyHandler getFaultHandler()
    {
        return handlerRegistry.getFaultHandler() ;
    }
    
    /**
     * Get the interceptor handlers.
     * @return The interceptor handlers.
     */
    public Set getInterceptorHandlers()
    {
        return handlerRegistry.getInterceptorHandlers() ;
    }

    /**
     * Return the SOAP 1.1 processor.
     * @return The SOAP 1.1 processor.
     */
    public SoapProcessor getSOAP11Processor()
    {
        return soap11Processor ;
    }

    /**
     * Return the SOAP 1.2 processor.
     * @return The SOAP 1.2 processor.
     */
    public SoapProcessor getSOAP12Processor()
    {
        return soap12Processor ;
    }

    /**
     * Handle the processing of the SOAP request.
     * @param messageContext The current message context.
     * @param messageResponseContext The current message response context.
     * @param action The transport SOAP action.
     * @param streamReader The XML stream reader.
     * @param soapDetails The SOAP details.
     * @return The SOAP response.
     * @throws XMLStreamException For XML parsing errors.
     * @throws SoapFault For SOAP processing errors.
     */
    public SoapBody parse(final MessageContext messageContext, final MessageContext messageResponseContext,
        final String action, final XMLStreamReader streamReader, final SoapDetails soapDetails)
        throws XMLStreamException, SoapFault
    {
        return SoapParser.parse(messageContext, messageResponseContext, action, streamReader, this, soapDetails) ;
    }
}
