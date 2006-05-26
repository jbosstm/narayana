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
package com.arjuna.webservices.wsaddr2005.handlers;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsaddr2005.AddressingContext;
import com.arjuna.webservices.wsaddr2005.AttributedURIType;

/**
 * Header handler for parsing the WS-Addressing To header.
 * @author kevin
 */
public class ToHandler extends AddressingHeaderHandler
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
        final QName headerName = in.getName() ;
        final AttributedURIType to = new AttributedURIType(in) ;
        final AddressingContext addressingContext = AddressingContext.getContext(context) ;
        if (addressingContext.getTo() != null)
        {
            addressingContext.setFaultHeaderName(headerName) ;
            addressingContext.setFaultHeader(to) ;
        }
        else
        {
            addressingContext.setTo(to) ;
        }
    }
}
