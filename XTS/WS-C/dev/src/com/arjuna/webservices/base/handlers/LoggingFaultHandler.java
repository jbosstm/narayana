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
package com.arjuna.webservices.base.handlers;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.arjuna.webservices.BodyHandler;
import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapBody;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.soap.SoapDetails;

/**
 * Logging fault handler.
 * @author kevin
 */
public class LoggingFaultHandler implements BodyHandler
{
    private final String serviceName ;
    
    /**
     * Create the logging fault handler.
     * @param serviceName The name of the service.
     */
    public LoggingFaultHandler(final String serviceName)
    {
        this.serviceName = serviceName ;
    }
    
    /**
     * Handle the body element.
     * @param soapDetails The SOAP details.
     * @param context The current message context.
     * @param responseContext The response message context.
     * @param action The transport SOAP action.
     * @param in The current stream reader.
     * @throws XMLStreamException for parsing errors.
     * @throws SoapFault for processing errors.
     * @return The response elements or null if one way.
     * @message com.arjuna.webservices.base.handlers.LoggingFaultHandler_1 [com.arjuna.webservices.base.handlers.LoggingFaultHandler_1] - Service {0} received unexpected fault: {1}
     */
    public SoapBody invoke(final SoapDetails soapDetails, final MessageContext context,
        final MessageContext responseContext, final String action, final XMLStreamReader in)
        throws XMLStreamException, SoapFault
    {
        final SoapFault soapFault = soapDetails.parseSoapFault(in) ;
        final String soapFaultType = soapFault.getSoapFaultType().toString() ;
        final QName subcode = soapFault.getSubcode() ;
        final String faultDetails ;
        if (subcode == null)
        {
            faultDetails = soapFaultType ;
        }
        else
        {
            faultDetails = soapFaultType + ":" + subcode ;
        }
        
        if (WSCLogger.arjLoggerI18N.isDebugEnabled())
        {
            WSCLogger.arjLoggerI18N.warn("com.arjuna.webservices.base.handlers.LoggingFaultHandler_1",
                new Object[] {serviceName, faultDetails}) ;
        }
        return null ;
    }
}
