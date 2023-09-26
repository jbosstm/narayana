/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.outbound;

import org.jboss.jbossts.txbridge.utils.txbridgeLogger;

import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;

/**
 * A handler that sits in the client side JAX-WS processing pipeline between the application
 * and the XTS header context processor. Takes the JTA transaction context provided by the
 * former and maps it to a WS-AT transaction context for use by the latter. Handles Thread
 * association of the WS-AT context.
 *
 * Note: we assume that there is a JTA transaction context present and
 * that the service needs a WS-AT context. The handler should not be registered on
 * methods unless both these conditions hold.
 *
 * @author jonathan.halliday@redhat.com, 2009-02-10
 */
public class JaxWSTxOutboundBridgeHandler implements Handler
{
    /**
     * Process a message. Determines if it is inbound or outbound and dispatches accordingly.
     *
     * @param msgContext the context to process
     * @return true on success, false on error
     */
    public boolean handleMessage(MessageContext msgContext)
    {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("JaxWSTxOutboundBridgeHandler.handleMessage()");
        }

        Boolean outbound = (Boolean)msgContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
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
            txbridgeLogger.logger.trace("JaxWSTxOutboundBridgeHandler.handleFault()");
        }

        return suspendTransaction();
    }

    public void close(MessageContext messageContext)
    {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("JaxWSTxOutboundBridgeHandler.close()");
        }
    }

    /**
     * Tidy up the Transaction/Thread association before returning a message to the client.
     *
     * @param msgContext unused
     * @return true on success, false on error
     */
    protected boolean handleInbound(MessageContext msgContext)
    {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("JaxWSTxOutboundBridgeHandler.handleInbound()");
        }

        return suspendTransaction();
    }

    /**
     * Process outbound messages by mapping the JTA transaction context
     * to a WS-AT one and associating the latter to the current Thread.
     *
     * @param msgContext unused
     * @return true on success, false on error
     */
    protected boolean handleOutbound(MessageContext msgContext)
    {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("JaxWSTxOutboundBridgeHandler.handleOutbound()");
        }

        try
        {
            OutboundBridge outboundBridge = OutboundBridgeManager.getOutboundBridge();
            outboundBridge.start();
        }
        catch (Exception e)
        {
            txbridgeLogger.logger.error(e);
            return false;
        }

        return true;
    }

    /**
     * Break the association between the WS-AT transaction context and the calling Thread.
     *
     * @return true on success, false on error
     */
    private boolean suspendTransaction()
    {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("JaxWSTxOutboundBridgeHandler.suspendTransaction()");
        }

        try
        {
            OutboundBridge outboundBridge = OutboundBridgeManager.getOutboundBridge();
            outboundBridge.stop();
        }
        catch (Exception e)
        {
            txbridgeLogger.logger.error(e);
            return false;
        }

        return true;
    }
}