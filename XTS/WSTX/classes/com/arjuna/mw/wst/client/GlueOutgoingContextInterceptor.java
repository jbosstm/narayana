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

import com.arjuna.mw.wsc.context.Context;
import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.BusinessActivityManagerFactory;
import com.arjuna.mw.wst.TransactionManager;
import com.arjuna.mw.wst.TransactionManagerFactory;
import com.arjuna.mw.wst.common.CoordinationContextHelper;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;

import electric.soap.ISOAPInterceptor;
import electric.xml.Element;

/**
 * The class is used to perform WS-Transaction context insertion
 * and extraction for application level SOAP messages within WebMethods Glue.
 */
public class GlueOutgoingContextInterceptor implements ISOAPInterceptor
{
    /**
     * @message com.arjuna.mw.wst.client.GlueOCI_1 [com.arjuna.mw.wst.client.GlueOCI_1] - Error in: 
     * @message com.arjuna.mw.wst.client.GlueOCI_2 [com.arjuna.mw.wst.client.GlueOCI_2] - Stack trace: 
     */

    public void intercept( electric.soap.SOAPMessage soapMessage, electric.util.Context messageContext )
    {
    	if (soapMessage == null)
    	{
    	    return;
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
        		Element headElement = new Element(CoordinationConstants.WSCOOR_PREFIX, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT, CoordinationConstants.WSCOOR_NAMESPACE);
        		headElement.setNamespace(CoordinationConstants.WSCOOR_PREFIX, CoordinationConstants.WSCOOR_NAMESPACE);
                soapMessage.addHeaderElement(headElement) ;
        		CoordinationContextHelper.serialise(headElement, coordinationContext) ;
            }
        }
        catch (final Throwable th) 
        {
            wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.client.GlueOCI_1",
                new Object[]{"com.arjuna.mw.wst.client.GlueOutgoingContextInterceptor.intercept()"});

    	    wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.client.GlueOCI_2",
                new Object[]{th});
        }

        return;
    }    
}
