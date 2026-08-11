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

package com.arjuna.mw.wst.service;

import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPMessage;

/**
 * The class is used to perform WS-Transaction context insertion
 * and extraction for application level SOAP messages using JaxRPC.
 */
public class JaxRPCSubordinateHeaderContextProcessor extends JaxRPCHeaderContextProcessor implements Handler
{
    /**
     * Handle the request.
     * @param messageContext The current message context.
     */
    public boolean handleRequest(final MessageContext messageContext)
    {
		final SOAPMessageContext soapMessageContext = (SOAPMessageContext)messageContext ;
        final SOAPMessage soapMessage = soapMessageContext.getMessage() ;

        // the generic handler can do the job for us -- just pass the correct flag

        return handleInboundMessage(soapMessage, false);
    }
}