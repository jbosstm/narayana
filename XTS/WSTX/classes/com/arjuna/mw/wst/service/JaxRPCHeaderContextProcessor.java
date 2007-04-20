/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id$
 */

package com.arjuna.mw.wst.service;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.BusinessActivityManagerFactory;
import com.arjuna.mw.wst.TransactionManager;
import com.arjuna.mw.wst.TransactionManagerFactory;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst.common.CoordinationContextHelper;
import com.arjuna.mw.wst.common.SOAPUtil;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;

/**
 * The class is used to perform WS-Transaction context insertion
 * and extraction for application level SOAP messages using JaxRPC.
 *
 * @message com.arjuna.mw.wst.service.JaxRPCHCP_1 [com.arjuna.mw.wst.service.JaxRPCHCP_1] - Error in:
 * @message com.arjuna.mw.wst.service.JaxRPCHCP_2 [com.arjuna.mw.wst.service.JaxRPCHCP_2] - Stack trace:
 * @message com.arjuna.mw.wst.service.JaxRPCHCP_3 [com.arjuna.mw.wst.service.JaxRPCHCP_3] - Unknown context type:
 */

public class JaxRPCHeaderContextProcessor implements Handler
{
    /**
     * The handler information.
     */
    private HandlerInfo handlerInfo ;

    /**
     * Initialise the handler information.
     * @param handlerInfo The handler information.
     */
    public void init(final HandlerInfo handlerInfo)
    {
        this.handlerInfo = handlerInfo ;
    }

    /**
     * Destroy the handler.
     */
    public void destroy()
    {
    }

    /**
     * Get the headers.
     * @return the headers.
     */
    public QName[] getHeaders()
    {
		return new QName[] {new QName(CoordinationConstants.WSCOOR_NAMESPACE, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT)};
    }

    /**
     * Handle the request.
     * @param messageContext The current message context.
     */
    public boolean handleRequest(final MessageContext messageContext)
    {
		final SOAPMessageContext soapMessageContext = (SOAPMessageContext)messageContext ;
        final SOAPMessage soapMessage = soapMessageContext.getMessage() ;

        if (soapMessage != null)
        {
            try
            {
                final SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope() ;
                final SOAPHeader soapHeader = soapEnvelope.getHeader() ;
                final SOAPHeaderElement soapHeaderElement = getHeaderElement(soapHeader, CoordinationConstants.WSCOOR_NAMESPACE, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT) ;

                if (soapHeaderElement != null)
                {
                    final CoordinationContextType cc = CoordinationContextHelper.deserialise(soapEnvelope, soapHeaderElement) ;
                    final String coordinationType = cc.getCoordinationType().getValue() ;
                    if (AtomicTransactionConstants.WSAT_PROTOCOL.equals(coordinationType))
                    {
                        final TxContext txContext = new com.arjuna.mwlabs.wst.at.context.TxContextImple(cc) ;
                        TransactionManagerFactory.transactionManager().resume(txContext) ;
                        clearMustUnderstand(soapHeader, soapHeaderElement) ;
                    }
                    else if (BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME.equals(coordinationType))
                    {
                        final TxContext txContext = new com.arjuna.mwlabs.wst.ba.context.TxContextImple(cc);
                        BusinessActivityManagerFactory.businessActivityManager().resume(txContext) ;
                        clearMustUnderstand(soapHeader, soapHeaderElement) ;
                    }
                    else
                    {
                        wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.service.JaxRPCHCP_1",
                            new Object[]{"com.arjuna.mw.wst.service.JaxRPCHeaderContextProcessor.handleRequest(MessageContext context)"});

            		    wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.service.JaxRPCHCP_3",
                            new Object[]{coordinationType});
                    }
                }
            }
            catch (final Throwable th)
            {
        		wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.service.JaxRPCHCP_1",
                    new Object[]{"com.arjuna.mw.wst.service.JaxRPCHeaderContextProcessor.handleRequest(MessageContext context)"});

        		wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.service.JaxRPCHCP_2",
                    new Object[]{th});
            }
        }
        return true ;
    }

    /**
     * Handle the response.
     * @param messageContext The current message context.
     */
    public boolean handleResponse(final MessageContext messageContext)
    {
        suspendTransaction() ;
        return true ;
    }

    /**
     * Handle the fault.
     * @param messageContext The current message context.
     */
    public boolean handleFault(final MessageContext messageContext)
    {
        suspendTransaction() ;
        return true ;
    }

    /**
     * Suspend the current transaction.
     */
    private void suspendTransaction()
    {
        try
        {
            /*
             * There should either be an Atomic Transaction *or* a Business Activity
             * associated with the thread.
             */
            final TransactionManager transactionManager = TransactionManagerFactory.transactionManager() ;
            final BusinessActivityManager businessActivityManager = BusinessActivityManagerFactory.businessActivityManager() ;

            if (transactionManager != null)
            {
                transactionManager.suspend() ;
            }

            if (businessActivityManager != null)
            {
                businessActivityManager.suspend() ;
            }
        }
        catch (final Throwable th)
        {
	    wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.service.JaxRPCHCP_1",
					  new Object[]{"com.arjuna.mw.wst.service.JaxRPCHeaderContextProcessor.suspendTransaction()"});

	    wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.service.JaxRPCHCP_2",
					  new Object[]{th});

            th.printStackTrace(System.err) ;
        }
    }

    /**
     * Retrieve the first header matching the uri and name.
     * @param soapHeader The soap header containing the header element.
     * @param uri The uri of the header element.
     * @param name The name of the header element.
     * @return The header element or null if not found.
     */
    private SOAPHeaderElement getHeaderElement(final SOAPHeader soapHeader, final String uri, final String name)
        throws SOAPException
    {
        if (soapHeader != null)
        {
            final Iterator headerIter = SOAPUtil.getChildElements(soapHeader) ;
            while(headerIter.hasNext())
            {
                final SOAPHeaderElement current = (SOAPHeaderElement)headerIter.next() ;
                final Name currentName = current.getElementName() ;
                if ((currentName != null) &&
                    match(name, currentName.getLocalName()) &&
                    match(uri, currentName.getURI()))
                {
                    return current ;
                }
            }
        }
        return null ;
    }

    /**
     * Do the two references match?
     * @param lhs The first reference.
     * @param rhs The second reference.
     * @return true if the references are both null or if they are equal.
     */
    private boolean match(final Object lhs, final Object rhs)
    {
        if (lhs == null)
        {
            return (rhs == null) ;
        }
        else
        {
            return lhs.equals(rhs) ;
        }
    }

    /**
     * Clear the soap MustUnderstand.
     * @param soapHeader The SOAP header.
     * @param soapHeaderElement The SOAP header element.
     */
    private void clearMustUnderstand(final SOAPHeader soapHeader, final SOAPHeaderElement soapHeaderElement)
    	throws SOAPException
    {
	final Name headerName = soapHeader.getElementName() ;

	final SOAPFactory factory = SOAPFactory.newInstance() ;
	final Name attributeName = factory.createName("mustUnderstand", headerName.getPrefix(), headerName.getURI()) ;

	soapHeaderElement.removeAttribute(attributeName) ;
    }
}
