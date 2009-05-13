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
 * $Id: RecoveryEnablement.java 2342 2006-03-30 13:06:17Z  $
 *
 */

package com.arjuna.ats.internal.jts.orbspecific.recovery;

import com.arjuna.ats.arjuna.recovery.RecoveryActivator;
import com.arjuna.ats.jts.logging.*;
import com.arjuna.common.util.logging.*;
import com.arjuna.orbportability.common.Environment;

import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.SystemException;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.orbportability.orb.Attribute;

import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.orbportability.*;
import com.arjuna.common.util.propertyservice.PropertyManager;

import com.arjuna.ats.internal.jts.Implementations;

import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCreator;
import com.arjuna.ats.internal.jts.recovery.*;
import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.RecoveryServiceInit;

import java.net.*;

/**
 * Registers the appropriate classes with the ORB.
 *   An application using the Transaction Service should load an instance of this class
 *   prior to orb-initialisation.
 *   Loading an instance can be achieved by naming the class in an OrbPreInit
 *   property.
 *   <p>Orb-specific details of recovery are handled by this class.
 *  <p>
 *  The class also includes the static startRCservice method (package access),
 *  used by the RecoveryManager, which is orb-specific
 *
 *
 * @author Peter Furniss (peter.furniss@arjuna.com), Mark Little (mark@arjuna.com), Malik Saheb (malik.saheb@arjuna.com)
 * @version $Id: RecoveryEnablement.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 *
 * @message com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_1 [com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_1] - Could not locate supported ORB for RecoveryCoordinator initialisation.
 * @message com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_2 [com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_2] - Full crash recovery is not supported with this orb
 * @message com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_3 [com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_3] - Set property {0}  =  {1}
 * @message com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_4 [com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_4] - RecoveryCoordinator service can only be provided in RecoveryManager
 * @message com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_5 [com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_5] - ORB/OA initialisation failed: {0}
 * @message com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_6 [com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_5] - The Recovery Service Initialisation failed: {0}
 */

public class RecoveryEnablement implements RecoveryActivator
{

    private static boolean _isNormalProcess = true;
    private static String  _RecoveryManagerTag = null;

    // no accessible variable for this first property name prefix
    private static final String eventHandlerPropertyPrefix = Environment.EVENT_HANDLER;

    /**
     * Constructor does the work as a result of being registered as an ORBPreInit
     * class
     */

    public RecoveryEnablement ()
    {
	Implementations.initialise();
    }

    /**
     * This static method is used by the RecoveryManager to suppress
     * aspects of recovery enablement in it's own
     * process, without requiring further property manipulations
     */

    public static void isNotANormalProcess()
    {
	_isNormalProcess = false;
    }

    public static boolean isNormalProcess ()
    {
	return _isNormalProcess;
    }

    /**
     *  Used by the RecoveryManager to start the recoverycoordinator
     *  service, using whatever orb-specific techniques are appropriate.
     *  This is placed here because it may need to set post-orbinitialisation
     *  properties, like the regular enabler.
     */

    public boolean startRCservice()
    {
	int orbType = ORBInfo.getOrbEnumValue();
	boolean result = false;
	RecoveryServiceInit recoveryService = null;

	String theClassName = null;

	// The class that should start the service shall not be called directly. An intermediate class shall be used
	try
	{
	    switch (orbType)
	    {
	    case ORBType.JACORB:
		{
		    theClassName = "com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCServiceInit";
		    recoveryService = (RecoveryServiceInit) Thread.currentThread().getContextClassLoader().loadClass(theClassName).newInstance();
		    recoveryService.startRCservice();

		    result = true;
		}
		break;
	    default:
		{
		    if (jtsLogger.loggerI18N.isWarnEnabled())
		    {
			jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_1");
		    }
		}
		break;
	    }
	}
	catch (Exception e)
	{
	    jtsLogger.loggerI18N.fatal("com.arjuna.ats.internal.jts.recovery.RecoveryEnablement_6", new Object[] {e}, e);
	}

	return result;
    }

    /**
     * Return the RecoveryManager tag. This can be set by a property.
     */
    public static String getRecoveryManagerTag()
    {
	if (_RecoveryManagerTag != null) {
	    return _RecoveryManagerTag;
	} else {
	    return null;
	}
    }

    static{

	// tell the recovery init we aren't a normal TS-user
	RecoveryEnablement.isNotANormalProcess();
	RecoveryInit.isNotANormalProcess();

	// see if there is a property defining the recoverymanager
	// servicename

	//	_RecoveryManagerTag = System.getProperty(RecoveryEnvironment.RECOVERY_MANAGER_TAG);

	if (_RecoveryManagerTag == null)
	{
	    // if no property, use the hostname/ip address

	    InetAddress thisAddress = null;
	    try
	    {
		thisAddress = InetAddress.getLocalHost();
		_RecoveryManagerTag = thisAddress.getHostName();
	    }
	    catch (UnknownHostException uhe)
	    {
		uhe.printStackTrace();
	    }

	}
	// prune off any spaces
	if (_RecoveryManagerTag != null)
	{
	    _RecoveryManagerTag =_RecoveryManagerTag.trim();
	}
    }

}

