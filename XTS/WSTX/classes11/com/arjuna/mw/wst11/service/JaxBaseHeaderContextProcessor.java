/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2007,
 * @author JBoss Inc.
 */
package com.arjuna.mw.wst11.service;

import com.arjuna.mw.wst.*;
import com.arjuna.mw.wst11.common.CoordinationContextHelper;
import com.arjuna.mw.wst.common.SOAPUtil;
import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.mwlabs.wst11.at.context.TxContextImple;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import javax.xml.soap.*;
import java.util.Iterator;

/**
 * Common base class for classes used to perform
 * WS-Transaction context manipulation on SOAP messages.
 *
 * @message com.arjuna.mw.wst11.service.JaxHC11P_1 [com.arjuna.mw.wst11.service.JaxHC11P_1] - Error in:
 * @message com.arjuna.mw.wst11.service.JaxHC11P_2 [com.arjuna.mw.wst11.service.JaxHC11P_2] - Stack trace:
 * @message com.arjuna.mw.wst11.service.JaxHC11P_3 [com.arjuna.mw.wst11.service.JaxHC11P_3] - Unknown context type:
 */
class JaxBaseHeaderContextProcessor
{
    /**
     * Handle the request.
     * @param soapMessage The current message context.
     */
    protected boolean handleInboundMessage(final SOAPMessage soapMessage)
    {
        if (soapMessage != null)
        {
            try
            {
                final SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope() ;
                final SOAPHeader soapHeader = soapEnvelope.getHeader() ;
                final SOAPHeaderElement soapHeaderElement = getHeaderElement(soapHeader, CoordinationConstants.WSCOOR_NAMESPACE, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT) ;

                if (soapHeaderElement != null)
                {
                    final CoordinationContextType cc = CoordinationContextHelper.deserialise(soapHeaderElement) ;
                    final String coordinationType = cc.getCoordinationType();
                    if (AtomicTransactionConstants.WSAT_PROTOCOL.equals(coordinationType))
                    {
                        final TxContext txContext = new TxContextImple(cc) ;
                        TransactionManagerFactory.transactionManager().resume(txContext) ;
                        clearMustUnderstand(soapHeader, soapHeaderElement) ;
                    }
                    else if (BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME.equals(coordinationType))
                    {
                        final TxContext txContext = new com.arjuna.mwlabs.wst11.ba.context.TxContextImple(cc);
                        BusinessActivityManagerFactory.businessActivityManager().resume(txContext) ;
                        clearMustUnderstand(soapHeader, soapHeaderElement) ;
                    }
                    else
                    {
                        wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst11.service.JaxHC11P_1",
                                new Object[]{"com.arjuna.mw.wst11.service.JaxBaseHeaderContextProcessor.handleRequest(MessageContext context)"});

                        wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst11.service.JaxHC11P_3",
                                new Object[]{coordinationType});
                    }
                }
            }
            catch (final Throwable th)
            {
                wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst11.service.JaxHC11P_1",
                        new Object[]{"com.arjuna.mw.wst11.service.JaxBaseHeaderContextProcessor.handleRequest(MessageContext context)"});

                wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst11.service.JaxHC11P_2",
                        new Object[]{th});
            }
        }
        return true ;
    }

    /**
     * Suspend the current transaction.
     */
    protected void suspendTransaction()
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
            wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst11.service.JaxHC11P_1",
                    new Object[]{"com.arjuna.mw.wst11.service.JaxBaseHeaderContextProcessor.suspendTransaction()"});

            wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst11.service.JaxHC11P_2",
                    new Object[]{th});

            th.printStackTrace(System.err) ;
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
}