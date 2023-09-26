/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.webservices.logging;

import org.jboss.logging.Logger;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
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
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            soapMessage.writeTo(baos);
            logger.trace(baos);
        } catch (IOException | SOAPException e) {
            logger.trace("Failure on logging content of the SOAP message " + soapMessage, e);
        }
    }
}