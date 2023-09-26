/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.context;

import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.orbportability.ORBInfo;
import com.arjuna.orbportability.ORBType;

/**
 * Registers the appropriate filter with the ORB.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: ContextPropagationManager.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public class ContextPropagationManager
{
    public ContextPropagationManager ()
    {
	String contextMode = jtsPropertyManager.getJTSEnvironmentBean().getContextPropMode();
	boolean interposition = true;

	if (contextMode != null)
	{
	    if (contextMode.equals("CONTEXT"))
		interposition = false;
	    else
	    {
		if (contextMode.equals("NONE"))
		    return;
	    }
	}

	int orbType = ORBInfo.getOrbEnumValue();

	switch (orbType)
	{
	case ORBType.JAVAIDL:
	    {
		if (interposition)
			System.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.com.arjuna.ats.jts.orbspecific.javaidl.interceptors.interposition.InterpositionORBInitializerImpl", "com.arjuna.ats.jts.orbspecific.javaidl.interceptors.interposition.InterpositionORBInitializerImpl");
		else
			System.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.com.arjuna.ats.jts.orbspecific.javaidl.interceptors.context.ContextORBInitializerImpl", "com.arjuna.ats.jts.orbspecific.javaidl.interceptors.context.ContextORBInitializerImpl");
	    }
	    break;
	default:
	    {
            jtsLogger.i18NLogger.warn_context_orbnotsupported("ContextPropagationManager", ORBInfo.getInfo());
	    }
	    break;
	}
    }

}