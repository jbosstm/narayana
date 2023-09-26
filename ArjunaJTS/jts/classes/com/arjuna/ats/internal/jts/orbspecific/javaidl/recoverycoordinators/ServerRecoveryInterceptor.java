/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators;


import com.arjuna.ats.jts.logging.jtsLogger;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 * This interceptor looks for the content of the Service Context field and extract the data that 
 * contains information identifying the transaction and the process Id.
 *
 * @author Malik Saheb
 *
 */

public class ServerRecoveryInterceptor
        extends org.omg.CORBA.LocalObject
        implements ServerRequestInterceptor
{

    int RecoveryContextId = 100001;

    public ServerRecoveryInterceptor(org.omg.CORBA.ORB orb)
    {
    }

    public void receive_request_service_contexts (ServerRequestInfo ri)
            throws ForwardRequest
    {
        ServiceContext context;

        try
        {
            context = ri.get_request_service_context (RecoveryContextId);
            JavaIdlRCDefaultServant.RCObjectId = context.context_data;
        }
        catch (Exception ex) {
            jtsLogger.i18NLogger.warn_orbspecific_recoverycoordinators_ServerInitializer_1(ex);
        }
    }

    public String name ()
    {
        return "arjuna.ServerRecoveryInterceptor";
    }

    public void destroy ()
    {
    }

    public void receive_request (ServerRequestInfo ri)
            throws ForwardRequest
    {
    }

    public void send_reply (ServerRequestInfo ri)
    {
    }

    public void send_exception (ServerRequestInfo ri)
            throws ForwardRequest
    {
    }

    public void send_other (ServerRequestInfo ri)
            throws ForwardRequest
    {
    }

}