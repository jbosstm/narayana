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
 * Copyright (C) 2003,            
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ServerRecoveryInterceptor.java 2342 2006-03-30 13:06:17Z  $                                                                 
 */

package com.arjuna.ats.internal.jts.orbspecific.ibmorb.recoverycoordinators;


import com.arjuna.ats.jts.logging.jtsLogger;
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
            jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_ServerInitializer_1(ex);
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
