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
 * Copyright (C) 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: InterpositionServerRequestInterceptorImpl.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.orbspecific.jacorb.interceptors.interposition;

import com.arjuna.ats.jts.*;
import com.arjuna.ats.jts.common.Environment;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.common.Defaults;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.OTSImpleManager;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.*;
import org.omg.PortableInterceptor.*; 
import org.omg.IOP.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.TRANSACTION_REQUIRED;

/**
 * PortableInterceptor::ServerRequestInterceptor implementation which checks 
 * that a transaction context was received.
 *
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.interceptors.interposition.srie {0} caught an unexpected exception: {1}
 */

/*
 * Only add and remove transactional context if the
 * destination object is inherited from TransactionalObject.
 *
 * When we have finished implementing fully checked transactions
 * we will use the filter to prevent remote access to the factory
 * and the terminator.
 */

/*
 * If we're not within a transaction when we call a TransactionalObject
 * the spec. implies that we should raise a TransactionExpected exception.
 * This is too restrictive, and removed the ability to build modular
 * applications. Therefore, by default we will *not* do this, but this
 * can be overridden with an appropriate environment variable. (Could do
 * this through a separate base class.)
 */

class InterpositionServerRequestInterceptorImpl extends LocalObject implements ServerRequestInterceptor
{
    /*
     * Only the transaction creator can terminate the transaction. So don't
     * propagate the terminator.
     */

    /*
     * Propagate/expect context if:
     *
     * (i) we are a TransactionalObject.
     * (ii) we define otsAlwaysPropagate to TRUE.
     *
     * Throw an exception at the client/server side if there is no context if:
     *
     * (i) we define otsNeedTranContext to FALSE.
     */

public InterpositionServerRequestInterceptorImpl (int dataSlot, Codec codec)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionServerRequestInterceptorImpl ( "+dataSlot+" )");
	}

	_dataSlot = dataSlot;
	_codec = codec;
    }

public String name ()
    {
	return "OTS_Interposition";
    }

public void receive_request_service_contexts (ServerRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionServerRequestInterceptorImpl::receive_request_service_contexts ( "+request_info.operation()+" )");
	}
	
	try
	{
	    try
	    {
		if (!InterpositionServerRequestInterceptorImpl.otsAlwaysPropagate)
		{
		    if (!request_info.target_is_a(TransactionalObjectHelper.id()))
			throw new BAD_PARAM();
		}
	    }
	    catch (Exception ex)
	    {
		// just in case the object isn't in the IR.
	    }
	    
	    /*
	     * OK, we may be transactional and expect some context information,
	     * even if it zero.
	     */

	    try
	    {
		ServiceContext serviceContext = null;
		
		try
		{
		    serviceContext = request_info.get_request_service_context(OTSManager.serviceId);
		}
		catch (BAD_PARAM bp)
		{
		    // no context, so nothing shipped!

		    serviceContext = null;
		}

		if (serviceContext != null)
		{
		    Any receivedData = _codec.decode_value(serviceContext.context_data, PropagationContextHelper.type());

		    /*
		     * Set the slot information for the "current" thread. When
		     * the real invocation thread actually needs to get its
		     * transaction context it must check this slot (if it does
		     * not have a transaction context already) and then do
		     * a resume.
		     */

		    request_info.set_slot(_dataSlot, receivedData);
		}
		else
		{
		    /*
		     * Only throw an exception if we have no transaction
		     * context and we require one.
		     */
	    
		    if (otsNeedTranContext)
			throw new TRANSACTION_REQUIRED();
		}
	    }
	    catch (TRANSACTION_REQUIRED ex)
	    {
		throw ex;
	    }
	    catch (Exception e)
	    {
	    }
	}
	catch (BAD_PARAM ex)
	{
	}
    }

public void receive_request (ServerRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionServerRequestInterceptorImpl.receive_request ( "+request_info.operation()+" )");
	}
    }

public void send_reply (ServerRequestInfo request_info) throws SystemException
    {
	/*
	 * We could send the propagation context back to the client. Any
	 * reason?
	 * Yes, so that we can do low-cost abort and registration.
	 *
	 * //    PropagationContext* ctx = theCoordinator->get_txcontext();
	 */

	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionServerRequestInterceptorImpl::send_reply ( "+request_info.operation()+" )");
	}

	try
	{
	    suspendContext(request_info);
	}
	catch (SystemException ex)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.interceptors.interposition.srie",
					  new java.lang.Object[] {"InterpositionServerRequestInterceptorImpl::send_reply", ex} );
	    }

	    ex.printStackTrace();
	    
	    throw ex;
	}
	catch (Exception e)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.interceptors.interposition.srie",
					  new java.lang.Object[] {"InterpositionServerRequestInterceptorImpl::send_reply", e} );
	    }

	    throw new BAD_OPERATION();
	}
    }

public void send_exception (ServerRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionServerRequestInterceptorImpl::send_exception ( "+request_info.operation()+" )");
	}

	try
	{
	    suspendContext(request_info);
	}
	catch (SystemException ex)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.interceptors.interposition.srie",
					  new java.lang.Object[] {"InterpositionServerRequestInterceptorImpl::send_exception", ex} );
	    }

	    throw ex;
	}
	catch (Exception e)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.interceptors.interposition.srie",
					  new java.lang.Object[] {"InterpositionServerRequestInterceptorImpl::send_exception", e} );
	    }

	    throw new BAD_OPERATION();
	}
    }

public void send_other (ServerRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionServerRequestInterceptorImpl::send_other ( "+request_info.operation()+" )");
	}

	try
	{
	    suspendContext(request_info);
	}
	catch (SystemException ex)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.interceptors.interposition.srie",
					  new java.lang.Object[] {"InterpositionServerRequestInterceptorImpl::send_other", ex} );
	    }

	    throw ex;
	}
	catch (Exception e)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.interceptors.interposition.srie",
					  new java.lang.Object[] {"InterpositionServerRequestInterceptorImpl::send_other", e} );
	    }

	    throw new BAD_OPERATION();
	}
    }

    /*
     * If there is a thread id associated with PICurrent then it will
     * have been placed there by a server-side thread which executed in
     * the application object and needed to associate an imported
     * transaction with itself. In which case we need to do the
     * equivalent of a suspend to remove the thread from Current and
     * from the current transaction.
     */

private void suspendContext (ServerRequestInfo request_info) throws SystemException, InvalidSlot
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionServerRequestInterceptorImpl.suspendContext ( "+request_info.operation()+" )");
	}

	Any data = request_info.get_slot(_dataSlot);

	if ((data != null) && (data.type().kind().value() != TCKind._tk_null))
	{
	    String threadId = null;

	    try
	    {
		if ((threadId = data.extract_string()) != null)
		{
		    //		    ControlWrapper ctx = OTSImpleManager.systemCurrent().contextManager().popAction(threadId);
		    ControlWrapper ctx = OTSImpleManager.current().contextManager().popAction(threadId);
		
		    //		    OTSImpleManager.systemCurrent().contextManager().purgeActions(threadId);

		    OTSImpleManager.current().contextManager().purgeActions(threadId);
		}
	    }
	    catch (BAD_OPERATION bex)
	    {
		// not a string, so still a pgcts
	    }
	    
	    request_info.set_slot(_dataSlot, null);
	}
    }

public void destroy()
	{
		// Do nothing
	}

private Codec _codec;
private int   _dataSlot;

private static boolean otsNeedTranContext = Defaults.needTransactionContext;
private static boolean otsAlwaysPropagate = Defaults.alwaysPropagateContext;
private static boolean otsHaveChecked = false;
    
    static
    {
	if (!otsHaveChecked)
	{
	    String env = jtsPropertyManager.propertyManager.getProperty(Environment.NEED_TRAN_CONTEXT, null);

	    if (env != null)
	    {
		if (env.compareTo("YES") == 0)
		    otsNeedTranContext = true;
	    }

	    env = jtsPropertyManager.propertyManager.getProperty(Environment.ALWAYS_PROPAGATE_CONTEXT, null);

	    if (env != null)
	    {
		if (env.compareTo("YES") == 0)
		    otsAlwaysPropagate = true;
	    }
	    
	    otsHaveChecked = true;
	}
    }

}
