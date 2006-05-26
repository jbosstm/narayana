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
package com.arjuna.webservices.soap;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.HeaderHandler;
import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapMessage;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.util.StreamHelper;

abstract class SoapMessageBase implements SoapMessage
{
    /**
     * The SOAP details for this service.
     */
    private final SoapDetails soapDetails ;
    /**
     * The SOAP service.
     */
    private final SoapService soapService ;
    /**
     * The message context.
     */
    private final MessageContext messageContext ;

    /**
     * Construct the SOAP message base.
     * @param soapDetails The SOAP details.
     * @param soapService The SOAP service.
     * @param messageContext The message context.
     */
    protected SoapMessageBase(final SoapDetails soapDetails, final SoapService soapService,
        final MessageContext messageContext)
    {
        this.soapDetails = (soapDetails == null ? new Soap11Details() : soapDetails) ;
        this.soapService = soapService ;
        this.messageContext = messageContext ;
    }
    
    /**
     * Get the action URI for the message.
     * @return The action URI for the message.
     */
    public abstract String getAction() ;
    
    /**
     * Output the message to the output stream.
     * @param writer The output writer.
     * @throws IOException If errors occur during output.
     */
    public void output(final Writer writer)
        throws IOException
    {
        try
        {
            writeEnvelope(SoapUtils.getXMLStreamWriter(writer)) ;
        }
        catch (final XMLStreamException xmlse)
        {
            throw new IOException(xmlse.toString()) ;
        }
    }
    
    /**
     * Output the envelope to the stream writer.
     * @param streamWriter The stream writer.
     * @throws XMLStreamException If errors occur during output.
     */
    private void writeEnvelope(final XMLStreamWriter streamWriter)
        throws XMLStreamException
    {
        final QName envelopeName = getQualifiedName(SoapConstants.SOAP_ENVELOPE_NAME) ;
        final String envNamespace = StreamHelper.writeStartElement(streamWriter, envelopeName) ;
        
        writeHeaders(streamWriter) ;
        writeBody(streamWriter) ;
        
        StreamHelper.writeEndElement(streamWriter, envelopeName.getPrefix(), envNamespace) ;
    }
    
    /**
     * Output the headers to the stream writer.
     * @param streamWriter The stream writer.
     * @throws XMLStreamException If errors occur during output.
     */
    private void writeHeaders(final XMLStreamWriter streamWriter)
        throws XMLStreamException
    {
        final QName headerName = getQualifiedName(SoapConstants.SOAP_HEADER_NAME) ;
        final String headerNamespace = StreamHelper.writeStartElement(streamWriter, headerName) ;
        
        writeMessageHeaders(streamWriter) ;
        
        if (soapService != null)
        {
            final Map headerHandlers = soapService.getHeaderHandlers() ;
            final Iterator headerHandlerEntryIterator = headerHandlers.entrySet().iterator() ;
            while(headerHandlerEntryIterator.hasNext())
            {
                final Map.Entry entry = (Map.Entry)headerHandlerEntryIterator.next() ;
                final QName headerHandlerName = (QName)entry.getKey() ;
                final HeaderHandler headerHandler = (HeaderHandler)entry.getValue() ;
                headerHandler.writeContent(streamWriter, headerHandlerName, messageContext, soapDetails) ;
            }
        }
        
        StreamHelper.writeEndElement(streamWriter, headerName.getPrefix(), headerNamespace) ;
    }
    
    /**
     * Output the body to the stream writer.
     * @param streamWriter The stream writer.
     * @throws XMLStreamException If errors occur during output.
     */
    private void writeBody(final XMLStreamWriter streamWriter)
        throws XMLStreamException
    {
        final QName bodyName = getQualifiedName(SoapConstants.SOAP_BODY_NAME) ;
        final String bodyNamespace = StreamHelper.writeStartElement(streamWriter, bodyName) ;
        
        writeMessageBody(streamWriter) ;
        
        StreamHelper.writeEndElement(streamWriter, bodyName.getPrefix(), bodyNamespace) ;
    }
    
    /**
     * Get the SOAP qualified name.
     * @param localName The local name.
     * @return The SOAP qualified name.
     */
    protected QName getQualifiedName(final String localName)
    {
        return new QName(soapDetails.getNamespaceURI(), localName,
            SoapConstants.SOAP_PREFIX) ;
    }
    
    /**
     * Get the SOAP details associated with this message.
     * @return The SOAP details.
     */
    public SoapDetails getSoapDetails()
    {
        return soapDetails ;
    }
    
    /**
     * Get the SOAP service associated with this message.
     * @return The SOAP service or null if not known.
     */
    public SoapService getSoapService()
    {
        return soapService ;
    }
    
    /**
     * Get the message context associated with this message.
     * @return The message context or null if not known.
     */
    public MessageContext getMessageContext()
    {
        return messageContext ;
    }
    
    /**
     * Write the headers specific to the message.
     * @param streamWriter The stream writer.
     * @throws XMLStreamException For errors during writing.
     */
    protected abstract void writeMessageHeaders(final XMLStreamWriter streamWriter)
        throws XMLStreamException ;
    
    /**
     * Write the body specific to the message.
     * @param streamWriter The stream writer.
     * @throws XMLStreamException For errors during writing.
     */
    protected abstract void writeMessageBody(final XMLStreamWriter streamWriter)
        throws XMLStreamException ;
}
