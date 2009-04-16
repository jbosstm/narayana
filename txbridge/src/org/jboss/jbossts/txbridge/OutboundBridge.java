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
import org.jboss.jbossts.xts.bridge.at.BridgeWrapper;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;

/**
 * Manages Thread association of the interposed coordinator.
 * Typically called from handlers in the WS stack.
 *
 * @author jonathan.halliday@redhat.com, 2009-02-10
 */
public class OutboundBridge
{
    private static Logger log = Logger.getLogger(OutboundBridge.class);

    /**
     * Management object for the subordinate transaction
     */
    private BridgeWrapper bridgeWrapper;

    /**
     * Create a new OutboundBridge to manage the given subordinate WS-AT transaction.
     *
     * @param bridgeWrapper the subordinate transaction controller
     */
    public OutboundBridge(BridgeWrapper bridgeWrapper)
    {
        log.trace("OutboundBridge(BridgeWrapper="+bridgeWrapper+")");

        this.bridgeWrapper = bridgeWrapper;
    }

    /**
     * Associate the WS-AT transaction to the current Thread.
     * Typically used by the client side outbound handler.
     *
     * @throws UnknownTransactionException
     * @throws SystemException
     */
    public void start() throws UnknownTransactionException, SystemException
    {
		log.trace("start(BridgeWrapper="+bridgeWrapper+")");

        TxContext txContext = bridgeWrapper.getContext();

        TransactionManagerFactory.transactionManager().resume(txContext);
	}

    /**
     * Disassociate the WS-AT transaction from the current Thread.
     * Typically used by the client side inbound handler.
     *
     * @throws SystemException
     */
    public void stop() throws SystemException
    {
        log.trace("stop(BridgeWrapper="+bridgeWrapper+")");

        TransactionManagerFactory.transactionManager().suspend();
    }
}
