/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * Copyright (C) 2005,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id$
 */

/**
 * @author Malik SAHEB 
 */

package com.arjuna.mw.wst.client;

import com.arjuna.mw.wst.BusinessActivityManagerFactory;
import com.arjuna.mw.wst.TransactionManagerFactory;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst.common.CoordinationContextHelper;
import com.arjuna.mw.wst.common.Protocols;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;

import electric.soap.ISOAPInterceptor;
import electric.xml.Element;

/**
 * The class is used to perform WS-Transaction context insertion
 * and extraction for application level SOAP messages within WebMethods Glue.
 */
public class GlueIncomingContextInterceptor implements ISOAPInterceptor
{
    public void intercept( electric.soap.SOAPMessage message, electric.util.Context messageContext )
    {
        resumeTransaction(message) ;
        return ;
    }    

    /**
     * Resume the current transaction.
     *
     * @message com.arjuna.mw.wst.client.GlueICI_1 [com.arjuna.mw.wst.client.GlueICI_1] - Error in: 
     * @message com.arjuna.mw.wst.client.GlueICI_2 [com.arjuna.mw.wst.client.GlueICI_2] - Unknown context type: 
     * @message com.arjuna.mw.wst.client.GlueICI_3 [com.arjuna.mw.wst.client.GlueICI_3] - Stack trace: 
     */

    private void resumeTransaction(final electric.soap.SOAPMessage soapMessage)
    {
    	if (soapMessage != null)
    	{
            try
            {
                final Element soapHeaderElement = soapMessage.getHeaderElement(CoordinationConstants.WSCOOR_NAMESPACE,
                        CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT) ;
                if (soapHeaderElement != null)
                {
                    final CoordinationContextType cc = CoordinationContextHelper.deserialise(soapHeaderElement) ;
                    final String coordinationType = cc.getCoordinationType().getValue() ;
                    if (Protocols.AtomicTransaction.equals(coordinationType))
                    {
                        final TxContext txContext = new com.arjuna.mwlabs.wst.at.context.TxContextImple(cc) ;
                        TransactionManagerFactory.transactionManager().resume(txContext) ;
                    }
                    else if (Protocols.BusinessActivityAtomic.equals(coordinationType))
                    {
                        final TxContext txContext = new com.arjuna.mwlabs.wst.ba.context.TxContextImple(cc);
                        BusinessActivityManagerFactory.businessActivityManager().resume(txContext) ;
                    }
                    else
                    {
        			    wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.client.GlueICI_1",
    			            new Object[]{"com.arjuna.mw.wst.client.GlueIncomingContextInterceptor.resumeTransaction()"});

            		    wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.client.GlueICI_2",
        		            new Object[]{coordinationType});
                    }
                }
            }
            catch (final Throwable th) 
    	    {
        		wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.client.GlueICI_1",
				    new Object[]{"com.arjuna.mw.wst.client.GlueIncomingContextInterceptor.resumeTransaction()"});

            	wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.client.GlueICI_3",
                    new Object[]{th});
            }
        }
    }
}
