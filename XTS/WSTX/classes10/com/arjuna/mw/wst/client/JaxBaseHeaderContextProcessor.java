/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * (C) 2007,
 * @author JBoss Inc.
 */
package com.arjuna.mw.wst.client;

import com.arjuna.mw.wst.common.CoordinationContextHelper;
import com.arjuna.mw.wst.common.SOAPUtil;
import com.arjuna.mw.wsc.context.Context;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsba.BusinessActivityConstants;

import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.BusinessActivityManagerFactory;
import com.arjuna.mw.wst.TransactionManager;
import com.arjuna.mw.wst.TransactionManagerFactory;
import com.arjuna.mw.wst.TxContext;

import javax.xml.soap.*;

import java.util.Iterator;

/**
 * Common base class for classes used to perform
 * WS-Transaction context manipulation on SOAP messages.
 *
 */
public class JaxBaseHeaderContextProcessor
{
    /**
     * Handle the request.
     * @param soapMessage The current message context.
     */

    public boolean handleOutboundMessage(final SOAPMessage soapMessage)
    {
        if (soapMessage == null)
        {
            return true ;
        }

        try
        {
            /*
             * There should either be an Atomic Transaction *or* a Business Activity
             * associated with the thread.
             */
            final TransactionManager transactionManager = TransactionManagerFactory.transactionManager() ;
            final BusinessActivityManager businessActivityManager = BusinessActivityManagerFactory.businessActivityManager() ;

            final Context atContext ;
            if (transactionManager != null)
            {
                final com.arjuna.mwlabs.wst.at.context.TxContextImple txContext =
                    (com.arjuna.mwlabs.wst.at.context.TxContextImple)transactionManager.currentTransaction() ;
                atContext = (txContext == null ? null : txContext.context()) ;
            }
            else
            {
                atContext = null ;
            }

            final Context baContext ;
            if (businessActivityManager != null)
            {
                final com.arjuna.mwlabs.wst.ba.context.TxContextImple txContext =
                    (com.arjuna.mwlabs.wst.ba.context.TxContextImple)businessActivityManager.currentTransaction() ;
                baContext = (txContext == null ? null : txContext.context()) ;
            }
            else
            {
                baContext = null ;
            }

            final CoordinationContextType coordinationContext ;
            if (atContext != null)
            {
                coordinationContext = atContext.getCoordinationContext() ;
            }
            else if (baContext != null)
            {
                coordinationContext = baContext.getCoordinationContext() ;
            }
            else
            {
                coordinationContext = null ;
            }

            if (coordinationContext != null)
            {
                final SOAPEnvelope env = soapMessage.getSOAPPart().getEnvelope() ;
                SOAPHeader header = env.getHeader() ;
                if (header == null)
                {
                    header = env.addHeader() ;
                }
                final Name name = env.createName(CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT, CoordinationConstants.WSCOOR_PREFIX, CoordinationConstants.WSCOOR_NAMESPACE) ;
                final SOAPHeaderElement headerElement = header.addHeaderElement(name) ;
                headerElement.addNamespaceDeclaration(CoordinationConstants.WSCOOR_PREFIX, CoordinationConstants.WSCOOR_NAMESPACE) ;
                headerElement.setMustUnderstand(true) ;
                CoordinationContextHelper.serialise(env, headerElement, coordinationContext) ;
            }
        }
        catch (final Throwable th) {
            wstxLogger.i18NLogger.warn_mw_wst_client_JaxHCP_1("com.arjuna.mw.wst.client.JaxRPCHeaderContextProcessor.handleRequest()", th);
        }

        return true ;
    }

    /**
     * Resume the current transaction.
     *
     */

    protected void resumeTransaction(final SOAPMessage soapMessage)
    {
        if (soapMessage != null)
        {
            try
            {
                final SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope() ;
                final SOAPHeaderElement soapHeaderElement = getHeaderElement(soapEnvelope, CoordinationConstants.WSCOOR_NAMESPACE, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT) ;

                if (soapHeaderElement != null)
                {
                    final CoordinationContextType cc = CoordinationContextHelper.deserialise(soapEnvelope, soapHeaderElement) ;
                    if (cc != null)
                    {
                        final String coordinationType = cc.getCoordinationType().getValue() ;
                        if (AtomicTransactionConstants.WSAT_PROTOCOL.equals(coordinationType))
                        {
                            final TxContext txContext = new com.arjuna.mwlabs.wst.at.context.TxContextImple(cc) ;
                            TransactionManagerFactory.transactionManager().resume(txContext) ;
                        }
                        else if (BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME.equals(coordinationType))
                        {
                            final TxContext txContext = new com.arjuna.mwlabs.wst.ba.context.TxContextImple(cc);
                            BusinessActivityManagerFactory.businessActivityManager().resume(txContext) ;
                        }
                        else {
                            wstxLogger.i18NLogger.warn_mw_wst_client_JaxHCP_2("com.arjuna.mw.wst.client.JaxRPCHeaderContextProcessor.resumeTransaction()", coordinationType);
                        }
                    }
                }
            }
            catch (final Throwable th) {
                wstxLogger.i18NLogger.warn_mw_wst_client_JaxHCP_1("com.arjuna.mw.wst.client.JaxRPCHeaderContextProcessor.resumeTransaction()", th);
            }
        }
    }

    /**
     * Retrieve the first header matching the uri and name.
     * @param soapEnvelope The soap envelope containing the header.
     * @param uri The uri of the header element.
     * @param name The name of the header element.
     * @return The header element or null if not found.
     */
    private SOAPHeaderElement getHeaderElement(final SOAPEnvelope soapEnvelope, final String uri, final String name)
        throws SOAPException
    {
        final SOAPHeader soapHeader = soapEnvelope.getHeader() ;
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
}
