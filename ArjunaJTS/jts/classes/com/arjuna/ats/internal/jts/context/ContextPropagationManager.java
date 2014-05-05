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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ContextPropagationManager.java 2342 2006-03-30 13:06:17Z  $
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
	case ORBType.JACORB:
	    {
		if (interposition)
			System.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.com.arjuna.ats.jts.orbspecific.jacorb.interceptors.interposition.InterpositionORBInitializerImpl", "com.arjuna.ats.jts.orbspecific.jacorb.interceptors.interposition.InterpositionORBInitializerImpl");
		else
			System.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.com.arjuna.ats.jts.orbspecific.jacorb.interceptors.context.ContextORBInitializerImpl", "com.arjuna.ats.jts.orbspecific.jacorb.interceptors.context.ContextORBInitializerImpl");
	    }
	    break;
	case ORBType.IBMORB:
	    {
		if (interposition)
			System.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.com.arjuna.ats.jts.orbspecific.ibmorb.interceptors.interposition.InterpositionORBInitializerImpl", "com.arjuna.ats.jts.orbspecific.ibmorb.interceptors.interposition.InterpositionORBInitializerImpl");
		else
			System.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.com.arjuna.ats.jts.orbspecific.ibmorb.interceptors.context.ContextORBInitializerImpl", "com.arjuna.ats.jts.orbspecific.ibmorb.interceptors.context.ContextORBInitializerImpl");
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
