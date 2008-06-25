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
package com.arjuna.webservices.soap;

import java.io.IOException;
import java.io.Reader;

import javax.xml.stream.XMLStreamReader;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapBody;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.SoapMessage;
import com.arjuna.webservices.SoapProcessor;
import com.arjuna.webservices.SoapService;

/**
 * The SOAP processor implementation.
 * @author kevin
 */
public class SoapProcessorImpl implements SoapProcessor
{
    /**
     * The SOAP service associated with this processor.
     */
    private final SoapService soapService ;
    /**
     * The SOAP details associated with this processor.
     */
    private final SoapDetails soapDetails ;
    
    /**
     * Create the SOAP processor for the specific SOAP service and details.
     * @param soapService The SOAP service.
     * @param soapDetails The SOAP details.
     */
    public SoapProcessorImpl(final SoapService soapService, final SoapDetails soapDetails)
    {
        this.soapService = soapService ;
        this.soapDetails = soapDetails ;
    }
    
    /**
     * Process the input stream and generate a response.
     * @param messageContext The message context for the request.
     * @param messageResponseContext The message context for the response.
     * @param action The transport SOAP action.
     * @param reader The input reader.
     * @return The SOAP response.
     * @throws IOException For errors reading the input stream.
     */
    public SoapMessage process(final MessageContext messageContext, final MessageContext messageResponseContext, final String action, final Reader reader)
    {
        try
        {
            final XMLStreamReader streamReader = SoapUtils.getXMLStreamReader(reader) ;
            final SoapBody response = soapService.parse(messageContext, messageResponseContext, action, streamReader, soapDetails) ;
            if (response == null)
            {
                return null ;
            }
            
            return new SoapBodyMessage(response, soapDetails, soapService, messageResponseContext) ;
        }
        catch (final SoapFault sf)
        {
            if (sf.isHeaderFault())
            {
                return new SoapFaultMessage(sf, soapDetails, null, messageContext) ;
            }
            else
            {
                return new SoapFaultMessage(sf, soapDetails, soapService, messageResponseContext) ;
            }
        }
        catch (final Throwable th)
        {
            return new SoapFaultMessage(new SoapFault10(th), soapDetails) ;
        }
    }
}
