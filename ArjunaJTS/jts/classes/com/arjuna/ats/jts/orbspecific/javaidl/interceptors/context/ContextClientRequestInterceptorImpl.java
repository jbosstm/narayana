/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.orbspecific.javaidl.interceptors.context;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.LocalObject;
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

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.utils.ThreadUtil;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.ats.jts.common.InterceptorInfo;
import com.arjuna.ats.jts.logging.jtsLogger;

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

	/**
	 * Provides an opportunity to destroy this interceptor.
	 * The destroy method is called during <code>ORB.destroy</code>. When an
	 * application calls <code>ORB.destroy</code>, the ORB:
	 * <ol>
	 *   <li>waits for all requests in progress to complete</li>
	 *   <li>calls the <code>Interceptor.destroy</code> operation for each
	 *       interceptor</li>
	 *   <li>completes destruction of the ORB</li>
	 * </ol>
	 * Method invocations from within <code>Interceptor.destroy</code> on
	 * object references for objects implemented on the ORB being destroyed
	 * result in undefined behavior. However, method invocations on objects
	 * implemented on an ORB other than the one being destroyed are
	 * permitted. (This means that the ORB being destroyed is still capable
	 * of acting as a client, but not as a server.)
	 */
	public void destroy()
	{
	}
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
	if (jtsLogger.logger.isTraceEnabled())
	{
	    jtsLogger.logger.trace("ContextClientRequestInterceptorImpl ( "+localSlot+" )");
	}

	_localSlot = localSlot;
	_codec = codec;
    }

public String name ()
    {
	return "OTS_Context";
    }

public void send_request (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isTraceEnabled())
	{
	    jtsLogger.logger.trace("ContextClientRequestInterceptorImpl::send_request ( "+request_info+" )");
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

		if (localData.type().kind().value() != TCKind._tk_null)
		{
		    if ((threadId = localData.extract_string()) == null)
			throw new UNKNOWN(jtsLogger.i18NLogger.get_orbspecific_javaidl_interceptors_context_invalidparam());
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
	    catch (Exception ex) {
			if (arjPropertyManager.getCoreEnvironmentBean().isLogAndRethrow()) {
				jtsLogger.i18NLogger.warn_orbspecific_javaidl_interceptors_context_error(ex); // JBTM-3990
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
    }

public void send_poll (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isTraceEnabled())
	{
	    jtsLogger.logger.trace("ContextClientRequestInterceptorImpl::send_poll ( "+request_info+" )");
	}
    }

public void receive_reply (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isTraceEnabled())
	{
	    jtsLogger.logger.trace("ContextClientRequestInterceptorImpl::receive_reply ( "+request_info+" )");
	}
    }

public void receive_exception (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isTraceEnabled())
	{
	    jtsLogger.logger.trace("ContextClientRequestInterceptorImpl::receive_exception ( "+request_info+" )");
	}

	// mark transaction as rollback only if a system exception
    }

public void receive_other (ClientRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isTraceEnabled())
	{
	    jtsLogger.logger.trace("ContextClientRequestInterceptorImpl::receive_other ( "+request_info+" )");
	}
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
private ThreadLocal _inUse = new ThreadLocal();
}
