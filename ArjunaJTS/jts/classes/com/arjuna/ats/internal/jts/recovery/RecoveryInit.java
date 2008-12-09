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
 * $Id: RecoveryInit.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery;

import com.arjuna.ats.arjuna.recovery.RecoveryActivator;
import com.arjuna.ats.jts.logging.*;
import com.arjuna.common.util.logging.*;

import com.arjuna.orbportability.orb.Attribute;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.common.Environment;
import com.arjuna.orbportability.*;

import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.SystemException;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.common.util.propertyservice.PropertyManager;

import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCreator;

/**
 * Registers the appropriate classes with the ORB.
 *   An application using the Transaction Service should load an instance of this class
 *   prior to orb-initialisation.
 *   Loading an instance can be achieved by naming the class in an OrbPreInit
 *   property.
 *   <p>Orb-specific details of recovery are handled by this class.
 *  
 * @author Malik SAHEB 
 *
 * @message com.arjuna.ats.internal.jts.recovery.RecoveryInit_1 [com.arjuna.ats.internal.jts.recovery.RecoveryInit_1] - added ORBAttribute for recoveryCoordinatorInitialiser
 * @message com.arjuna.ats.internal.jts.recovery.RecoveryInit_2 [com.arjuna.ats.internal.jts.recovery.RecoveryInit_2] - Full crash recovery is not supported with this orb
 * @message com.arjuna.ats.internal.jts.recovery.RecoveryInit_2 [com.arjuna.ats.internal.jts.recovery.RecoveryInit_3] - Set property {0}  =  {1}
 * @message com.arjuna.ats.internal.jts.recovery.RecoveryInit_3 [com.arjuna.ats.internal.jts.recovery.RecoveryInit_4] - RecoveryCoordinator service can only be provided in RecoveryManager
 * @message com.arjuna.ats.internal.jts.recovery.RecoveryInit_4 [com.arjuna.ats.internal.jts.recovery.RecoveryInit_5] - ORB/OA initialisation failed: {0}
 */

public class RecoveryInit
{
    
    private static boolean _initialised = false;
    private static boolean _isNormalProcess = true;
    
    // no accessible variable for this first property name prefix
    private static final String eventHandlerPropertyPrefix = Environment.EVENT_HANDLER;
 
    /** 
     * Constructor does the work as a result of being registered as an ORBPreInit
     * class
     *
     * @message com.arjuna.ats.internal.jts.recovery.recoveryinit_1 Failure recovery not supported for this ORB.
     */
    public RecoveryInit ()
    {
	/*
	 * We only really want a single instance of this class to be executed,
	 * no matter how many ORBs we may have in a process.
	 */

	if (!_initialised)
	{
	    _initialised = true;
	
	    // the eventhandler is the same for all orbs (at the moment)
	    String eventHandlerPropertyName = eventHandlerPropertyPrefix + "_Recovery";
	    String eventHandlerPropertyValue = "com.arjuna.ats.internal.jts.recovery.contact.RecoveryContactWriter";

	    Object recoveryCoordinatorInitialiser = null;
	    String InitClassName = null;

	    if ( _isNormalProcess) 
	    {
		try
		{
		    // Use Here a class that should be initialized with a specific class specific to the ORB
		    // To determine the class to load use the ORBType
		    
		    int orbType = ORBInfo.getOrbEnumValue();
			
		    switch (orbType)
		    {
		    case ORBType.ORBIX2000:
			{
			    InitClassName = "com.arjuna.ats.internal.jts.orbspecific.orbix2000.recoverycoordinators.Orbix2kRecoveryInit";
			    recoveryCoordinatorInitialiser = Thread.currentThread().getContextClassLoader().loadClass(InitClassName).newInstance();
			}
			break;
		    case ORBType.JACORB:
			{
			    InitClassName = "com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRecoveryInit";
			    recoveryCoordinatorInitialiser = Thread.currentThread().getContextClassLoader().loadClass(InitClassName).newInstance();
			}
			break;
		    default:
			{
			    if (jtsLogger.loggerI18N.isWarnEnabled())
			    {
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.recoveryinit_1");
			    }
			}
			break;
		    }
		    
		    // register the ContactWriter to watch for the first ArjunaFactory construction

		    opPropertyManager.propertyManager.setProperty(eventHandlerPropertyName,eventHandlerPropertyValue);
			
		    // Change here above the way to call this startRCService - 
		    // otherwise call it in JacOrbRecoveryInit above.
		}
		catch (Exception e)
		{
		    jtsLogger.loggerI18N.fatal("com.arjuna.ats.internal.jts.recovery.RecoveryInit_4", new Object[] {e});

		    throw new com.arjuna.ats.arjuna.exceptions.FatalError();
		}
		
		if (jtsLogger.loggerI18N.isDebugEnabled())
		{
		    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
					       FacilityCode.FAC_CRASH_RECOVERY, 
					       "com.arjuna.ats.internal.jts.recovery.RecoveryInit_1");
		    
		    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
					       FacilityCode.FAC_CRASH_RECOVERY, 
					       "com.arjuna.ats.internal.jts.recovery.RecoveryInit_2", 
					       new Object[] {eventHandlerPropertyName, eventHandlerPropertyValue});
		}
	    }
	}
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

}

