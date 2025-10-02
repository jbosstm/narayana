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
/*
 * Copyright (C) 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id$
 */

package com.arjuna.mw.wst.client;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import com.arjuna.webservices.wscoor.CoordinationConstants;

/**
 * The class is used to perform WS-Transaction context insertion
 * and extraction for application level SOAP messages using JaxRPC.
 *
 */

public class JaxRPCHeaderContextProcessor extends JaxBaseHeaderContextProcessor implements Handler
{
    /**
     * The handler information.
     */
    private HandlerInfo handlerInfo ;

    /**
     * Initialise the handler information.
     * @param handlerInfo The handler information.
     */
    public void init(final HandlerInfo handlerInfo)
    {
        this.handlerInfo = handlerInfo ;
    }

    /**
     * Destroy the handler.
     */
    public void destroy()
    {
    }

    /**
     * Get the headers.
     * @return the headers.
     */
    public QName[] getHeaders()
    {
		return new QName[] {new QName(CoordinationConstants.WSCOOR_NAMESPACE, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT)};
    }

    /**
     * Handle the request.
     * @param messageContext The current message context.
     */

    public boolean handleRequest(final MessageContext messageContext)
    {
        final SOAPMessageContext soapMessageContext = (SOAPMessageContext)messageContext ;
        final SOAPMessage soapMessage = soapMessageContext.getMessage() ;

        return handleOutboundMessage(soapMessage);
    }

    /**
     * Handle the response.
     * @param messageContext The current message context.
     */
    public boolean handleResponse(final MessageContext messageContext)
    {
        final SOAPMessageContext soapMessageContext = (SOAPMessageContext)messageContext ;
        resumeTransaction(soapMessageContext.getMessage()) ;
        return true ;
    }

    /**
     * Handle the fault.
     * @param messageContext The current message context.
     */
    public boolean handleFault(final MessageContext messageContext)
    {
        final SOAPMessageContext soapMessageContext = (SOAPMessageContext)messageContext ;
        resumeTransaction(soapMessageContext.getMessage()) ;
        return true ;
    }
}
