/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.outbound;

import org.jboss.jbossts.txbridge.utils.txbridgeLogger;
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
    /**
     * Management object for the subordinate transaction
     */
    private final BridgeWrapper bridgeWrapper;

    /**
     * Create a new OutboundBridge to manage the given subordinate WS-AT transaction.
     *
     * @param bridgeWrapper the subordinate transaction controller
     */
    public OutboundBridge(BridgeWrapper bridgeWrapper)
    {
        txbridgeLogger.logger.trace("OutboundBridge.<ctor>(BridgeWrapper="+bridgeWrapper+")");

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
        txbridgeLogger.logger.trace("OutboundBridge.start(BridgeWrapper="+bridgeWrapper+")");

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
        txbridgeLogger.logger.trace("OutboundBridge.stop(BridgeWrapper="+bridgeWrapper+")");

        TransactionManagerFactory.transactionManager().suspend();
    }
}