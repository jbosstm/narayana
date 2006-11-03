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
package com.arjuna.webservices.soap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapBody;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.stax.NamedElement;
import com.arjuna.webservices.util.StreamHelper;

/**
 * SOAP message representing a normal message.
 * @author kevin
 */
public class SoapBodyMessage extends SoapMessageBase
{
    /**
     * The SOAP body.
     */
    private final SoapBody soapBody ;
    
    /**
     * Construct the SOAP body message.
     * @param soapBody The SOAP body.
     * @param soapService The SOAP service.
     * @param messageContext The message context.
     */
    public SoapBodyMessage(final SoapBody soapBody, final SoapService soapService,
        final MessageContext messageContext)
    {
        this(soapBody, null, soapService, messageContext) ;
    }
    
    /**
     * Construct the SOAP body message.
     * @param soapBody The SOAP body.
     * @param soapDetails The SOAP service details.
     * @param soapService The SOAP service.
     * @param messageContext The message context.
     */
    public SoapBodyMessage(final SoapBody soapBody, final SoapDetails soapDetails,
       final SoapService soapService, final MessageContext messageContext)
    {
        super(soapDetails, soapService, messageContext) ;
        this.soapBody = soapBody ;
    }

    /**
     * Does the message represent a fault?
     * @return true if a fault, false otherwise.
     */
    public boolean isFault()
    {
        return false ;
    }
    
    /**
     * Get the SOAP body.
     * @return The SOAP body.
     */
    public SoapBody getSoapBody()
    {
        return soapBody ;
    }
    
    /**
     * Get the action URI for the message.
     * @return The action URI for the message.
     */
    public String getAction()
    {
        return soapBody.getAction() ;
    }
    
    /**
     * Write the headers specific to the message.
     * @param streamWriter The stream writer.
     * @throws XMLStreamException For errors during writing.
     */
    protected void writeMessageHeaders(final XMLStreamWriter streamWriter)
        throws XMLStreamException
    {
        // No headers to write.
    }
    
    /**
     * Write the body specific to the message.
     * @param streamWriter The stream writer.
     * @throws XMLStreamException For errors during writing.
     */
    protected void writeMessageBody(final XMLStreamWriter streamWriter)
        throws XMLStreamException
    {
        if (soapBody != null)
        {
            final NamedElement bodyElement = soapBody.getContents() ;
            if (bodyElement != null)
            {
                final QName bodyElementName = bodyElement.getName() ;
                final String namespaceURI = StreamHelper.writeStartElement(streamWriter, bodyElementName) ;
                bodyElement.getElementContent().writeContent(streamWriter) ;
                StreamHelper.writeEndElement(streamWriter, bodyElementName.getPrefix(), namespaceURI) ;
            }
        }
    }
}
