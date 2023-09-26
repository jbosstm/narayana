/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wstx.logging;

import org.jboss.logging.Logger;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class wstxLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.mw.wstx");
    public static final wstxI18NLogger i18NLogger = Logger.getMessageLogger(wstxI18NLogger.class, "com.arjuna.mw.wstx");

    /**
     * Print the content of the SOAP message.
     *
     * @param soapMessageContext  SOAP message context to extract and log the message content from
     */
    public static final void traceMessage(SOAPMessageContext soapMessageContext) {
        SOAPMessage soapMessage = ((SOAPMessageContext) soapMessageContext).getMessage();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            soapMessage.writeTo(baos);
            logger.trace(baos);
        } catch (IOException | SOAPException e) {
            logger.trace("Failure on logging content of the SOAP message " + soapMessage, e);
        }
    }
}