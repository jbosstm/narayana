/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ActionStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.gandiva.ObjectName;
import com.arjuna.ats.arjuna.utils.FileLock;
import com.arjuna.ats.arjuna.utils.Utility;
import java.io.File;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import java.io.IOException;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

/**
 * The basic transaction log implementation. Uses the no file-level
 * locking implementation of the file system store since only a single user
 * (the coordinator) can ever be manipulating the action's state.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ActionStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ActionStore extends ShadowNoFileLockStore
{

public int typeIs ()
    {
	return ObjectStoreType.ACTION;
    }
    
    /**
     * @return current state of object. Assumes that genPathName allocates
     * enough extra space to allow extra chars to be added.
     * Action stores only store committed objects
     *
     * @message com.arjuna.ats.internal.arjuna.objectstore.ActionStore_1 [com.arjuna.ats.internal.arjuna.objectstore.ActionStore_1] - ActionStore.currentState({0}, {1}) - returning {2}
     */

public int currentState (Uid objUid, String tName) throws ObjectStoreException
    {
	int theState = ObjectStore.OS_UNKNOWN;
    
	if (storeValid())
	{
	    String path = genPathName(objUid, tName, ObjectStore.OS_ORIGINAL);

	    if (exists(path))
		theState = ObjectStore.OS_COMMITTED;

	    path = null;
	}

	if (tsLogger.arjLoggerI18N.debugAllowed())
	{
	    tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					 FacilityCode.FAC_OBJECT_STORE, 
					 "com.arjuna.ats.internal.arjuna.objectstore.ActionStore_1", 
					 new Object[]{objUid, tName,ObjectStore.stateStatusString(theState)});
	}
	
	return theState;
    }

    /**
     * Commit a previous write_state operation which
     * was made with the SHADOW StateType argument. This is achieved by
     * renaming the shadow and removing the hidden version.
     */

    public boolean commit_state (Uid objUid,
				 String tName) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, 
				     "ActionStore.commit_state("+objUid+", "+tName+")");
	}

	boolean result = false;

	/* Bail out if the object store is not set up */

	if (!storeValid())
	    return false;

	if (currentState(objUid, tName) == ObjectStore.OS_COMMITTED)
	    result = true;
    
	return result;
    }

    public boolean hide_state (Uid u, String tn) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "ActionStore.hide_state("+u+", "+tn+")");
	}
	
	return false;
    }

    public boolean reveal_state (Uid u, String tn) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "ActionStore.reveal_state("+u+", "+tn+")");
	}
	
	return false;
    }

    public InputObjectState read_committed (Uid storeUid, String tName) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "ActionStore.read_committed("+storeUid+", "+tName+")");
	}
	
	return super.read_committed(storeUid, tName);
    }

    public InputObjectState read_uncommitted (Uid u, String tn) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "ActionStore.read_uncommitted("+u+", "+tn+")");
	}
	
	return null;
    }

    public boolean remove_committed (Uid storeUid, String tName) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "ActionStore.remove_committed("+storeUid+", "+tName+")");
	}
	
	return super.remove_committed(storeUid, tName);
    }

    public boolean remove_uncommitted (Uid u, String tn) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "ActionStore.remove_uncommitted("+u+", "+tn+")");
	}
	
	return false;
    }

    public boolean write_committed (Uid storeUid, String tName, OutputObjectState state) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "ActionStore.write_committed("+storeUid+", "+tName+")");
	}
	
	return super.write_committed(storeUid, tName, state);
    }

    public boolean write_uncommitted (Uid u, String tn, OutputObjectState s) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "ActionStore.write_uncommitted("+u+", "+tn+", "+s+")");
	}
	
	return false;
    }

    public ClassName className ()
    {
	return ArjunaNames.Implementation_ObjectStore_ActionStore();
    }

    public static ClassName name ()
    {
	return ArjunaNames.Implementation_ObjectStore_ActionStore();
    }    

    /**
     * Have to return as a ShadowingStore because of
     * inheritence.
     */
    
    public static ShadowingStore create ()
    {
	return new ActionStore("");
    }

    /**
     * @message com.arjuna.ats.internal.arjuna.objectstore.ActionStore_2 [com.arjuna.ats.internal.arjuna.objectstore.ActionStore_2] - ActionStore.create caught: {0}
     */

    public static ShadowingStore create (Object[] param)
    {
	if (param == null)
	    return null;

	String location = (String) param[0];
	Integer shareStatus = (Integer) param[1];
	int ss = ObjectStore.OS_UNSHARED;
	
	if (shareStatus != null)
	{
	    try
	    {
		if (shareStatus.intValue() == ObjectStore.OS_SHARED)
		    ss = ObjectStore.OS_SHARED;
	    }
	    catch (Exception e)
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.ActionStore_2",
						new Object[]{e});
		}
	    }
	}
	
	return new ActionStore(location, ss);
    }

    public static ShadowingStore create (ObjectName param)
    {
	if (param == null)
	    return null;
	else
	    return new ActionStore(param);
    }
    
    protected ActionStore (String locationOfStore)
    {
	this(locationOfStore, ObjectStore.OS_UNSHARED);
    }

    protected ActionStore (String locationOfStore, int shareStatus)
    {
	super(shareStatus);

	try
	{
	    setupStore(locationOfStore);
	}
	catch (ObjectStoreException e)
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		tsLogger.arjLogger.warn(e.getMessage());

	    super.makeInvalid();
	    
	    throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString());
	}
    }

    protected ActionStore ()
    {
	this(ObjectStore.OS_UNSHARED);
    }
    
    protected ActionStore (int shareStatus)
    {
	super(shareStatus);
    }

    protected ActionStore (ObjectName objName)
    {
	super(objName);
    }
    
    protected synchronized boolean setupStore (String location) throws ObjectStoreException
    {
	if (!checkSync)
	{
	    String syncOpt = arjPropertyManager.propertyManager.getProperty(Environment.TRANSACTION_SYNC);

	    if (syncOpt != null)
	    {
		if (syncOpt.compareTo("OFF") == 0)
		    syncOff();
		else
		    syncOn();
	    }
	    else
		syncOn();

	    checkSync = true;
	}

	return super.setupStore(location);
    }
    
    private static boolean checkSync = false;

}
