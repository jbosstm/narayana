/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators;

import com.arjuna.ats.jts.logging.jtsLogger;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * This class registers the ServerForwardInterceptor 
 * with the ORB.
 *
 * @author Malik Saheb
 *
 */

public class ServerInitializer
        extends org.omg.CORBA.LocalObject
        implements ORBInitializer
{

    static com.arjuna.orbportability.ORB myORB = null;

    public ServerInitializer() {
    }

    /**
     * This method resolves the NameService and registers the 
     * interceptor.
     */

    public void post_init(ORBInitInfo info)
    {
        try
        {
            org.omg.CORBA.ORB theORB = ((com.sun.corba.se.impl.interceptors.ORBInitInfoImpl)info).getORB();

            info.add_server_request_interceptor (new ServerRecoveryInterceptor(theORB));
        }
        catch (Exception e) {
            jtsLogger.i18NLogger.warn_orbspecific_recoverycoordinators_ServerInitializer_1(e);
        }
    }

    public void pre_init(ORBInitInfo info) {
    }
} // ServerInitializer