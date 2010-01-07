/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * $Id: ContextClientRequestInterceptorImpl.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.orbspecific.jacorb.interceptors.context;

import com.arjuna.ats.arjuna.utils.ThreadUtil;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.ats.jts.common.InterceptorInfo;
import com.arjuna.ats.jts.logging.FacilityCode;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.common.util.logging.DebugLevel;
import com.arjuna.common.util.logging.VisibilityLevel;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.TransactionalObject;
import org.omg.CosTransactions.TransactionalObjectHelper;
import org.omg.CosTransactions.Unavailable;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;

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

class ContextClientRequestInterceptorImpl extends LocalObject implements ClientRequestInterceptor
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

public ContextClientRequestInterceptorImpl (int localSlot, Codec codec)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				   (FacilityCode.FAC_OTS | FacilityCode.FAC_INTERCEPTOR), "ContextClientRequestInterceptorImpl ( "+localSlot+" )");
	}

	_localSlot = localSlot;
	_codec = codec;
    }

public String name ()
    {
	return "OTS_Context";
    }

    /**
     * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.interceptors.context.error Context interceptor caught an unexpected exception:
     * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.interceptors.context.invalidparam Invalid portable interceptor transaction parameter!
     */

public void send_request (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				   (FacilityCode.FAC_OTS | FacilityCode.FAC_INTERCEPTOR), "ContextClientRequestInterceptorImpl::send_request ( "+request_info+" )");
	}

	if (systemCall(request_info))
	    return;

	final boolean otsAlwaysPropagate = InterceptorInfo.getAlwaysPropagate() ;
	try
	{
	    if (!otsAlwaysPropagate)
	    {
		TransactionalObject ptr = TransactionalObjectHelper.narrow(request_info.target());

		if (ptr == null)
		    throw new BAD_PARAM();
	    }
	    else
	    {
		/** If we are set to always propagate then ensure we're not already in use **/
	        /** If the value is not null then we are currently in use **/
                if ( _inUse.get() != null )
                {
            	    return;
                }
                else
                {
            	    _inUse.set(_inUse);
                }
	    }

	    try
	    {
		/*
		 * We get back an Any, which contains a key which we must
		 * now use to get the actual transaction context. This
		 * saves use having to pack and unpack the context every
		 * time it changes, even if we don't then make a remote
		 * invocation.
		 */

		Any localData = request_info.get_slot(_localSlot);
		String threadId = null;
		boolean problem = false;
		String stringRef = null;

		/*
		 * If we are using co-location optimisations, then
		 * filters may not have been used to set up the
		 * thread-to-context association. So, if the PI slot
		 * is null, check whether the current thread has a context
		 * on it already. If so, use it.
		 */

		if ( (localData != null) && (localData.type().kind().value() != TCKind._tk_null) )
		{
		    if ( (threadId = localData.extract_string()) == null )
			throw new UNKNOWN(jtsLogger.loggerI18N.getString("com.arjuna.ats.internal.jts.orbspecific.jacorb.interceptors.context.invalidparam"));
		}
		else
		    threadId = ThreadUtil.getThreadId() ;

		if (threadId != null)
		{
		    ControlWrapper theControl = OTSImpleManager.current().contextManager().current(threadId);

		    if (theControl != null)
		    {
			try
			{
			    Coordinator theCoordinator = theControl.get_coordinator();

			    if (theCoordinator != null)
			    {
				stringRef = ORBManager.getORB().orb().object_to_string(theCoordinator);
			    }
			    else
			    {
				problem = true;
			    }
			}
			catch (Unavailable e)
			{
			    problem = true;
			}

			theControl = null;
		    }
		    else
			problem = true;
		}
		else
		    problem = true;

		if (problem)
		{
		    /*
		     * Only throw an exception if we have no
		     * transaction context and we require one.
		     */

		    if (InterceptorInfo.getNeedTranContext())
			throw new TRANSACTION_REQUIRED();
		    else
			stringRef = null;
		}

		if (stringRef != null)
		{
		    Any data = ORBManager.getORB().orb().create_any();

		    data.insert_string(stringRef);

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
		if (jtsLogger.loggerI18N.isWarnEnabled())
		{
		    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.interceptors.context.error", ex);
		}

		throw new UNKNOWN(ex.toString());
	    }
            finally
            {
                /** If we are set to always propagate then ensure we clear the inuse flag **/
                if (otsAlwaysPropagate)
                {
                        _inUse.set(null);
                }
            }
	}
	catch (BAD_PARAM ex)
	{
	    // narrow failed, so not a transactional object.
	}
	catch (OBJECT_NOT_EXIST ex)
	{
	}
    }

public void send_poll (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (FacilityCode.FAC_OTS | FacilityCode.FAC_INTERCEPTOR), "ContextClientRequestInterceptorImpl::send_poll ( "+request_info+" )");
	}
    }

public void receive_reply (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (FacilityCode.FAC_OTS | FacilityCode.FAC_INTERCEPTOR), "ContextClientRequestInterceptorImpl::receive_reply ( "+request_info+" )");
	}
    }

public void receive_exception (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (FacilityCode.FAC_OTS | FacilityCode.FAC_INTERCEPTOR), "ContextClientRequestInterceptorImpl::receive_exception ( "+request_info+" )");
	}

	// mark transaction as rollback only if a system exception
    }

public void receive_other (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (FacilityCode.FAC_OTS | FacilityCode.FAC_INTERCEPTOR), "ContextClientRequestInterceptorImpl::receive_other ( "+request_info+" )");
	}
    }

private final boolean systemCall (ClientRequestInfo request_info)
    {
	return ("_is_a".equals(request_info.operation())) ;
    }

	public void destroy()
	{
		// Do nothing
	}

private int   _localSlot;
private Codec _codec;
private ThreadLocal _inUse = new ThreadLocal();
}

