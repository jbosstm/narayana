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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: InterpositionClientRequestInterceptorImpl.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.orbspecific.hporb.interceptors.interposition;

import com.arjuna.common.util.propertyservice.PropertyManager;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.jts.*;
import com.arjuna.ats.jts.common.Environment;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.common.Defaults;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.OTSImpleManager;

import com.arjuna.common.util.logging.*;

import com.arjuna.orbportability.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.*;
import org.omg.PortableInterceptor.*; 
import org.omg.PortableInterceptor.ORBInitInfoPackage.*; 
import org.omg.IOP.*;
import org.omg.IOP.CodecFactoryPackage.*;

import java.util.Hashtable;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CosTransactions.Unavailable;

/**
 * PortableInterceptor::ClientRequestInterceptor implementation which adds a 
 * service context carrying the transaction context.
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

class InterpositionClientRequestInterceptorImpl extends LocalObject implements ClientRequestInterceptor
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

public InterpositionClientRequestInterceptorImpl (int localSlot, Codec codec)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionClientRequestInterceptorImpl ( "+localSlot+" )");
	}

	_localSlot = localSlot;
	_codec = codec;
    }

public String name ()
    {
	return "OTS_Interposition";
    }

public void destroy ()
    {
    }
    
public void send_request (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionClientRequestInterceptorImpl::send_request ( "+request_info.operation()+" )");
	}

	if (systemCall(request_info))
	    return;

	try
	{
	    if (!InterpositionClientRequestInterceptorImpl.otsAlwaysPropagate)
	    {
		TransactionalObject ptr = org.omg.CosTransactions.TransactionalObjectHelper.narrow(request_info.target());
	    
		if (ptr == null)
		    throw new BAD_PARAM();
	    }

	    try
	    {
		/*
		 * We get back an Any, which contains a key which we must
		 * now use to get the actual transaction context. This saves
		 * use having to pack and unpack the context every time it
		 * changes, even if we don't then make a remote invocation.
		 */

		org.omg.CORBA.Any localData = request_info.get_slot(_localSlot);
		String threadId = null;
		boolean problem = false;
		org.omg.CORBA.Any data = null;

		/*
		 * If we are using co-location optimisations, then
		 * filters may not have been used to set up the
		 * thread-to-context association. So, if the PI slot
		 * is null, check whether the current thread has a context
		 * on it already. If so, use it.
		 */

		if (localData.type().kind().value() != TCKind._tk_null)
		{
		    if ((threadId = localData.extract_string()) == null)
			throw new UNKNOWN("Invalid PI tx parameter!");
		}
		else
		    threadId = Integer.toHexString(System.identityHashCode(Thread.currentThread())) ;
		
		if (threadId != null)
		{
		    ControlWrapper theControl = OTSImpleManager.current().contextManager().current(threadId);

		    if (theControl != null)
		    {
			try
			{
			    Coordinator theCoordinator = theControl.get_coordinator();
			    PropagationContext ctx = null;
				
			    if (theCoordinator != null)
			    {
				ctx = theCoordinator.get_txcontext();

				data = packPropagationContext(ctx);
				
				theCoordinator = null;
			    }
			    else
				throw new Unavailable();
			}
			catch (Unavailable ex)
			{
			    /*
			     * We may have to make calls during
			     * commit (e.g., after_completion)
			     * which are valid, but which will get
			     * Unavailable.
			     */

			    problem = true;
			}
		    }
		    else
			problem = true;
		}
		else
		    problem = true;

		if (problem)
		{
		    /*
		     * Only throw an exception if we have no transaction
		     * context and we require one.
		     */
	    
		    if (otsNeedTranContext)
			throw new TRANSACTION_REQUIRED();
		}
		    
		if (data != null)
		{
		    byte[] octets = _codec.encode_value(data);
							 
		    ServiceContext service_context = new ServiceContext(OTSManager.serviceId, octets);

		    request_info.add_request_service_context(service_context, true);
		}
	    }
	    catch (SystemException e)
	    {
		throw e;
	    }
	    catch (Exception ex)
	    {
		throw new UNKNOWN(ex.toString());
	    }
	}
	catch (BAD_PARAM ex)
	{
	    // narrow failed, so not a transactional object.
	}
    }

public void send_poll (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionClientRequestInterceptorImpl::send_poll ( "+request_info.operation()+" )");
	}
    }

public void receive_reply (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionClientRequestInterceptorImpl::receive_reply ( "+request_info.operation()+" )");
	}
    }

public void receive_exception (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionClientRequestInterceptorImpl::receive_exception ( "+request_info.operation()+" )");
	}

	// mark transaction as rollback only if a system exception
    }

public void receive_other (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionClientRequestInterceptorImpl::receive_other ( "+request_info.operation()+" )");
	}
    }

private final org.omg.CORBA.Any packPropagationContext (PropagationContext ctx)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionClientRequestInterceptorImpl.packPropagationContext ( "+ctx+" )");
	}

	org.omg.CORBA.Any data = ORBManager.getORB().orb().create_any();

	if (ctx != null)
	    PropagationContextHelper.insert(data, ctx);

	return data;
    }

private final boolean systemCall (ClientRequestInfo request_info)
    {
	if (request_info.operation().equals("_is_a"))
	    return true;
	else
	    return false;
    }

private int   _localSlot;
private Codec _codec;

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

