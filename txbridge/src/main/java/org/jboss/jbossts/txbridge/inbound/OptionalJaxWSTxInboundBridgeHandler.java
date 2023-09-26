/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.inbound;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.wst.SystemException;
import org.jboss.jbossts.txbridge.utils.txbridgeLogger;

import jakarta.transaction.Transaction;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class OptionalJaxWSTxInboundBridgeHandler implements Handler {

    private final JaxWSTxInboundBridgeHandler delegate;

    public OptionalJaxWSTxInboundBridgeHandler() {
        delegate = new JaxWSTxInboundBridgeHandler();
    }

    @Override
    public boolean handleMessage(final MessageContext messageContext) {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("OptionalJaxWSTxInboundBridgeHandler.handleMessage()");
        }

        final Boolean isOutbound = (Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (isOutbound == null) {
            return true;
        } else if (isOutbound) {
            return handleOutbound(messageContext);
        } else {
            return handleInbound(messageContext);
        }
    }

    @Override
    public boolean handleFault(final MessageContext messageContext) {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("OptionalJaxWSTxInboundBridgeHandler.handleFault()");
        }

        if (isJTATransactionAvailable()) {
            return delegate.handleFault(messageContext);
        }

        return true;
    }

    @Override
    public void close(final MessageContext messageContext) {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("OptionalJaxWSTxInboundBridgeHandler.close()");
        }
    }

    private boolean handleInbound(final MessageContext messageContext) {
        if (isWSATTransactionAvailable()) {
            return delegate.handleInbound(messageContext);
        }

        return true;
    }

    private boolean handleOutbound(final MessageContext messageContext) {
        if (isJTATransactionAvailable()) {
            return delegate.handleOutbound(messageContext);
        }

        return true;
    }

    private boolean isWSATTransactionAvailable() {
        TxContext txContext = null;

        try {
            txContext = TransactionManagerFactory.transactionManager().currentTransaction();
        } catch (SystemException e) {
        }

        return txContext != null;
    }

    private boolean isJTATransactionAvailable() {
        Transaction transaction = null;

        try {
            transaction = TransactionManager.transactionManager().getTransaction();
        } catch (jakarta.transaction.SystemException e) {
        }

        return transaction != null;
    }
}