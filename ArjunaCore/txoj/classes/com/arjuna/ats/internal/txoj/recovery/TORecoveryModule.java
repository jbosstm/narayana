/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
 * Copyright (C) 2000, 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TORecoveryModule.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.txoj.recovery;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.txoj.logging.txojLogger;
import com.arjuna.ats.txoj.logging.FacilityCode;

import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.gandiva.ObjectName;

import com.arjuna.common.util.logging.*;

import java.util.*;
import java.io.PrintWriter;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import java.io.IOException;

/**
 * This class is a plug-in module for the recovery manager.  This
 * class is responsible for the recovery of Transactional Objects (aka
 * AIT objects), i.e., objects that derive from LockManager and StateManager.
 *
 * @message com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_1 [com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_1] - TORecoveryModule created
 * @message com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_2 [com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_2] - TORecoveryModule created with {0}
 * @message com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_3 [com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_3] - TORecoveryModule - first pass
 * @message com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_4 [com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_4] - TO currently uncommitted {0} is a {1}
 * @message com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_5 [com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_5] - TORecoveryModule: searching for TOs: 
 * @message com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_6 [com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_6] - TORecoveryModule - second pass
 * @message com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_7 [com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_7] - TORecoveryModule.periodicWork(): Object ({0}, {1}) is no longer uncommitted.
 * @message com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_8 [com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_8] - TORecoveryModule.periodicWork(): Object ({0}, {1})  no longer exists.
 */

public class TORecoveryModule implements RecoveryModule
{

    /**
     * Create the module to scan in the default location for object states.
     * Any modifications to locations must occur in the properties file.
     */
    
public TORecoveryModule ()
{
    if (txojLogger.aitLoggerI18N.isDebugEnabled())
    {
	txojLogger.aitLoggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY, 
				       "com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_1");
    }
    
    /*
     * Where are TO's stored. Default.
     */
    
    _objectStore = new ObjectStore(ObjectStore.OS_SHARED);
}
    
    /**
     * Create the module to scan in the object store specified within the
     * ObjectName parameter. Any properties specified in the ObjectName
     * will not be overridden by anything in an associated property file.
     *
     * @see com.arjuna.ats.arjuna.gandiva.ObjectName
     */
    
public TORecoveryModule (ObjectName objName)
{
    if (txojLogger.aitLoggerI18N.isDebugEnabled())
    {
	txojLogger.aitLoggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY, 
				     "com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_2", 
				     new Object[]{objName});
    }
    
    _objectStore = new ObjectStore(objName);
}

public void periodicWorkFirstPass ()
{
    if (txojLogger.aitLoggerI18N.isInfoEnabled())
    {
	txojLogger.aitLoggerI18N.info("com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_3");
    }
        
    // Build a hashtable of uncommitted transactional objects
    _uncommittedTOTable = new Hashtable();
    
    try
	{
	 InputObjectState types = new InputObjectState();
	 
	 // find all the types of transactional object (in this ObjectStore)
	 if (_objectStore.allTypes(types)){
	     String theName = null;
	     
	     try {
		 boolean endOfList = false;
		 
		 while (!endOfList)    {
				// extract a type
		     theName = types.unpackString();
		     
		     if (theName.compareTo("") == 0)
			 endOfList = true;
		     else {
			 InputObjectState uids = new InputObjectState();
			 
			 // find the uids of anything with an uncommitted entry in the object store
			 if (_objectStore.allObjUids(theName, uids, ObjectStore.OS_UNCOMMITTED))
			 {
			     Uid theUid = new Uid(Uid.nullUid());
			     
			     try
			     {
				 boolean endOfUids = false;
				 
				 while (!endOfUids) {
				     // extract a uid
				     theUid.unpack(uids);
				     
				     if (theUid.equals(Uid.nullUid()))
					 endOfUids = true;
				     else {
					 String newTypeString = new String(theName);
					 Uid newUid = new Uid(theUid);
					 _uncommittedTOTable.put(newUid, newTypeString);
					 if (txojLogger.aitLoggerI18N.isDebugEnabled())
					 {
					     txojLogger.aitLoggerI18N.debug(DebugLevel.FUNCTIONS, 
									  VisibilityLevel.VIS_PUBLIC,
									  com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY,
									  "com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_4", new Object[]{newUid, newTypeString});
					 }
				     }
				 }
			     }
			     catch (Exception e)
			     {
				 // end of uids!
			     }
			 }
		     }
		 }
	     }
	     catch (IOException ex)
	     {
		 // nothing there.
	     }
	     catch (Exception e)
	     {
		     if (txojLogger.aitLoggerI18N.isWarnEnabled())
		     {
			 txojLogger.aitLoggerI18N.warn("com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_5", e);
		     }
	     }
	 }
	}
    catch (Exception e)
    {
	if (txojLogger.aitLoggerI18N.isWarnEnabled())
	{
	    txojLogger.aitLoggerI18N.warn("com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_5", e);
	}
    }
    
}

public void periodicWorkSecondPass ()
{
      if (txojLogger.aitLoggerI18N.isInfoEnabled())
      {
	  txojLogger.aitLoggerI18N.info("com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_6");
      }
    
    Enumeration uncommittedObjects = _uncommittedTOTable.keys();
    
    while (uncommittedObjects.hasMoreElements())
    {
	    Uid objUid = (Uid) uncommittedObjects.nextElement();
	    String objType = (String) _uncommittedTOTable.get(objUid);
	    
	    try
		{
		    if (_objectStore.currentState(objUid, objType) == ObjectStore.OS_UNCOMMITTED)
			{
			    recoverObject(objUid, objType);
			}
		    else {
			if (txojLogger.aitLoggerI18N.isDebugEnabled())
			{
			    txojLogger.aitLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
							 com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY, 
							 "com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_7",
							 new Object[]{objUid, objType});
			    }
			}
		}
	    catch (ObjectStoreException ose)
		{
		    if (txojLogger.aitLoggerI18N.isDebugEnabled())
		    {
			txojLogger.aitLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						     com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY, 
						     "com.arjuna.ats.internal.txoj.recovery.TORecoveryModule_8", 
						     new Object[]{objUid, objType});
		    }
		}
	}
}

/**
 * Set-up routine.
 */

protected void initialise ()
{
    if (txojLogger.aitLogger.isDebugEnabled())
    {
	txojLogger.aitLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				 com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY,
				   "TORecoveryModule.initialise()");
    }
}
    
private final void recoverObject(Uid objUid, String objType)
    {
	if (txojLogger.aitLogger.isDebugEnabled())
	{
	    txojLogger.aitLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
				     com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY,
				     "TORecoveryModule.recoverObject("
				       +objUid+", "+objType+")");
	}
	
	/*
	 * Get a shell of the TO and find out which transaction it was that
	 * got it uncommitted.
	 */
	 
	RecoveredTransactionalObject recoveredTO = new RecoveredTransactionalObject(objUid, objType, _objectStore);

	/*
	 * Tell it to replayPhase2, in whatever way it does
	 * (in fact it won't do anything unless it determines the
	 * transaction rolled back).
	 */

	recoveredTO.replayPhase2();
    }

	private Hashtable	   _uncommittedTOTable;
	
private static ObjectStore _objectStore = null;

    
}



