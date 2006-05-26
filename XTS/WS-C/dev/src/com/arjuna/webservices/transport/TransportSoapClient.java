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
package com.arjuna.webservices.transport;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapBody;
import com.arjuna.webservices.SoapClient;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapMessage;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.soap.SoapBodyMessage;
import com.arjuna.webservices.soap.SoapDetails;
import com.arjuna.webservices.soap.SoapUtils;

/**
 * The base SOAP client for the transports.
 * @author kevin
 */
public abstract class TransportSoapClient implements SoapClient
{
    /**
     * Parse the response from the input stream.
     * @param soapService The client SOAP service.
     * @param messageContext The current message context.
     * @param messageResponseContext The response message context.
     * @param action The transport SOAP action.
     * @param reader The current reader.
     * @param soapDetails The SOAP details.
     * @return the named element.
     * @throws SoapFault for SOAP processing faults.
     * @throws XMLStreamException For errors during parsing.
     */
    protected SoapBodyMessage parseResponse(final SoapService soapService,
        final MessageContext messageContext, final MessageContext messageResponseContext,
        final String action, final Reader reader, final SoapDetails soapDetails)
        throws SoapFault, XMLStreamException
    {
        final XMLStreamReader streamReader = SoapUtils.getXMLStreamReader(reader) ;
        final SoapBody response = soapService.parse(messageContext, messageResponseContext, action, streamReader, soapDetails) ;
        return new SoapBodyMessage(response, soapDetails, soapService, messageContext) ;
    }
    
    /**
     * Serialise the SOAP request to a string.
     * @param request The SOAP request.
     * @return The string representation.
     * @throws IOException for errors creating the stream
     */
    protected String serialiseRequest(final SoapMessage request)
        throws IOException
    {
        final StringWriter writer = new StringWriter() ;
        request.output(writer) ;
        return writer.toString() ;
    }
}
