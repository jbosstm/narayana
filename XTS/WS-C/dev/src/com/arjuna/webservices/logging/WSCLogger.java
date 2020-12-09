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
 * Copyright (C) 2005,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: WSCLogger.java,v 1.1.2.1 2005/11/22 10:32:48 kconner Exp $
 */

package com.arjuna.webservices.logging;

import org.jboss.logging.Logger;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * WS-C logger instances.
 */
public class WSCLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.wsc");
    public static final wscI18NLogger i18NLogger = Logger.getMessageLogger(wscI18NLogger.class, "com.arjuna.wsc");

    /**
     * Logging to trace category the content of the SOAP message.
     *
     * @param soapMessageContext  soap message context converted to extract and log the SOAP message
     */
    public static final void traceMessage(SOAPMessageContext soapMessageContext) {
        SOAPMessage soapMessage = ((SOAPMessageContext) soapMessageContext).getMessage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            soapMessage.writeTo(baos);
            logger.trace(baos);
        } catch (IOException e) {
            logger.trace("Failure on logging content of the SOAP message " + soapMessage, e);
        } catch (SOAPException e) {
            logger.trace("Failure on logging content of the SOAP message " + soapMessage, e);
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                logger.trace("unable to close ByteArrayOutputStream during trace logging", e);
            }
        }
    }
}
