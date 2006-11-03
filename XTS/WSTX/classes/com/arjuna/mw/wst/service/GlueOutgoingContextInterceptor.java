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
/* * Copyright (C) 2005, * * Arjuna Technologies Limited, * Newcastle upon Tyne, * Tyne and Wear, * UK. * * $Id$ */package com.arjuna.mw.wst.service;import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.BusinessActivityManagerFactory;
import com.arjuna.mw.wst.TransactionManager;
import com.arjuna.mw.wst.TransactionManagerFactory;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsba.BusinessActivityConstants;

import electric.soap.ISOAPInterceptor;
/** * The class is used to perform WS-Transaction context insertion * and extraction for application level SOAP messages within WebMethods Glue. * * @message com.arjuna.mw.wst.service.GlueOCI_1 [com.arjuna.mw.wst.service.GlueOCI_1] - Error in:  * @message com.arjuna.mw.wst.service.GlueOCI_2 [com.arjuna.mw.wst.service.GlueOCI_2] - Stack trace:  */public class GlueOutgoingContextInterceptor implements ISOAPInterceptor{    public void intercept( electric.soap.SOAPMessage soapMessage, electric.util.Context messageContext )    {        try        {            final TransactionManager transactionManager = TransactionManagerFactory.transactionManager() ;            final com.arjuna.mwlabs.wst.at.context.TxContextImple atTXContext =                (com.arjuna.mwlabs.wst.at.context.TxContextImple)messageContext.getProperty(AtomicTransactionConstants.WSAT_PROTOCOL) ;            if (atTXContext != null)            {                transactionManager.resume(atTXContext) ;            }            else            {                transactionManager.suspend() ;            }            final BusinessActivityManager businessActivityManager = BusinessActivityManagerFactory.businessActivityManager() ;            final com.arjuna.mwlabs.wst.ba.context.TxContextImple baTXContext =                (com.arjuna.mwlabs.wst.ba.context.TxContextImple)messageContext.getProperty(BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME) ;            if (baTXContext != null)            {                businessActivityManager.resume(baTXContext) ;            }            else            {                businessActivityManager.suspend() ;            }        }        catch (final Throwable th)         {    	    wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.service.GlueOCI_1",                new Object[]{"com.arjuna.mw.wst.service.GlueOutgoingContextInterceptor.intercept()"});    	    wstxLogger.arjLoggerI18N.warn("com.arjuna.mw.wst.service.GlueOCI_2",                new Object[]{th});        }    }    }
