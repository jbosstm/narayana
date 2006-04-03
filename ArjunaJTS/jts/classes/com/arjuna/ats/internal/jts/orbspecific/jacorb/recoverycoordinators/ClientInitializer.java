/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * $Id: ClientInitializer.java 2342 2006-03-30 13:06:17Z  $                                                                 
 */

package com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators;

import org.omg.PortableInterceptor.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.Any;
import org.omg.CosTransactions.*;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.logging.*;
import com.arjuna.common.util.logging.*;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import org.jacorb.orb.ORB;

/**
 * This class registers the ClientForwardInterceptor 
 * with the ORB.
 *
 * @author Malik Saheb
 *
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientInitializer_ [com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientInitializer_1] -  Failed in ClientInitializer::post_init - 
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
	    org.omg.CORBA.ORB theORB = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl)info).getORB();
	    
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
        catch (Exception e)
        {
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientInitialize_1", e);
        }
    }

    public void pre_init(ORBInitInfo info) {    
    }
} // ClientInitializer
