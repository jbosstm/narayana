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
package com.arjuna.webservices.wsaddr2005.handlers;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsaddr2005.EndpointReferenceType;

/**
 * Header handler for parsing the WS-Addressing From header.
 * @author kevin
 */
public class FromHandler extends AddressingHeaderHandler
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
        final EndpointReferenceType from = new EndpointReferenceType(in) ;
        final HandlerAddressingContext addressingContext = HandlerAddressingContext.getHandlerContext(context) ;
        if (!addressingContext.isFaultedFrom())
        {
            if (addressingContext.getFrom() != null)
            {
                addressingContext.setFaultedFrom() ;
                addressingContext.setFrom(null) ;
                if (addressingContext.getFaultHeaderName() == null)
                {
                    addressingContext.setFaultHeaderName(headerName) ;
                    addressingContext.setFaultHeader(from) ;
                }
            }
            else
            {
                addressingContext.setFrom(from) ;
            }
        }
    }
}
