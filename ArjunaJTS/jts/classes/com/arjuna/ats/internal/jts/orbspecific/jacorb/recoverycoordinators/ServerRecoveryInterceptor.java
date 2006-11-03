/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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

package com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators;


import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * This interceptor looks for the content of the Service Context field and extract the data that 
 * contains information identifying the transaction and the process Id.
 *
 * @author Malik Saheb
 *
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ServerRecoveryInterceptor_1 [com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ServerRecoveryInterceptor_1] -  Failed to obtain the service context - 
 */

public class ServerRecoveryInterceptor
    extends org.omg.CORBA.LocalObject 
    implements ServerRequestInterceptor
{
    private RecoveryCoordinator reco = null;
    private boolean in_loop = false;
    private org.omg.CORBA.ORB _ourOrb = null;
    
    ServiceContext RCctx = null;
    int RecoveryContextId = 100001;

    byte[] RCobjectId;

    public ServerRecoveryInterceptor(org.omg.CORBA.ORB orb)
    {
	org.omg.CORBA.Object obj = null ;
	_ourOrb = orb;
    }

  public void receive_request_service_contexts (ServerRequestInfo ri)
        throws ForwardRequest
    {
        ServiceContext context;
        org.omg.CORBA.Any any;
        
        try
        {
            context = ri.get_request_service_context (RecoveryContextId);
	    String objectIdString = new String(context.context_data);
	    JacOrbRCDefaultServant.RCObjectId = context.context_data;
        }
        catch (Exception ex)
        {
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ServerInitialize_1", ex);
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
