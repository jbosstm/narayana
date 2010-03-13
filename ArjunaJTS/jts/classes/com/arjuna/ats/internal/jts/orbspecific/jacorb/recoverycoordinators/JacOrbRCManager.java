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
 * $Id: JacOrbRCManager.java 2342 2006-03-30 13:06:17Z  $
 */


package com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosTransactions.*;

import com.arjuna.ats.internal.jts.orbspecific.recovery.recoverycoordinators.*;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;
import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.*;

import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.state.*;

/**
 * Implementation of RecoveryCreator for JacOrb
 * Handles the creation of RecoveryCoordinator objects for
 * JacOrb .  The RCs are created locally but also will be
 * recreated in the RecoveryManager if necessary following a crash
 * of this process.
 *
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_1 [com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_1] - JacOrbRCManager: Created reference for tran {0} = {1}
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_2 [com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_2] - RCManager.makeRC did not make rcvco reference
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_3 [com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_3] - RCManager could not find file in object store.
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_4 [com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_4] - RCManager could not find file in object store during setup.
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_5 [com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_5] - Unexpected exception during IOR setup {0}
 */

public class JacOrbRCManager implements RcvCoManager
{

/**
 * The repository id for RecoveryCoordinator
 */
private static final String rcvcoRepositoryId = RecoveryCoordinatorHelper.id();

/**
 *  Creates RecoveryCoordinator IORs under JacOrb.
 *  Unlike some other RcvCoManager's, this does not create any real
 *  RecoveryCoordinator objects.
 */
    public JacOrbRCManager()
    {
        // _ourPOA = JacOrbRCServiceInit.getRCPOA("transaction");
    }

    /**
     * We create a RecoveryCoordinator reference, but without (we think)
     * actually making the implementation object available to the orb.
     * The data needed to construct the RecoveryCoordinator is put in
     * the ObjectId. If a replay_completion is received, it will be sent,
     * via the locationd daemon, to the RecoveryManager.
     */

    public RecoveryCoordinator makeRC( Uid RCUid, Uid tranUid,
				       Uid processUid,
				       boolean isServerTransaction )
    {
	initialise();

	RecoveryCoordinator rc = null;

	// mangle those parameters to the string key (object id sort of thing)

	try
	{
	    String rcObjectId = GenericRecoveryCoordinator.makeId(RCUid, tranUid, processUid, isServerTransaction);

	    if (ref_ReCoo != null)
	    {
		// New for IOR template
		String new_ior = RecoverIOR.newObjectKey(ref_ReCoo, rcObjectId);
		org.omg.CORBA.Object rcAsObject = ORBManager.getORB().orb().string_to_object(new_ior);
		//End for IOR Template

		rc = RecoveryCoordinatorHelper.narrow(rcAsObject);

		if (jtsLogger.loggerI18N.isDebugEnabled())
		{
		    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS,
					       VisibilityLevel.VIS_PUBLIC,
					       FacilityCode.FAC_CRASH_RECOVERY,
					       "com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_1", new java.lang.Object[]{tranUid, rc});
		}
	    }
	    else
	    {
		if (JacOrbRCManager._runWithoutDaemon)
		    throw new NO_IMPLEMENT();
		else
		{
		    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_3");

		    rc = null;
		}
	    }
	}

	catch (Exception ex)
	{
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_2", ex);
	}

	return rc;
    }

    public void destroy (RecoveryCoordinator rc) throws SystemException
    {
        // does nothing for JacORB
    }

    public void destroyAll (java.lang.Object[] params) throws SystemException
    {
        // does nothing for JacORB
    }

    private final synchronized void initialise ()
    {
	if (!_initialised)
	{
	    _initialised = true;

	    if (!_runWithoutDaemon)
	    {
		try
		{
		    //Retrieve from Object Store
		    if (currentStore == null)
		    {
			currentStore = TxControl.getStore();
		    }

		    InputObjectState iState = currentStore.read_committed(new Uid( JacOrbRCServiceInit.uid4Recovery), JacOrbRCServiceInit.type());

		    if (iState != null)
			ref_ReCoo = iState.unpackString();
		    else
			jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_4");
		}
		catch (java.io.FileNotFoundException ex)
		{
		    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_4");
		}
		catch (Exception ex)
		{
		    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager_5", new java.lang.Object[]{ex});
		}
	    }
	}
    }

protected char rcKeyDelimiter = '#';

static protected String ref_ReCoo = null;

private POA _ourPOA;

    private static boolean _runWithoutDaemon = false;
    private static boolean _initialised = false;

    private ObjectStore     currentStore;

    static
    {
	/*
	 * Undocumented "feature" that lets us run tests without having
	 * to start the recovery daemon. In general we don't want people
	 * doing that kind of thing, but it makes development testing a
	 * lot easier.
	 *
	 * Note: this relies directly on system property lookup, since we don't
	 * want to expose the setting via the public EnvironmentBean config.
	 */
	String env = System.getProperty("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.noDaemon");

	if (env != null)
	{
	    if (env.equals("YES"))
		_runWithoutDaemon = true;
	}
    }

}
