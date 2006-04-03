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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Orbix2kRCDefaultServant.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators;

import org.omg.CORBA.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.internal.jts.orbspecific.recovery.recoverycoordinators.*;
import org.omg.CosTransactions.*;

import org.omg.PortableServer.*;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;

/**
 * @message com.arjuna.ats.internal.jts.recovery.recoverycoordinators.orbspecific.orbix2000.Orbix2kRCDefaultServant_1 [com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCDefaultServant_1] - Orbix2kRCDefaultServant replay_completion for {0}
 * @message com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCDefaultServant_2 [com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCDefaultServant_2] - Orbix2kRCDefaultServant.replay_completion got {0}
 */

public class Orbix2kRCDefaultServant extends GenericRecoveryCoordinator
{
    private ORB _ourOrb;
     
    /**
     * constructor supplies orb - used only within package
     */
     
    Orbix2kRCDefaultServant(ORB orb)
    {
	super();    // ensure id is null
	_ourOrb = orb;
	
	if (jtsLogger.logger.isDebugEnabled())
	    {
		jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, 
				       VisibilityLevel.VIS_PUBLIC, 
				       FacilityCode.FAC_CRASH_RECOVERY, 
				       "Orbix2kRCDefaultServant(orb)");
	    }
    }
    
    public Status replay_completion ( Resource res ) throws SystemException, NotPrepared
    {
	// finding the objectid
	// get the current poa
	if (jtsLogger.logger.isDebugEnabled())
	    {
		jtsLogger.logger.debug(DebugLevel.FUNCTIONS, 
				       VisibilityLevel.VIS_PUBLIC, 
				       FacilityCode.FAC_CRASH_RECOVERY, 
				       "Orbix2kRCDefaultServant::replay_completion)");
	    }

	try {
	    org.omg.CORBA.Object obj = _ourOrb.resolve_initial_references("POACurrent");
	    org.omg.PortableServer.Current poa_current = 
			    org.omg.PortableServer.CurrentHelper.narrow(obj);
	    byte[] objectId = poa_current.get_object_id();

	    String objectIdString = new String(objectId);

	    
	    // convert that to the structured id
	    RecoveryCoordinatorId  recovCoId = RecoveryCoordinatorId.reconstruct(objectIdString);
    
	    if (jtsLogger.loggerI18N.isDebugEnabled())
		{
		    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
					       VisibilityLevel.VIS_PUBLIC, 
					       FacilityCode.FAC_CRASH_RECOVERY, 
					       "com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCDefaultServant_1", new java.lang.Object[]{recovCoId});
		}

	    // and do the real replay
	    return GenericRecoveryCoordinator.replay_completion(recovCoId, res);
	}
	catch (NotPrepared exp)
	{
	    throw exp;
	}
	catch (Exception ex)
	{
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCDefaultServant_2", new java.lang.Object[] {ex});
	    return Status.StatusUnknown;
	}
    }
}
