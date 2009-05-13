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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: InterpositionORBInitializerImpl.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.orbspecific.hporb.interceptors.interposition;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.exceptions.FatalError;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import com.arjuna.ats.jts.*;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.orbportability.*;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.*;
import org.omg.PortableInterceptor.*;
import org.omg.PortableInterceptor.ORBInitInfoPackage.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecFactoryPackage.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CosTransactions.Unavailable;

public class InterpositionORBInitializerImpl extends LocalObject implements ORBInitializer
{

public InterpositionORBInitializerImpl ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionORBInitializerImpl ()");
	}

	/*
	 * Register the thread-setup object so that ArjunaCore can be
	 * used raw.
	 */

	ThreadActionData.addSetup(new InterpositionThreadSetup());
    }

public void pre_init (ORBInitInfo init_info)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionORBInitializerImpl.pre_init ()");
	}

	// nothing to do
    }

    /**
     * @message com.arjuna.ats.internal.jts.orbspecific.hporb.interceptors.interposition.codecerror {0} - a failure occured when getting {1} codec - unknown encoding.
     * @message com.arjuna.ats.internal.jts.orbspecific.hporb.interceptors.interposition.duplicatename {0} - duplicate interceptor name for {1} when registering
     * @message com.arjuna.ats.internal.jts.orbspecific.hporb.interceptors.interposition.codeccreate Cannot create a codec of the required encoding.
     * @message com.arjuna.ats.internal.jts.orbspecific.hporb.interceptors.interposition.sie A server-side request interceptor already exists with that name.
     */

public void post_init (ORBInitInfo init_info)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionORBInitializer.post_init ()");
	}

	/*
	 * These value should be part of the standard.
	 */

	int localSlot = init_info.allocate_slot_id();
	int receivedSlot = init_info.allocate_slot_id();

	com.arjuna.ats.jts.OTSManager.setLocalSlotId(localSlot);
	com.arjuna.ats.jts.OTSManager.setReceivedSlotId(receivedSlot);

	/*
	 * Get the CDR codec; used for encoding/decoding the service
	 * context and IOR components.
	 */

	Codec cdr_codec = null;

	try
	{
	    if (jtsLogger.logger.isDebugEnabled())
	    {
		jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						   (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionORBInitializerImpl - getting reference to ENCODING_CDR_ENCAPS codec");
	    }

	    Encoding cdr_encoding = new Encoding(ENCODING_CDR_ENCAPS.value, (byte)1, (byte)2);

	    cdr_codec = init_info.codec_factory().create_codec(cdr_encoding);
	}
	catch (UnknownEncoding ex)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.hporb.interceptors.interposition.codecerror",
					  new java.lang.Object[] { "InterpositionORBInitializerImpl", "ENCODING_CDR_ENCAPS" }, ex);
	    }

	    throw new FatalError(jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.hporb.interceptors.interposition.codeccreate"), ex);
	}

	/*
	 * Register client interceptor to propogate the context.
	 */

	try
	{
	    if (jtsLogger.logger.isDebugEnabled())
	    {
		jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						   (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionORBInitializerImpl - registering ClientRequestInterceptor");
	    }

	    ClientRequestInterceptor client_interceptor = new InterpositionClientRequestInterceptorImpl(localSlot, cdr_codec);

	    init_info.add_client_request_interceptor(client_interceptor);
	}
	catch (DuplicateName ex)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.hporb.interceptors.interposition.duplicatename",
					  new java.lang.Object[] { "InterpositionORBInitializerImpl", "ClientRequestInterceptor" }, ex);
	    }

	    throw new FatalError(jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.hporb.interceptors.interposition.cie"), ex);
	}

	/*
	 * Register a server interceptor to receive the context.
	 */

	try
	{
	    if (jtsLogger.logger.isDebugEnabled())
	    {
		jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						   (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.jts.logging.FacilityCode.FAC_INTERCEPTOR), "InterpositionORBInitializerImpl - registering ServerRequestInterceptor");
	    }

	    ServerRequestInterceptor server_interceptor = new InterpositionServerRequestInterceptorImpl(receivedSlot, cdr_codec);

	    init_info.add_server_request_interceptor(server_interceptor);
	}
	catch (DuplicateName ex)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.hporb.interceptors.interposition.duplicatename",
					  new java.lang.Object[] { "InterpositionORBInitializerImpl", "ServerRequestInterceptor" }, ex);
	    }

	    throw new FatalError(jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.hporb.interceptors.interposition.sie"), ex);
	}
    }

}
