/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.txbridge.outbound;

import java.util.Iterator;

import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

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