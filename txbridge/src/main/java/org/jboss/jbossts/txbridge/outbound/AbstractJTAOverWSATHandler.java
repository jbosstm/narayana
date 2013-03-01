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

package org.jboss.jbossts.txbridge.outbound;

import java.util.Iterator;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.mw.wst.common.SOAPUtil;
import com.arjuna.webservices.wsarj.ArjunaConstants;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public abstract class AbstractJTAOverWSATHandler<C extends MessageContext> implements Handler<C> {

    /**
     * Delegate handler does all the work if context propagation is enabled.
     */
    private final JaxWSTxOutboundBridgeHandler delegateHandler = new JaxWSTxOutboundBridgeHandler();

    /**
     * Delegates message handling to the JaxWSTxOutboundBridgeHandler if either JTAOverWSATFeature or default context
     * propagation is enabled.
     *
     * @see org.jboss.jbossts.txbridge.outbound.JaxWSTxOutboundBridgeHandler#handleMessage(MessageContext)
     *
     * @param context
     * @return true on success, false on error
     */
    @Override
    public boolean handleMessage(C context) {
        if (isContextPropagationEnabled(context) && isJTATransactionOnThread() && !isWSATContext(context)) {
            return delegateHandler.handleMessage(context);
        }

        return true;
    }

    /**
     * Delegates fault handling to the JaxWSTxOutboundBridgeHandler if either JTAOverWSATFeature or default context propagation
     * is enabled.
     *
     * @see org.jboss.jbossts.txbridge.outbound.JaxWSTxOutboundBridgeHandler#handleFault(MessageContext)
     *
     * @param context
     * @return true on success, false on error
     */
    @Override
    public boolean handleFault(C context) {
        if (isContextPropagationEnabled(context) && isJTATransactionOnThread() && !isWSATContext(context)) {
            return delegateHandler.handleFault(context);
        }

        return true;
    }

    /**
     * Delegates to the JaxWSTxOutboundBridgeHandler.
     *
     * @see org.jboss.jbossts.txbridge.outbound.JaxWSTxOutboundBridgeHandler#close(MessageContext)
     *
     * @param context
     */
    @Override
    public void close(MessageContext context) {
        delegateHandler.close(context);
    }

    /**
     * Checks if current thread is already associated with JTA transaction.
     *
     * @return true if thread has a transaction and false if not.
     */
    private boolean isJTATransactionOnThread() {
        boolean isJTATransaction = false;

        try {
            final Transaction transaction = TransactionManager.transactionManager().getTransaction();
            isJTATransaction = transaction != null;
        } catch (SystemException e) {
        }

        return isJTATransaction;
    }

    /**
     * Checks if message context contains WS-AT header element.
     *
     * @param context
     * @return true if WS-AT header element exists and false if not.
     */
    private boolean isWSATContext(final MessageContext context) {
        boolean isWSATContext = false;

        if (context instanceof SOAPMessageContext) {
            final SOAPMessageContext soapMessageContext = (SOAPMessageContext) context;
            final SOAPHeaderElement soapHeaderElement = getHeaderElement(soapMessageContext, ArjunaConstants.WSARJ_NAMESPACE,
                    ArjunaConstants.WSARJ_ELEMENT_INSTANCE_IDENTIFIER);

            isWSATContext = soapHeaderElement != null;
        }

        return isWSATContext;
    }

    /**
     * Extracts and returns SOAP header element from the SOAP message context based on <code>uri</code> and <code>name</code>.
     *
     * @param soapMessageContext
     * @param uri
     * @param name
     * @return SOAP header element if such element existed and <code>null</code> if not.
     */
    private SOAPHeaderElement getHeaderElement(final SOAPMessageContext soapMessageContext, final String uri, final String name) {
        SOAPHeaderElement soapHeaderElement = null;

        try {
            final SOAPMessage soapMessage = soapMessageContext.getMessage();
            final SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
            final SOAPHeader soapHeader = soapEnvelope.getHeader();

            if (soapHeader != null) {
                soapHeaderElement = getHeaderElement(soapHeader, uri, name);
            }
        } catch (SOAPException e) {
        }

        return soapHeaderElement;
    }

    /**
     * Extracts and returns SOAP header element from the SOAP header based on <code>uri</code> and <code>name</code>.
     *
     * @param soapHeader
     * @param uri
     * @param name
     * @return SOAP header element if such element existed and <code>null</code> if not.
     */
    private SOAPHeaderElement getHeaderElement(final SOAPHeader soapHeader, final String uri, final String name)
            throws SOAPException {

        @SuppressWarnings("unchecked")
        final Iterator<SOAPHeaderElement> iterator = SOAPUtil.getChildElements(soapHeader);

        while (iterator.hasNext()) {
            final SOAPHeaderElement current = iterator.next();
            final Name currentName = current.getElementName();

            if ((currentName != null) && match(name, currentName.getLocalName()) && match(uri, currentName.getURI())) {
                return current;
            }
        }

        return null;
    }

    /**
     * Compares two objects.
     *
     * @param lhs
     * @param rhs
     * @return true|false
     */
    private boolean match(final Object lhs, final Object rhs) {
        if (lhs == null) {
            return rhs == null;
        }

        return lhs.equals(rhs);
    }

    /**
     * Checks if JTAOverWSATHandler should propagate JTA context over WS-AT.
     *
     * @param context
     * @return true|false
     */
    protected abstract boolean isContextPropagationEnabled(C context);

}
