/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * (C) 2009 @author Red Hat Middleware LLC
 */
package org.jboss.jbossts.txbridge;

import org.apache.log4j.Logger;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

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
	private static Logger log = Logger.getLogger(JaxWSTxOutboundBridgeHandler.class);

	/**
	 * Process a message. Determines if it is inbound or outbound and dispatches accordingly.
	 *
	 * @param msgContext the context to process
	 * @return true on success, false on error
	 */
	public boolean handleMessage(MessageContext msgContext)
	{
		log.trace("handleMessage()");

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
		log.trace("handleFault()");

		return suspendTransaction();
	}

	public void close(MessageContext messageContext)
	{
		log.trace("close()");
	}

	/**
     * Tidy up the Transaction/Thread association before returning a message to the client.
	 *
	 * @param msgContext unused
	 * @return true on success, false on error
	 */
	protected boolean handleInbound(MessageContext msgContext)
	{
		log.trace("handleInbound()");

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
		log.trace("handleOutbound()");

		try
		{
			OutboundBridge outboundBridge = OutboundBridgeManager.getOutboundBridge();
			outboundBridge.start();
		}
		catch (Exception e)
		{
			log.error(e);
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
		log.trace("suspendTransaction()");

		try
		{
			OutboundBridge outboundBridge = OutboundBridgeManager.getOutboundBridge();
			outboundBridge.stop();
		}
		catch (Exception e)
		{
			log.error(e);
            return false;
		}

        return true;
	}
}