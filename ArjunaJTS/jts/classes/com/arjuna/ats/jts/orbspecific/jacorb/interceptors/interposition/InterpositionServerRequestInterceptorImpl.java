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
 * $Id: InterpositionServerRequestInterceptorImpl.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.orbspecific.jacorb.interceptors.interposition;

import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CosTransactions.PropagationContextHelper;
import org.omg.CosTransactions.TransactionalObjectHelper;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.ats.jts.common.InterceptorInfo;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

/**
 * PortableInterceptor::ServerRequestInterceptor implementation which checks
 * that a transaction context was received.
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
	if (jtsLogger.logger.isTraceEnabled())
	{
	    jtsLogger.logger.trace("InterpositionServerRequestInterceptorImpl ( "+dataSlot+" )");
	}

	_dataSlot = dataSlot;
	_codec = codec;
    }

public String name ()
    {
	return "OTS_Interposition";
    }

    private void trace_request(String method, ServerRequestInfo request_info) {
        jtsLogger.logger.tracef("InterpositionServerRequestInterceptorImpl::%s ( %s ) nodeId=%s requestId=%d target=%s",
                method, request_info.operation(),
                arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifier(),
                request_info.request_id(), ((ServerRequestInfoImpl) request_info).target().toString());
    }

public void receive_request_service_contexts (ServerRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isTraceEnabled())
	{
        trace_request("receive_request_service_contexts", request_info);
	}

	try
	{
	    try
	    {
		if (!InterceptorInfo.getAlwaysPropagate())
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

		    if (InterceptorInfo.getNeedTranContext())
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
	if (jtsLogger.logger.isTraceEnabled())
	{
	    trace_request("receive_request", request_info);
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

	if (jtsLogger.logger.isTraceEnabled())
	{
        trace_request("send_reply", request_info);
	}

	try
	{
	    suspendContext(request_info);
	}
	catch (SystemException ex)
	{
        jtsLogger.i18NLogger.warn_orbspecific_jacorb_interceptors_interposition_srie(
                "InterpositionServerRequestInterceptorImpl::send_reply", ex);
	    
	    throw ex;
	}
	catch (Exception e) {
        jtsLogger.i18NLogger.warn_orbspecific_jacorb_interceptors_interposition_srie("InterpositionServerRequestInterceptorImpl::send_reply", e);

        throw new BAD_OPERATION();
    }
    }

public void send_exception (ServerRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isTraceEnabled())
	{
        trace_request("send_exception", request_info);
	}

	try
	{
	    suspendContext(request_info);
	}
	catch (SystemException ex) {
        jtsLogger.i18NLogger.warn_orbspecific_jacorb_interceptors_interposition_srie("InterpositionServerRequestInterceptorImpl::send_exception", ex);

        throw ex;
    }
	catch (Exception e) {
        jtsLogger.i18NLogger.warn_orbspecific_jacorb_interceptors_interposition_srie("InterpositionServerRequestInterceptorImpl::send_exception", e);

        throw new BAD_OPERATION();
    }
    }

public void send_other (ServerRequestInfo request_info) throws SystemException
    {
	if (jtsLogger.logger.isTraceEnabled())
	{
        trace_request("send_other", request_info);
	}

	try
	{
	    suspendContext(request_info);
	}
	catch (SystemException ex) {
        jtsLogger.i18NLogger.warn_orbspecific_jacorb_interceptors_interposition_srie("InterpositionServerRequestInterceptorImpl::send_other", ex);

        throw ex;
    }
	catch (Exception e) {
        jtsLogger.i18NLogger.warn_orbspecific_jacorb_interceptors_interposition_srie("InterpositionServerRequestInterceptorImpl::send_other", e);

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
	if (jtsLogger.logger.isTraceEnabled())
	{
        trace_request("suspendContext", request_info);
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
}
