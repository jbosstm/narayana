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
package com.arjuna.webservices.wsarj.handler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.HeaderHandler;
import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.soap.SoapDetails;
import com.arjuna.webservices.util.StreamHelper;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;

/**
 * Header handler for parsing the Arjuna WS instance identifier header.
 * @author kevin
 */
public class InstanceIdentifierHandler implements HeaderHandler
{
    /**
     * Handle the header element.
     * @param in The current streamreader.
     * @param context The current message context.
     * @throws XMLStreamException for parsing errors.
     * @throws SoapFault for processing errors.
     */
    public void invoke(final XMLStreamReader in, final MessageContext context)
        throws XMLStreamException, SoapFault
    {
        final InstanceIdentifier action = new InstanceIdentifier(in) ;
        ArjunaContext.getContext(context).setInstanceIdentifier(action) ;
    }
    
    /**
     * Write the header element in a response.
     * @param out The output stream.
     * @param headerElementName The name of the header element.
     * @param context The current message context.
     * @param soapDetails The SOAP details.
     * @throws XMLStreamException 
     */
    public void writeContent(final XMLStreamWriter out, final QName headerElementName,
        final MessageContext context, final SoapDetails soapDetails)
        throws XMLStreamException
    {
        final ArjunaContext arjunaContext = ArjunaContext.getCurrentContext(context) ;
        if (arjunaContext != null)
        {
            final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
            if (instanceIdentifier != null)
            {
                StreamHelper.writeElement(out, headerElementName, instanceIdentifier) ;
            }
        }
    }
    
    /**
     * Validate headers after processing.
     * @param context The current message context.
     * @throws SoapFault for validation errors.
     */
    public void headerValidate(final MessageContext context)
        throws SoapFault
    {
    }
    
    /**
     * Notification of a subsequent header processing fault.
     * @param context The current message context.
     */
    public void headerFaultNotification(final MessageContext context)
    {
    }
}
