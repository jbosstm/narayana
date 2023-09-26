/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.mw.wst11.service;

import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.mw.wst11.service.JaxBaseHeaderContextProcessor;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.util.HashSet;
import java.util.Set;

/**
 * The class is used to perform WS-Transaction context insertion
 * and extraction for application level SOAP messages using JaxWS.
 * This is the server side version.
 */
public class JaxWSHeaderContextProcessor extends JaxBaseHeaderContextProcessor implements SOAPHandler<SOAPMessageContext>
{
    /**
     * Process a message. Determines if it is inbound or outbound and dispatches accordingly.
     *
     * @param msgContext
     * @return true
     */
    public boolean handleMessage(SOAPMessageContext msgContext)
    {
        Boolean outbound = (Boolean)msgContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound == null)
            throw new IllegalStateException("Cannot obtain required property: " + MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        return outbound ? handleOutbound(msgContext) : handleInbound(msgContext);
    }

    /**
     * Tidy up the Transaction/Thread association before faults are thrown back to the client.
     *
     * @param messageContext
     * @return true
     */
    public boolean handleFault(SOAPMessageContext messageContext)
    {
        if (wstxLogger.logger.isTraceEnabled()) {
            wstxLogger.logger.trace("service/JaxWSHeaderContextProcessor.handleFault()");
            wstxLogger.traceMessage(messageContext);
        }

        suspendTransaction() ;
        return true;
    }

    public void close(MessageContext messageContext)
    {
    }

    /**
     * Gets the header blocks that can be processed by this Handler instance.
     */
    public Set<QName> getHeaders()
    {
        Set<QName> headerSet = new HashSet<QName>();
        headerSet.add(new QName(CoordinationConstants.WSCOOR_NAMESPACE, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT));

        return headerSet;
    }

    /**
     * Sets the header blocks that can be processed by this Handler instance.
     * Note: this impl ignores this function's args as the values are hardcoded.
     */
    public void setHeaders(Set headers)
    {
    }


    /**
     * Process the tx context header that is attached to the received message.
     *
     * @param msgContext
     * @return true
     */
    protected boolean handleInbound(SOAPMessageContext msgContext)
    {
        if (wstxLogger.logger.isTraceEnabled()) {
            wstxLogger.logger.trace("service/JaxWSHeaderContextProcessor.handleInbound()");
            wstxLogger.traceMessage(msgContext);
        }

        final SOAPMessageContext soapMessageContext = (SOAPMessageContext)msgContext ;
        final SOAPMessage soapMessage = soapMessageContext.getMessage() ;

        return handleInboundMessage(soapMessage);
    }

    /**
     * Tidy up the Transaction/Thread association before response is returned to the client.
     *
     * @param messageContext The current message context.
     * @return true
     */
    protected boolean handleOutbound(SOAPMessageContext messageContext)
    {
        if (wstxLogger.logger.isTraceEnabled()) {
            wstxLogger.logger.trace("service/JaxWSHeaderContextProcessor.handleOutbound()");
            wstxLogger.traceMessage(messageContext);
        }

        suspendTransaction() ;
        return true;
    }
}