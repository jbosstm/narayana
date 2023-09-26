/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators;

import com.arjuna.ats.jts.logging.jtsLogger;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * This class registers the ClientForwardInterceptor 
 * with the ORB.
 *
 * @author Malik Saheb
 */

public class ClientInitializer 
    extends org.omg.CORBA.LocalObject 
    implements ORBInitializer
{

    static com.arjuna.orbportability.ORB myORB = null;

    public ClientInitializer() {
    }

    /**
     * This method resolves the NameService and registers the 
     * interceptor.
     */

    public void post_init(ORBInitInfo info) 
    {
        try
        {
	    // Obtain the Orb reference having requested this initilization
	    org.omg.CORBA.ORB theORB = ((com.sun.corba.se.impl.interceptors.ORBInitInfoImpl)info).getORB();
	    
	    /*
            NamingContextExt nc = NamingContextExtHelper.narrow
                (info.resolve_initial_references("NameService"));
	    */
	    
	    org.omg.PortableInterceptor.Current piCurrent = org.omg.PortableInterceptor.CurrentHelper.narrow
		(info.resolve_initial_references("PICurrent"));

	    int outSlotId = info.allocate_slot_id();

            info.add_client_request_interceptor 
		(new ClientForwardInterceptor(theORB, piCurrent, outSlotId));
        }
        catch (Exception e) {
            jtsLogger.i18NLogger.warn_orbspecific_recoverycoordinators_ClientInitializer_1(e);
        }
    }

    public void pre_init(ORBInitInfo info) {    
    }
} // ClientInitializer