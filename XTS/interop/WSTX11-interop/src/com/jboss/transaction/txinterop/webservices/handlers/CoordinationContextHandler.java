/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.jboss.transaction.txinterop.webservices.handlers;

import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.jboss.transaction.txinterop.webservices.CoordinationContextManager;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.ProtocolException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.util.Map;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.w3c.dom.Node;

/**
 * Handler to serialise and deserialise a coordination context to/from a SOAP header.
 */
public class CoordinationContextHandler implements SOAPHandler<SOAPMessageContext> {
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
     * @throws javax.xml.ws.ProtocolException Causes the JAX-WS runtime to switch to
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
            CoordinationContextType coordinationContext = CoordinationContextManager.getThreadContext();
            if (coordinationContext != null) {
                final JAXBContext jaxbCtx = getJaxbContext();

                // insert a header into the current message containing the coordination context
                final SOAPMessage soapMessage = context.getMessage();
                final SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
                SOAPHeader soapHeader = soapEnvelope.getHeader() ;
                if (soapHeader == null)
                {
                    soapHeader = soapEnvelope.addHeader() ;
                }
                /*
                 * this does not work but it is what we want!!
                 *
                 * The problem here is that the marshaller creates plain old elements and inserts them top
                 * down as it goes along. but the soap header add child method checks its argument and
                 * replaces plain elements with soap header elements before inserting them. it copies the
                 * inserted element substructure into the rpelacement but since it does not exist at
                 * copy time the chiuldren get lost
                Marshaller marshaller = jaxbCtx.createMarshaller();
                marshaller.marshal(coordinationContext, soapHeader);
                 */
                /*
                 * ok, here's the workaround -- marshall the object as a child of a dummy header, detach it and
                 * then insert it as a header element.
                 */
                SOAPHeaderElement headerElement = soapHeader.addHeaderElement(getDummyQName());
                Marshaller marshaller = jaxbCtx.createMarshaller();
                marshaller.marshal(coordinationContext, headerElement);
                soapHeader.replaceChild(headerElement.getChildNodes().item(0), headerElement);
                // ok, now we need to locate the inserted node and set the mustunderstand attribute
                Iterator<SOAPHeaderElement> iterator = soapHeader.examineAllHeaderElements();
                while (iterator.hasNext()) {
                    headerElement = iterator.next();
                    if (CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT_QNAME.equals(headerElement.getElementQName())) {
                        headerElement.setMustUnderstand(true);
                        break;
                    }
                }
            }
        } catch (Exception se) {
            throw new ProtocolException(se);
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
                if (CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT_QNAME.equals(headerElement.getElementQName())) {
                    // found it - clear the must understand flag, retrieve the value and store an arjuna
                    // context in the message context
                    headerElement.setMustUnderstand(false);
                    final JAXBContext jaxbCtx = getJaxbContext();
                    final JAXBElement<CoordinationContextType> elt = jaxbCtx.createUnmarshaller().unmarshal(headerElement, CoordinationContextType.class);
                    final CoordinationContextType coordinationContext = elt.getValue();
                    CoordinationContextManager.setContext(context, coordinationContext);
                }
            }
        } catch (Exception se) {
            throw new ProtocolException(se);
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
    public void close(javax.xml.ws.handler.MessageContext context)
    {
    }

    /**
     * a singleton set containing the only header this handler is interested in
     */
    private static Set<QName> headers = Collections.singleton(CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT_QNAME);

    private static JAXBContext jaxbContext;
    private synchronized JAXBContext getJaxbContext()
    {
        if (jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance("org.oasis_open.docs.ws_tx.wscoor._2006._06");
            } catch (JAXBException e) {
                // TODO log error here
            }
        }

        return jaxbContext;
    }
    private static QName dummyQName = null;
    private synchronized QName getDummyQName()
    {
        if (dummyQName == null) {
            dummyQName = new QName("http://transactions.jboss.com/xts/dummy/", "DummyElement", "dummy");
        }

        return dummyQName;
    }
}
