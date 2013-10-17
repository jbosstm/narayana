/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
 */
package org.jboss.jbossts.txbridge.inbound;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.wst.SystemException;
import org.jboss.jbossts.txbridge.utils.txbridgeLogger;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class OptionalJaxWSTxInboundBridgeHandler implements Handler {

    private JaxWSTxInboundBridgeHandler delegate;

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

        if (delegate != null) {
            return delegate.handleFault(messageContext);
        }

        return true;
    }

    @Override
    public void close(final MessageContext messageContext) {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("OptionalJaxWSTxInboundBridgeHandler.close()");
        }

        if (delegate != null) {
            delegate.close(messageContext);
        }
    }

    private boolean handleInbound(final MessageContext messageContext) {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("OptionalJaxWSTxInboundBridgeHandler.handleInbound()");
        }

        if (isTransactionAvailable()) {
            delegate = new JaxWSTxInboundBridgeHandler();
            return delegate.handleInbound(messageContext);
        }

        delegate = null;
        return true;
    }

    private boolean handleOutbound(final MessageContext messageContext) {
        if (txbridgeLogger.logger.isTraceEnabled()) {
            txbridgeLogger.logger.trace("OptionalJaxWSTxInboundBridgeHandler.handleOutbound()");
        }

        if (delegate != null) {
            return delegate.handleOutbound(messageContext);
        }

        return true;
    }

    private boolean isTransactionAvailable() {
        TxContext txContext = null;

        try {
            txContext = TransactionManagerFactory.transactionManager().currentTransaction();
        } catch (SystemException e) {
        }

        return txContext != null;
    }
}
