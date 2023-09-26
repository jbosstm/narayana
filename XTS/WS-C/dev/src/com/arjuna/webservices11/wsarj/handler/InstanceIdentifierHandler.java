/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsarj.handler;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.wsarj.ArjunaConstants;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.ProtocolException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Header handler for parsing the Arjuna WS instance identifier header.
 * @author adinn
 */
public class InstanceIdentifierHandler implements SOAPHandler<SOAPMessageContext>
{
    /**
     * Gets the header blocks that can be processed by this Handler
     * instance.
     *
     * @return Set of QNames of header blocks processed by this
     *         handler instance. <code>QName</code> is the qualified
     *         name of the outermost element of the Header block.
     */
    public Set<QName> getHeaders()
    {
        return headers;
    }

    /**
     * Handle an outgoing message by inserting any current arjuna context attached to the context into the message
     * headers and handle an incoming message by retrieving the context from the headers and attaching it to the
     * context,
     *
     * @param context the message context.
     * @return Always return true
     * @throws RuntimeException               Causes the JAX-WS runtime to cease
     *                                        handler processing and generate a fault.
     * @throws jakarta.xml.ws.ProtocolException Causes the JAX-WS runtime to switch to
     *                                        fault message processing.
     */
    public boolean handleMessage(SOAPMessageContext context) throws ProtocolException
    {
        final boolean outbound = (Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {
            return handleMessageOutbound(context);
        } else {
            return handlemessageInbound(context);
        }
    }

    /**
     * check for an arjuna context attached to the message context and, if found, install its identifier as the value
     * of a soap message header element
     * @param context
     * @return
     * @throws ProtocolException
     */
    protected boolean handleMessageOutbound(SOAPMessageContext context) throws ProtocolException
    {
        try {
            ArjunaContext arjunaContext = ArjunaContext.getCurrentContext(context);
            if (arjunaContext != null) {
                InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier();
                // insert a header into the current message containing the instance identifier as a text element
                final SOAPMessage soapMessage = context.getMessage();
                final SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
                SOAPHeader soapHeader = soapEnvelope.getHeader() ;
                if (soapHeader == null)
                {
                    soapHeader = soapEnvelope.addHeader() ;
                }
                final SOAPHeaderElement headerElement = soapHeader.addHeaderElement(ArjunaConstants.WSARJ_ELEMENT_INSTANCE_IDENTIFIER_QNAME);
                headerElement.setValue(instanceIdentifier.getInstanceIdentifier());
                headerElement.setMustUnderstand(true);
            }
        } catch (Exception se) {
            throw new ProtocolException(se);
        }

        if (WSCLogger.logger.isTraceEnabled()) {
            WSCLogger.logger.trace("InstanceIdentifierHandler.handleMessageOutbound()");
            WSCLogger.traceMessage(context);
        }

        return true;
    }

    /**
     * check for an arjuna instance identifier element embedded in the soap message headesr and, if found, use it to
     * label an arjuna context attached to the message context
     * @param context
     * @return
     * @throws ProtocolException
     */
    private boolean handlemessageInbound(SOAPMessageContext context)  throws ProtocolException
    {
        try {
            final SOAPMessage soapMessage = context.getMessage();
            final SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
            Iterator<SOAPHeaderElement> iterator = soapEnvelope.getHeader().examineAllHeaderElements();
            while (iterator.hasNext()) {
                final SOAPHeaderElement headerElement = iterator.next();
                if (ArjunaConstants.WSARJ_ELEMENT_INSTANCE_IDENTIFIER_QNAME.equals(headerElement.getElementQName())) {
                    // found it - clear the must understand flag, retrieve the value and store an arjuna
                    // context in the message context
                    headerElement.setMustUnderstand(false);
                    String identifierString = headerElement.getValue();
                    if (identifierString != null) {
                        ArjunaContext arjunaContext = ArjunaContext.getContext(context);
                        arjunaContext.setInstanceIdentifier(new InstanceIdentifier(identifierString));
                        break;
                    }
                }
            }
        } catch (Exception se) {
            throw new ProtocolException(se);
        }

        if (WSCLogger.logger.isTraceEnabled()) {
            WSCLogger.logger.trace("InstanceIdentifierHandler.handlemessageInbound()");
            WSCLogger.traceMessage(context);
        }

        return true;
    }

    /**
     * this handler ignores faults but allows other handlers to deal with them
     *
     * @param context the message context
     * @return true to allow fault handling to continue
     */

    public boolean handleFault(SOAPMessageContext context)
    {
        return true;
    }

    /**
     * this hanlder ignores close messages
     *
     * @param context the message context
     */
    public void close(jakarta.xml.ws.handler.MessageContext context)
    {
    }

    /**
     * a singleton set containing the only header this handler is interested in
     */
    private static Set<QName> headers = Collections.singleton(ArjunaConstants.WSARJ_ELEMENT_INSTANCE_IDENTIFIER_QNAME);
}