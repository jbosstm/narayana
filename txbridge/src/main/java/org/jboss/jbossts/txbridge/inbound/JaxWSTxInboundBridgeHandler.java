/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.inbound;

import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;

import org.jboss.jbossts.txbridge.utils.txbridgeLogger;

/**
 * A handler that sits in the server side JAX-WS processing pipeline between the XTS header
 * context processor and the web service. Takes the WS transaction context provided by the
 * former and maps it to a JTA transaction context for use by the latter. Handles Thread
 * association of the JTA context.
 *
 * Note: we assume that there is a web services transaction context present and
 * that the service needs a JTA context. The handler should not be registered on
 * methods unless both these conditions hold.
 *
 * @author jonathan.halliday@redhat.com, 2007-04-30
 */
public class JaxWSTxInboundBridgeHandler implements Handler
{

    private InboundBridge inboundBridge;

    /**
     * Process a message. Determines if it is inbound or outbound and dispatches accordingly.
     *
     * @param msgContext the context to process
     * @return true on success, false on error
     */
    public boolean handleMessage(MessageContext msgContext)
    {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("JaxWSTxInboundBridgeHandler.handleMessage()");
        }

        Boolean outbound = (Boolean) msgContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound == null)
            throw new IllegalStateException("Cannot obtain required property: " + MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        return outbound ? handleOutbound(msgContext) : handleInbound(msgContext);
    }

    /**
     * Tidy up the Transaction/Thread association before faults are thrown back to the client.
     *
     * @param messageContext unused
     * @return true on success, false on error
     */
    public boolean handleFault(MessageContext messageContext)
    {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("JaxWSTxInboundBridgeHandler.handleFault()");
        }

        return suspendTransaction();
    }

    public void close(MessageContext messageContext)
    {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("JaxWSTxInboundBridgeHandler.close()");
        }
    }

    /**
     * Process inbound messages by mapping the WS transaction context
     * to a JTA one and associating the latter to the current Thread.
     *
     * @param msgContext unused
     * @return true on success, false on error
     */
    protected boolean handleInbound(MessageContext msgContext)
    {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("JaxWSTxInboundBridgeHandler.handleInbound()");
        }

        try
        {
            inboundBridge = org.jboss.jbossts.txbridge.inbound.InboundBridgeManager.getInboundBridge();
            inboundBridge.start();
        }
        catch (Exception e)
        {
            txbridgeLogger.logger.error(e);
            return false;
        }

        return true;
    }

    /**
     * Tidy up the Transaction/Thread association before returning a message to the client.
     *
     * @param msgContext unused
     * @return true on success, false on error
     */
    protected boolean handleOutbound(MessageContext msgContext)
    {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("JaxWSTxInboundBridgeHandler.handleOutbound()");
        }

        return suspendTransaction();
    }

    /**
     * Break the association between the JTA transaction context and the calling Thread.
     *
     * @return true on success, false on error
     */
    private boolean suspendTransaction()
    {
        txbridgeLogger.logger.trace("JaxWSTxInboundBridgeHandler.suspendTransaction()");

        try
        {
            inboundBridge.stop();
        }
        catch (Exception e)
        {
            txbridgeLogger.logger.error(e);
            return false;
        }

        return true;
    }
}