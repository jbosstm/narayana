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
 * Copyright (C) 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: Orbix2kRCServiceInit.java 2342 2006-03-30 13:06:17Z  $
 *
 */

package com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators;


import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.internal.jts.recovery.RecoveryCreator;
import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.*;

import com.arjuna.ats.jts.logging.*;
import com.arjuna.common.util.logging.*;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.orbportability.*;
import com.arjuna.common.util.propertyservice.PropertyManager;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosTransactions.*;

import com.iona.corba.IT_CORBA.*;
import com.iona.corba.IT_PortableServer.*;
import com.iona.corba.IT_CORBA.WELL_KNOWN_ADDRESSING_POLICY_ID;
import com.iona.corba.IT_PortableServer.PERSISTENCE_MODE_POLICY_ID;
import com.iona.corba.IT_PortableServer.PersistenceModePolicyValue;

/**
 * Initialises Orbix2000 RecoveryCoordinator creation subsystem
 * and provides the Orbix-specific implementations of stuff
 *
 * All orbs are likely to be the same, constructing a GenericRecoveryCreator,
 * but with an orb-specific manager
 *
 * @message com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_1 [com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_1] - Failed to create poa for recoverycoordinators 
 * @message com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_2 [com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_2] - Orbix2kRCServiceInit - set default servant and activated
 * @message com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_3 [com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_3] - Orbix2kRCServiceInit - Failed to start RC service 
 * @message com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_4 [com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_4] - Failed to create default ORB or POA
 */

public class Orbix2kRCServiceInit implements RecoveryServiceInit
{
	
public Orbix2kRCServiceInit()
{
}

/**
 * Provide the POA for the recoverycoordinator.
 * Construct with the policies appropriate for its use in the RecoveryManager,
 * but the policies are usable by the Orbix2kRCManager to create the IOR's in
 * TS-using processes.
 */
 
static POA getRCPOA (String domainName)
{
    String rcServiceName = GenericRecoveryCreator.getRecCoordServiceName();

    if (jtsLogger.logger.isDebugEnabled())
    {
	jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
			       FacilityCode.FAC_CRASH_RECOVERY, 
			       "Orbix2kRCServiceInit.getRCPOA " + rcServiceName );
    }
    
    if (_poa == null)
    {
	String poaName = POA_NAME_PREFIX + GenericRecoveryCreator.getRecCoordServiceName();
    
	_orb = com.arjuna.orbportability.internal.InternalORB.getInstance("RecoveryServer");
	
	// init it !!
	String[] params = new String[2];
	params[0] = "-ORBname";
	params[1] = orbName + orbNamePrefix + domainName;

	_orb.initORB(params, null);
	_oa = OA.getRootOA(_orb);

	// construct the Orbix-proprietary policies

	/*
	 * Start with the well-known addressing policy, so we can do lazy
	 * RecoveryCoordinator creation properly.
	 */

	try
	{
	    _oa.initOA();

	    org.omg.CORBA.ORB theORB = _orb.orb();

	    org.omg.PortableServer.POA rootPOA = _oa.rootPoa ();
	    Any wellKnownAddressingPolicy = theORB.create_any();

	    wellKnownAddressingPolicy.insert_string(domainName);

	    // specify direct persistence
	    Any persistenceValue = theORB.create_any();
	    PersistenceModePolicyValueHelper.insert(persistenceValue, PersistenceModePolicyValue.DIRECT_PERSISTENCE);
	    
	    // create direct persistent POA

	    // make the policy lists, with standard policies

	    org.omg.CORBA.Policy[] policies = null;
	    
	    policies = new Policy []
	    {
		rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT),
		rootPOA.create_thread_policy(ThreadPolicyValue.ORB_CTRL_MODEL),
		rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
		rootPOA.create_id_uniqueness_policy(IdUniquenessPolicyValue.MULTIPLE_ID),
		rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_DEFAULT_SERVANT),
		theORB.create_policy(WELL_KNOWN_ADDRESSING_POLICY_ID.value, wellKnownAddressingPolicy),
		theORB.create_policy(PERSISTENCE_MODE_POLICY_ID.value, persistenceValue)
	    };

	    _poa = rootPOA.create_POA(poaName, null, policies);
	}
	catch (Exception ex)
	{
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_1", ex);
	}
    }

    return _poa;    
}

/**
 * This starts the service in the RecoveryManager.
 */

public boolean startRCservice ()
{
    POA ourPOA = getRCPOA("recovery_coordinator");

    /*
     * We need an ORB and POA that are set up normally so that things like
     * string_to_object work! If we use the CR ORB/POA then we block since that POA
     * isn't fully activated (this is probably an Orbix-ism!)
     */

    com.arjuna.orbportability.ORB mainORB = com.arjuna.orbportability.ORB.getInstance("default");
    com.arjuna.orbportability.RootOA mainPOA = com.arjuna.orbportability.OA.getRootOA(mainORB);
    
    String[] args = null;
    
    try
    {
	/*
	 * Default parameters are good enough - we don't do much with the ORB or POA.
	 */

	mainORB.initORB(args, null);
	mainPOA.initOA();
    }
    catch (Exception ex)
    {
	ex.printStackTrace();

	jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_4", ex);

	return false;
    }

    ORBManager.setORB(mainORB);
    ORBManager.setPOA(mainPOA);

    try {
	// get the orb, so we can pass it to the default servant
	
	// make the default servant
	Orbix2kRCDefaultServant theButler = new Orbix2kRCDefaultServant(_orb.orb());

	// register it on the POA
	ourPOA.set_servant(theButler);

	// activate the poa 
	ourPOA.the_POAManager().activate();

	if (jtsLogger.loggerI18N.isDebugEnabled())
	{
	    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
				   FacilityCode.FAC_CRASH_RECOVERY, 
				   "com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_2");
	}
    
	runOaOrbix _runOA = new runOaOrbix();

	return true;
    } catch (Exception ex) {
	jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRCServiceInit_3", ex);
	return false;
    }    
    
}

public static void shutdownRCService ()
{
    _poa = null;
}

private static final String POA_NAME_PREFIX = "RcvCo-";

private static POA                _poa = null;

protected static com.arjuna.orbportability.ORB _orb = null;
protected static com.arjuna.orbportability.RootOA _oa = null;

private static final String orbNamePrefix = "ots_";
private static final String orbName = "arjuna.portable_interceptor.";


};
