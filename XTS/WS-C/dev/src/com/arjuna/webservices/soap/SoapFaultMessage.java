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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapService;

/**
 * SOAP message representing a fault.
 * @author kevin
 */
public class SoapFaultMessage extends SoapMessageBase
{
    /**
     * The message SOAP fault.
     */
    private final SoapFault soapFault ;
    
    /**
     * Construct a SOAP fault message.
     * @param soapFault The SOAP fault.
     * @param soapDetails The SOAP service details.
     */
    public SoapFaultMessage(final SoapFault soapFault, final SoapDetails soapDetails)
    {
        this(soapFault, soapDetails, null, null) ;
    }
    
    /**
     * Construct a SOAP service fault message.
     * @param soapFault The SOAP fault.
     * @param soapDetails The SOAP service details.
     * @param soapService The SOAP service.
     * @param messageContext The message context.
     */
    public SoapFaultMessage(final SoapFault soapFault, final SoapDetails soapDetails,
        final SoapService soapService, final MessageContext messageContext)
    {
        super(soapDetails, soapService, messageContext) ;
        this.soapFault = soapFault ;
    }
    
    /**
     * Does the message represent a fault?
     * @return true if a fault, false otherwise.
     */
    public boolean isFault()
    {
        return true ;
    }
    
    /**
     * Get the SOAP fault.
     * @return The SOAP fault.
     */
    public SoapFault getSoapFault()
    {
        return soapFault ;
    }
    
    /**
     * Get the action URI for the message.
     * @return The action URI for the message.
     */
    public String getAction()
    {
        return soapFault.getAction() ;
    }
    
    /**
     * Write the headers specific to the message.
     * @param streamWriter The stream writer.
     * @throws XMLStreamException For errors during writing.
     */
    protected void writeMessageHeaders(final XMLStreamWriter streamWriter)
        throws XMLStreamException
    {
        if (getSoapService() != null)
        {
            getSoapDetails().writeSoapFaultHeaders(streamWriter, soapFault) ;
        }
        else
        {
            getSoapDetails().writeHeaderSoapFaultHeaders(streamWriter, soapFault) ;
        }
    }
    
    /**
     * Write the body specific to the message.
     * @param streamWriter The stream writer.
     * @throws XMLStreamException For errors during writing.
     */
    protected void writeMessageBody(final XMLStreamWriter streamWriter)
        throws XMLStreamException
    {
        if (getSoapService() != null)
        {
            getSoapDetails().writeSoapFault(streamWriter, soapFault) ;
        }
        else
        {
            getSoapDetails().writeHeaderSoapFault(streamWriter, soapFault) ;
        }
    }
}
