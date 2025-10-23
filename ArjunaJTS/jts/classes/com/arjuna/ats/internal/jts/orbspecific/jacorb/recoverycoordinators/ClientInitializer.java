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
 * $Id: ClientInitializer.java 2342 2006-03-30 13:06:17Z  $                                                                 
 */

package com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators;

import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

import com.arjuna.ats.jts.logging.jtsLogger;

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
        catch (Exception e) {
            jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_ClientInitializer_1(e);
        }
    }

    public void pre_init(ORBInitInfo info) {    
    }
} // ClientInitializer
