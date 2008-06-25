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
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Orbix2kRCManager.java 2342 2006-03-30 13:06:17Z  $
 */


package com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosTransactions.*;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;

import com.arjuna.orbportability.orb.*;
import com.arjuna.orbportability.*;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.ats.internal.jts.orbspecific.recovery.recoverycoordinators.*;
import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.*;

import java.io.PrintWriter;

/**
 * Implementation of RecoveryCreator for Orbix2000 3.x.
 * Handles the creation of RecoveryCoordinator objects for
 * Orbix2000 .  The RCs are created locally but also will be
 * recreated in the RecoveryManager if necessary following a crash
 * of this process.
 *
 * @message com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCManager_1 [com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCManager_1] - Orbix2kRCManager: Created reference for tran {0} = {1}
 * @message com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCManager_2 [com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCManager_2] - Orbix2kRCManager.makeRC did not make rcvco reference 
 */

public class Orbix2kRCManager implements RcvCoManager
{

/**
 * The repository id for RecoveryCoordinator
 */
private static final String rcvcoRepositoryId = RecoveryCoordinatorHelper.id();

/**
 *  Creates RecoveryCoordinator IORs under Orbix 2000.
 *  Unlike some other RcvCoManager's, this does not create any real
 *  RecoveryCoordinator objects.
 */
public Orbix2kRCManager ()
    {
	_ourPOA = Orbix2kRCServiceInit.getRCPOA("transaction");
    }

    /**
     * We create a RecoveryCoordinator reference, but without (we think)
     * actually making the implementation object available to the orb.
     * The data needed to construct the RecoveryCoordinator is put in 
     * the ObjectId. If a replay_completion is received, it will be sent,
     * via the location daemon, to the RecoveryManager.
     */
    public RecoveryCoordinator makeRC( Uid RCUid, Uid tranUid,
				       Uid processUid,
				       boolean isServerTransaction )
    {
	RecoveryCoordinator rc = null;

	// mangle those parameters to the string key (object id sort of thing)

	try
	{
	    String rcObjectId = GenericRecoveryCoordinator.makeId(RCUid, tranUid, processUid, isServerTransaction);
    

	    byte[] rcObjectIdAsBytes = rcObjectId.getBytes();
	    org.omg.CORBA.Object rcAsObject = _ourPOA.create_reference_with_id(rcObjectIdAsBytes, rcvcoRepositoryId);

	    rc = RecoveryCoordinatorHelper.narrow(rcAsObject);

	    if (jtsLogger.loggerI18N.isDebugEnabled())
		{
		    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
					       VisibilityLevel.VIS_PUBLIC, 
					       FacilityCode.FAC_CRASH_RECOVERY, 
					       "com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCManager_1", new java.lang.Object[]{tranUid, rc});
		}
	    
	} catch (Exception ex) {
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCManager_2", ex);
	}

	return rc;
    }

public void destroy (RecoveryCoordinator rc) throws SystemException
    {
	// does nothing for Orbix 2000
    }

public void destroyAll (java.lang.Object[] params) throws SystemException
    {
	// does nothing for Orbix 2000
    }

private POA _ourPOA;

};
