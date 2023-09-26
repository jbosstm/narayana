/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.orbspecific.javaidl.interceptors.interposition;

import org.omg.CORBA.LocalObject;
import org.omg.IOP.Codec;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.ats.jts.logging.jtsLogger;

public class InterpositionORBInitializerImpl extends LocalObject implements ORBInitializer
{

public InterpositionORBInitializerImpl ()
    {
	if (jtsLogger.logger.isTraceEnabled())
	{
	    jtsLogger.logger.trace("InterpositionORBInitializerImpl ()");
	}

	/*
	 * Register the thread-setup object so that ArjunaCore can be
	 * used raw.
	 */

	ThreadActionData.addSetup(new InterpositionThreadSetup());
    }


public void pre_init (ORBInitInfo init_info)
    {
	if (jtsLogger.logger.isTraceEnabled())
	{
	    jtsLogger.logger.trace("InterpositionORBInitializer.pre_init ()");
	}

	/*
	 * These value should be part of the standard.
	 */

	int localSlot = init_info.allocate_slot_id();
	int receivedSlot = init_info.allocate_slot_id();

	OTSManager.setLocalSlotId(localSlot);
	OTSManager.setReceivedSlotId(receivedSlot);

	/*
	 * Get the CDR codec; used for encoding/decoding the service
	 * context and IOR components.
	 */

	Codec cdr_codec = null;

	try
	{
	    if (jtsLogger.logger.isTraceEnabled())
	    {
		jtsLogger.logger.trace("InterpositionORBInitializerImpl - getting reference to ENCODING_CDR_ENCAPS codec");
	    }

	    Encoding cdr_encoding = new Encoding(ENCODING_CDR_ENCAPS.value, (byte)1, (byte)2);

	    cdr_codec = init_info.codec_factory().create_codec(cdr_encoding);
	}
	catch (UnknownEncoding ex)
	{
        jtsLogger.i18NLogger.warn_orbspecific_javaidl_interceptors_interposition_codecerror(
                "InterpositionORBInitializerImpl", "ENCODING_CDR_ENCAPS", ex);

	    throw new FatalError(jtsLogger.i18NLogger.get_orbspecific_javaidl_interceptors_interposition_codeccreate(), ex);
	}

	/*
	 * Register client interceptor to propogate the context.
	 */

	try
	{
	    if (jtsLogger.logger.isTraceEnabled())
	    {
		jtsLogger.logger.trace("InterpositionORBInitializerImpl - registering ClientRequestInterceptor");
	    }

	    ClientRequestInterceptor client_interceptor = new InterpositionClientRequestInterceptorImpl(localSlot, cdr_codec);

	    init_info.add_client_request_interceptor(client_interceptor);
	}
	catch (DuplicateName ex)
	{
        jtsLogger.i18NLogger.warn_orbspecific_javaidl_interceptors_interposition_duplicatename(
                 "InterpositionORBInitializerImpl", "ClientRequestInterceptor", ex);

	    throw new FatalError(jtsLogger.i18NLogger.get_orbspecific_javaidl_interceptors_interposition_cie(), ex);
	}

	/*
	 * Register a server interceptor to receive the context.
	 */

	try
	{
	    if (jtsLogger.logger.isTraceEnabled())
	    {
		jtsLogger.logger.trace("InterpositionORBInitializerImpl - registering ServerRequestInterceptor");
	    }

	    ServerRequestInterceptor server_interceptor = new InterpositionServerRequestInterceptorImpl(receivedSlot, cdr_codec);

	    init_info.add_server_request_interceptor(server_interceptor);
	}
	catch (DuplicateName ex)
	{
        jtsLogger.i18NLogger.warn_orbspecific_javaidl_interceptors_interposition_duplicatename(
                "InterpositionORBInitializerImpl", "ServerRequestInterceptor", ex);

	    throw new FatalError(jtsLogger.i18NLogger.get_orbspecific_javaidl_interceptors_interposition_sie(), ex);
	}
    }

public void post_init (ORBInitInfo init_info)
    {
	if (jtsLogger.logger.isTraceEnabled())
	{
	    jtsLogger.logger.trace("InterpositionORBInitializerImpl.post_init ()");
	}

	// nothing to do
    }

}