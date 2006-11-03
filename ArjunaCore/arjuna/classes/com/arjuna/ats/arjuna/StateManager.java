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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: StateManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.*;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.internal.arjuna.template.*;
import java.io.PrintWriter;
import java.util.*;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import java.io.IOException;

/**
 * The root of the Arjuna class hierarchy. This class provides
 * state management facilities than can be automatically used by
 * other classes by inheritance.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: StateManager.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 *
 * @message com.arjuna.ats.arjuna.StateManager_1 [com.arjuna.ats.arjuna.StateManager_1] - StateManager::terminate() should be invoked in every destructor
 * @message com.arjuna.ats.arjuna.StateManager_2 [com.arjuna.ats.arjuna.StateManager_2] - Activate of object with id = {0} and type {1} unexpectedly failed"
 * @message com.arjuna.ats.arjuna.StateManager_3 [com.arjuna.ats.arjuna.StateManager_3] - StateManager::deactivate - object store error 
 * @message com.arjuna.ats.arjuna.StateManager_4 [com.arjuna.ats.arjuna.StateManager_4] - StateManager::deactivate - save_state error
 * @message com.arjuna.ats.arjuna.StateManager_5 [com.arjuna.ats.arjuna.StateManager_5] - StateManager::destroy for object-id {0}
 * @message com.arjuna.ats.arjuna.StateManager_6 [com.arjuna.ats.arjuna.StateManager_6] - StateManager.destroy - failed to add abstract record.
 * @message com.arjuna.ats.arjuna.StateManager_7 [com.arjuna.ats.arjuna.StateManager_7] - StateManager.destroy - caught object store exception: {0}
 * @message com.arjuna.ats.arjuna.StateManager_8 [com.arjuna.ats.arjuna.StateManager_8] - StateManager.destroy - called on non-persistent or new object!
 * @message com.arjuna.ats.arjuna.StateManager_9 [com.arjuna.ats.arjuna.StateManager_9] - StateManager.restore_state - could not find StateManager state in object state!
 * @message com.arjuna.ats.arjuna.StateManager_10 [com.arjuna.ats.arjuna.StateManager_10] - StateManager::modified() invocation on an object whose state has not been restored - activating object
 * @message com.arjuna.ats.arjuna.StateManager_11 [com.arjuna.ats.arjuna.StateManager_11] - Delete called on object with uid {0} and type {1} within atomic action.
 * @message com.arjuna.ats.arjuna.StateManager_12 [com.arjuna.ats.arjuna.StateManager_12] - StateManager.cleanup - could not save_state from terminate!
 * @message com.arjuna.ats.arjuna.StateManager_13 [com.arjuna.ats.arjuna.StateManager_13] - Attempt to use volatile store.
 * @message com.arjuna.ats.arjuna.StateManager_14 [com.arjuna.ats.arjuna.StateManager_14] - Volatile store not implemented!
 * @message com.arjuna.ats.arjuna.StateManager_15 [com.arjuna.ats.arjuna.StateManager_15] - Invalid object state.
 */

public class StateManager
{

    /**
     * These methods must be used by a derived class. They
     * are responsible for packing and unpacking an object's state
     * to/from a state buffer. StateManager calls them at appropriate
     * times during the lifetime of the object, and may then pass the
     * buffer to a persistent object store for saving.
     *
     * @return <code>true</code> on success, <code>false</code> otherwise.
     */

public boolean save_state (OutputObjectState os, int ot)
    {
	/*
	 * Only pack additional information if this is for a
	 * persistent state modification.
	 */

	if (ot == ObjectType.ANDPERSISTENT)
	{
	    try
	    {
		BasicAction action = BasicAction.Current();

		if (action == null)
		    packHeader(os, null, Utility.getProcessUid());
		else
		    packHeader(os, action.get_uid(), Utility.getProcessUid());
	    }
	    catch (IOException e)
	    {
		return false;
	    }
	}

	return true;
    }

    /**
     * These methods must be provided by a derived class. They
     * are responsible for packing and unpacking an object's state
     * to/from a state buffer. StateManager calls them at appropriate
     * times during the lifetime of the object, and may then pass the
     * buffer to a persistent object store for saving.
     *
     * @return <code>true</code> on success, <code>false</code> otherwise.
     */

public boolean restore_state (InputObjectState os, int ot)
    {
	if (ot == ObjectType.ANDPERSISTENT)
	{
	    try
	    {
		Uid txId = new Uid(Uid.nullUid());
		Uid processUid = new Uid(Uid.nullUid());
		
		unpackHeader(os, txId, processUid);
	    }
	    catch (IOException e)
	    {
		return false;
	    }
	}

	return true;
    }
    
    /**
     * Destructor.
     */

public void finalize () throws Throwable
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_STATE_MAN, "StateManager.finalize() for object-id "+get_uid());
	}

	if (currentStatus == ObjectStatus.ACTIVE_NEW)
	{
	    BasicAction action = BasicAction.Current();
 
	    if ((action != null) && (action.status() == ActionStatus.RUNNING))
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_1");
		cleanup(false);
	    }
	}

	smAttributes = null;
	objectName = null;

	modifyingActions = null;
	usingActions = null;
	
	objectStore = null;
	storeRoot = null;
	objectUid = null;
    }

    /**
     * This operation activates an object. Activation of an object may
     * entail loading its passive state from the object store and unpacking
     * it into the memory resident form, or it may simply be a no-op.
     * Full activation is only necessary if the object is currently marked
     * as being PASSIVE (that is, the object was constructed as being of
     * type ANDPERSISTENT with an existing uid and has not already been
     * activated).
     *
     * Objects that are not of type ANDPERSISTENT or are persistent but
     * have not yet been saved in an object store (so-called new persistent
     * objects) are unaffected by this function.
     * Returns false if PASSIVE object cannot be loaded from object store,
     * true otherwise.
     *
     * The root of the object store is taken as <code>null</code>.
     *
     * @return <code>true</code> on success, <code>false</code> otherwise.
     * @see com.arjuna.ats.arjuna.objectstore.ObjectStore
     */

public boolean activate ()
    {
	return activate(null);
    }

    /**
     * This operation activates an object. Activation of an object may
     * entail loading its passive state from the object store and unpacking
     * it into the memory resident form, or it may simply be a no-op.
     * Full activation is only necessary if the object is currently marked
     * as being PASSIVE (that is, the object was constructed as being of
     * type ANDPERSISTENT with an existing uid and has not already been
     * activated).
     *
     * Objects that are not of type ANDPERSISTENT or are persistent but
     * have not yet been saved in an object store (so-called new persistent
     * objects) are unaffected by this function.
     * Returns false if PASSIVE object cannot be loaded from object store,
     * true otherwise.
     *
     * @return <code>true</code> on success, <code>false</code> otherwise.
     * @see com.arjuna.ats.arjuna.objectstore.ObjectStore
     */
    
public synchronized boolean activate (String rootName)
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_STATE_MAN, 
				     "StateManager::activate( "+((rootName != null) ? rootName : "null")+") for object-id "+objectUid);
	}
	
	if (myType == ObjectType.NEITHER)
	{
	    return true;
	}

	if (currentStatus == ObjectStatus.DESTROYED)
	    return false;

	BasicAction action = null;
	int oldStatus = currentStatus;
	boolean result = true;			/* assume 'succeeds' */
	boolean forceAR = false;

	/*
	 * Check if this action has logged its presence before. If not we force
	 * creation of an ActivationRecord so that each thread/action tree has
	 * an ActivationRecord in it. This allows us to passivate the object
	 * when the last thread has finished with it, i.e., when the last
	 * ActivationRecord is gone.
	 */

	action = BasicAction.Current();

	if ((action != null) && (action.status() == ActionStatus.RUNNING))
	{
	    /*
	     * Only check for top-level action. This is sufficient because
	     * activation records are propagated to the parent on nested
	     * transaction commit, and dropped when the (nested) action
	     * aborts. Thus, an object remains active as long as a single
	     * ActivationRecord is being used, and we don't need to create a
	     * new record for each transaction in the same hierarchy. Once
	     * activated, the object remains active until the action commits
	     * or aborts (at which time it may be passivated, and then
	     * reactivated later by the creation of a new ActivationRecord.)
	     */

	    synchronized (usingActions)
	    {
		if (usingActions.get(action.topLevelAction().get_uid()) == null)
		{
		    usingActions.put(action.topLevelAction().get_uid(), action.topLevelAction());
		    forceAR = true;
		}
	    }
	}

	if (forceAR || (currentStatus == ObjectStatus.PASSIVE) ||
	    (currentStatus == ObjectStatus.PASSIVE_NEW))
	{
	    /*
	     * If object is recoverable only, then no need to set up
	     * the object store.
	     */

	    if (loadObjectState())
	    {
		setupStore(rootName);
	    }

	    /* Only really activate if object is PASSIVE */
	    
	    if (currentStatus == ObjectStatus.PASSIVE)
	    {
		/*
		 * If the object is shared between different processes, then we
		 * must load the state each time a top-level action accesses
		 * it. Otherwise we can continue to use the last state as in
		 * Dharma/ArjunaII.
		 */

		if (loadObjectState())
		{
		    InputObjectState oldState = null;
		
		    try
		    {
			oldState = objectStore.read_committed(objectUid, type());
		    }
		    catch (ObjectStoreException e)
		    {
			oldState = null;
		    }

		    if (oldState != null)
		    {
			if ((result = restore_state(oldState, ObjectType.ANDPERSISTENT)))
			{
			    currentStatus = ObjectStatus.ACTIVE;
			}

			oldState = null;
		    }
		    else
		    {
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
			    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_2",
							new Object[] {objectUid, type()});
			}

			return false;
		    }
		}
		else
		{
		    if (currentStatus == ObjectStatus.PASSIVE_NEW)
			currentStatus = ObjectStatus.ACTIVE_NEW;
		    else
			currentStatus = ObjectStatus.ACTIVE;
		}
	    }
	    else
	    {
		if (currentStatus == ObjectStatus.PASSIVE_NEW)
		    currentStatus = ObjectStatus.ACTIVE_NEW;
		else
		    currentStatus = ObjectStatus.ACTIVE;
	    }

	    /*
	     * Create ActivationRecord if status changed Passive->Active or if
	     * object is a new persistent object.
	     */
	
	    if (forceAR || ((currentStatus == ObjectStatus.ACTIVE) ||
			    (currentStatus == ObjectStatus.PASSIVE_NEW)) && (action != null))
	    {
		int arStatus = AddOutcome.AR_ADDED;
		ActivationRecord ar = new ActivationRecord(oldStatus, this, action.topLevelAction());

		if ((arStatus = action.add(ar)) != AddOutcome.AR_ADDED)
		{
		    ar = null;

		    if (forceAR)
		    {
			synchronized (usingActions)
			{
			    usingActions.remove(action.topLevelAction().get_uid());
			}
		    }
		    
		    if (arStatus == AddOutcome.AR_REJECTED)
			result = false;
		}
		else
		{
		    /*
		     * We never reset activated, so we can optimise state
		     * loading/unloading in the case of SINGLE object model
		     */
		    
		    currentlyActivated = activated = true;
		}
	    }
	}
	
	return result;
    }
    
    /**
     * This operation deactivates a persistent object.
     * It behaves in a similar manner to the activate operation, but has an
     * extra argument which defines whether the object's state should be
     * committed or written as a shadow.
     *
     * The root of the object store is <code>null</code>. It is assumed
     * that this is being called during a transaction commit.
     *
     * @return <code>true</code> on success, <code>false</code> otherwise.
     */

public boolean deactivate ()
    {
	return deactivate(null, true);
    }

    /**
     * This operation deactivates a persistent object.
     * It behaves in a similar manner to the activate operation, but has an
     * extra argument which defines whether the object's state should be
     * commited now or not.
     *
     * It is assumed that this is being called during a transaction commit.
     *
     * @return <code>true</code> on success, <code>false</code> otherwise.
     */

public boolean deactivate (String rootName)
    {
	return deactivate(rootName, true);
    }

    /**
     * This operation deactivates a persistent object.
     * It behaves in a similar manner to the activate operation, but has an
     * extra argument which defines whether the object's state should be
     * commited now or not.
     *
     * @return <code>true</code> on success, <code>false</code> otherwise.
     */

public synchronized boolean deactivate (String rootName, boolean commit)
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_STATE_MAN, 
				     "StateManager::deactivate("+((rootName != null) ? rootName : "null")+", "+commit+") for object-id " +objectUid);
	}
	
	boolean result = false;

	if ((currentlyActivated  && (myType == ObjectType.ANDPERSISTENT)) || loadObjectState())
	{
	    setupStore(rootName);

	    if ((currentStatus == ObjectStatus.ACTIVE_NEW) || (currentStatus == ObjectStatus.ACTIVE))
	    {
		String tn = type();
		OutputObjectState newState = new OutputObjectState(objectUid, tn);

		/*
		 * Call save_state again to possibly get a persistent
		 * representation of the object.
		 */

		if (save_state(newState, myType))
		{
		    try
		    {
			if (commit)
			    result = objectStore.write_committed(objectUid, tn, newState);
			else
			    result = objectStore.write_uncommitted(objectUid, tn, newState);
		    }
		    catch (ObjectStoreException e)
		    {
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_3",e);
			
			result = false;
		    }
		}
		else
		{
		    if (tsLogger.arjLoggerI18N.isWarnEnabled())
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_4");
		}
		
		/*
		 * Not needed any more because activation record does this when
		 * all actions are forgotten.
		 *
		 *  if (result)
		 *      currentStatus = ObjectStatus.PASSIVE;
		 *
		 */
	    }
	}
	else
	{
	    result = true;
	}
	
	return result;
    }

    /**
     * @return the object's current status (active, passive, ...)
     */

public synchronized int status ()
    {
	return currentStatus;
    }

    /**
     * @return the type of the object (persistent, recoverable, ...)
     */

public synchronized int ObjectType ()
    {
	return myType;
    }

    /**
     * @return the object's unique identifier.
     */

public final Uid get_uid ()
    {
	return objectUid;
    }
    
    /**
     * Destroy the object (e.g., remove its state from the persistent
     * store.)
     *
     * Calls to destroy for volatile objects (ones not maintained within the
     * volatile object store) are ignored, and FALSE is returned.
     *
     * @return <code>true</code> on success, <code>false</code> otherwise.
     */
    
public synchronized boolean destroy ()
    {
	if (tsLogger.arjLoggerI18N.debugAllowed())
	{
	    tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					 FacilityCode.FAC_STATE_MAN, 
					 "com.arjuna.ats.arjuna.StateManager_5", new Object[]{objectUid});
	}

	boolean result = false;
    
	if (objectStore != null)
	{
	    BasicAction action = BasicAction.Current();
	
	    if (action != null)  // add will fail if the status is wrong!
	    {
		DisposeRecord dr = new DisposeRecord(objectStore, this);

		if (action.add(dr) != AddOutcome.AR_ADDED)
		{
		    dr = null;

		    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		    {
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_6");
		    }
		}
		else
		    result = true;
	    }
	    else
	    {
		try
		{
		    result = objectStore.remove_committed(get_uid(), type());

		    /*
		     * Once destroyed, we can never use the object again.
		     */

		    if (result)
			destroyed();
		}
		catch (Exception e)
		{
		    if (tsLogger.arjLoggerI18N.isWarnEnabled())
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_7", e);

		    result = false;
		}
	    }
	}
	else
	{
	    /*
	     * Not a persistent object!
	     */
	    
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_8");
	    }
	}
	
	return result;
    }

    /**
     * The following function disables recovery for an object by setting
     * the ObjectType to NEITHER (RECOVERABLE or ANDPERSISTENT).
     * The value of this variable is checked in the modified operation
     * so that no recovery information is created if myType is set to NEITHER.
     */

public synchronized void disable ()
    {
	myType = ObjectType.NEITHER;
    }

    /**
     * Print out information about the object.
     */

public void print (PrintWriter strm)
    {
	strm.println("Uid: "+objectUid);
	strm.println("Type: "+type());
    }

    /**
     * The object's type. Derived classes should override this to
     * reflect their type structure. Typically this string is used for
     * locating the object state in an object store, and reflects the
     * hierarchy structure of the object.
     */

    public String type ()
    {
	return "/StateManager";
    }

    /**
     * @return the root of the object store this instance will
     * use if it has to save the state of the object to storage.
     */

    public final String getStoreRoot ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_STATE_MAN, "StateManager::getStoreRoot ()");
	}
	
	return storeRoot;
    }

    /**
     * @return the object store this instance will used if it has
     * to save the state of the object to storage.
     */

    public ObjectStore getStore ()
    {
        if (tsLogger.arjLogger.debugAllowed())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                         FacilityCode.FAC_STATE_MAN, "StateManager::getStore ()");
        }

        if (objectStore == null)
            setupStore();

        return objectStore;
    }

    /**
     * @return object specific attributes (e.g., object store type).
     */

public Object attributes ()
    {
	return smAttributes;
    }

    /**
     * Pack the necessary information for crash recovery.
     *
     * @since JTS 2.1.
     */

protected void packHeader (OutputObjectState os, Uid txId,
			   Uid processUid) throws IOException
    {
	/*
	 * If there is a transaction present than pack the process Uid of
	 * this JVM and the tx id. Otherwise pack a null Uid.
	 */

	try
	{
	    // pack the marker first.

	    os.packString(StateManager.marker);
		
	    /*
	     * Only pack something if there is a transaction. Otherwise
	     * the application is driving this object manually, and all
	     * bets are off!
	     */

	    if (txId != null)
	    {
		txId.pack(os);
		processUid.pack(os);
	    }
	    else
		Uid.nullUid().pack(os);
	}
	catch (IOException ex)
	{
	    throw ex;
	}
	catch (Exception e)
	{
	    throw new IOException(e.toString());
	}
    }

    /**
     * Unpack the crash recovery state header information and return it.
     *
     * @since JTS 2.1.
     * @param txId the identity of the transaction that last caused the
     * state to be written to the object store.
     * @return the <code>Uid</code> of the process that last managed this
     * state.
     */

protected void unpackHeader (InputObjectState os, Uid txId,
			     Uid processUid) throws IOException
    {
	try
	{
	    String myState = os.unpackString();
		
	    if (myState.equals(StateManager.marker))
	    {
		txId.unpack(os);

		/*
		 * Is there going to be a Uid to unpack?
		 */
		
		if (!txId.equals(Uid.nullUid()))
		    processUid.unpack(os);
	    }
	    else
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_9");
		}
		
		throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.StateManager_15"));
	    }
	}
	catch (IOException ex)
	{
	    throw ex;
	}
	catch (Exception e)
	{
	    throw new IOException(e.toString());
	}
    }

    /**
     * The following function checks to see if the object is going out of
     * scope while an action is still running.
     */

    protected void terminate ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_STATE_MAN, 
				     "StateManager::terminate() for object-id "+get_uid());
	}
	
	cleanup(true);
    }

    protected final synchronized void setStatus (int s)
    {
	currentStatus = s;
    }
    
    /**
     * Create object with specific uid. This constructor
     * is primarily used when recreating an existing object. The object type
     * is set to 'ANDPERSISTENT' this is equivalent to invoking
     * persist in the object constructor.
     */

protected StateManager (Uid objUid)
    {
	this(objUid, ObjectType.ANDPERSISTENT, null);
    }

protected StateManager (Uid objUid, ObjectName attr)
    {
	this(objUid, ObjectType.ANDPERSISTENT, attr);
    }
    
protected StateManager (Uid objUid, int ot)
    {
	this(objUid, ot, null);
    }

protected StateManager (Uid objUid, int ot, ObjectName objName)
    {
	objectName = objName;
	
	parseObjectName();

	if (ot == ObjectType.NEITHER)
	{
	    modifyingActions = null;
	    usingActions = null;
	}
	else
	{
	    modifyingActions = new Hashtable();
	    usingActions = new Hashtable();
	}
	
	activated = false;
	currentlyActivated = false;
	currentStatus = ObjectStatus.PASSIVE;
	initialStatus = ObjectStatus.PASSIVE;
	myType = ot;
	objectStore = null;
	storeRoot = null;

	objectUid = objUid;

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_STATE_MAN, "StateManager::StateManager( "+get_uid()+" )");
	}
    }    

protected StateManager ()
    {
	this(ObjectType.RECOVERABLE, null);
    }
    
protected StateManager (int ot)
    {
	this(ot, null);
    }

protected StateManager (int ot, ObjectName objName)
    {
	objectName = objName;
	
	parseObjectName();
	
	if (ot == ObjectType.NEITHER)
	{
	    modifyingActions = null;
	    usingActions = null;
	}
	else
	{
	    modifyingActions = new Hashtable();
	    usingActions = new Hashtable();
	}

	activated = false;
	currentlyActivated = false;
	currentStatus = (((smAttributes.objectModel == ObjectModel.SINGLE) && (ot == ObjectType.RECOVERABLE)) ? ObjectStatus.ACTIVE : ObjectStatus.PASSIVE_NEW);
	initialStatus = currentStatus;
	myType = ot;
	objectStore = null;
	storeRoot = null;

	objectUid = new Uid();

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_STATE_MAN, "StateManager::StateManager( "+ot+" )");
	}
    }

protected StateManager (ObjectName objName)
    {
	objectName = objName;
	
	parseObjectName();

	if (myType == ObjectType.NEITHER)
	{
	    modifyingActions = null;
	    usingActions = null;
	}
	else
	{
	    modifyingActions = new Hashtable();
	    usingActions = new Hashtable();
	}
	
	activated = false;
	currentlyActivated = false;
	currentStatus = ObjectStatus.PASSIVE;
	initialStatus = ObjectStatus.PASSIVE;
	objectStore = null;
	storeRoot = null;

	if (objectUid == null)
	    objectUid = new Uid();

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_STATE_MAN, "StateManager::StateManager( "+objName+" )");
	}
    }
    
    /*
     * Protected non-virtual functions.
     */
    
    /**
     * The object's state is about to be modified, and StateManager should
     * take a snapshot of the state if the object is being used within
     * a transaction.
     *
     * @return <code>true</code> on success, <code>false</code> otherwise.
     */

    protected synchronized boolean modified ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_STATE_MAN, "StateManager::modified() for object-id "+get_uid());
	}
	
	BasicAction action = BasicAction.Current();
	RecoveryRecord record = null;
	
	if ((myType == ObjectType.NEITHER) || (currentStatus == ObjectStatus.DESTROYED)) /*  NEITHER => no recovery info */
	{
	    return true;
	}
    
	if (currentStatus == ObjectStatus.PASSIVE)
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_10");
	    activate();
	}
	
	/*
	 * Need not have gone through active if new object.
	 */

	if (currentStatus == ObjectStatus.PASSIVE_NEW)
	    currentStatus = ObjectStatus.ACTIVE_NEW;
    
	if (action != null)
	{
	    /*
	     * Check if this is the first call to modified in this action.
	     * BasicList insert returns FALSE if the entry is already
	     * present.
	     */

	    synchronized (modifyingActions)
	    {
		if ((modifyingActions.size() > 0) &&
		    (modifyingActions.get(action.get_uid()) != null))
		{
		    return true;
		}
		else
		    modifyingActions.put(action.get_uid(), action);
	    }
	
	    /* If here then its a new action */
	
	    OutputObjectState state = new OutputObjectState(objectUid, type());
	    int rStatus = AddOutcome.AR_ADDED;
	
	    if (save_state(state, ObjectType.RECOVERABLE))
	    {
		if ((myType == ObjectType.RECOVERABLE) && (smAttributes.objectModel == ObjectModel.SINGLE))
		{
		    record = new RecoveryRecord(state, this);
		}
		else
		    record = new PersistenceRecord(state, objectStore, this);
	    
		if ((rStatus = action.add(record)) != AddOutcome.AR_ADDED)
		{
		    synchronized(modifyingActions)
		    {
			modifyingActions.remove(action.get_uid());  // remember to unregister with action
		    }
		    
		    record = null;

		    return false;
		}
	    }
	    else
		return false;
	}
	
	return true;
    }

    /**
     * The persist function changes the type of the object from RECOVERABLE
     * to ANDPERSISTENT.
     *
     * No changes are made unless the status of the object is ACTIVE, so it
     * is not possible to change the type of the object if it has been
     * modified.
     */

protected final synchronized void persist ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_STATE_MAN, "StateManager::persist() for object-id "+get_uid());
	}
     
	if (currentStatus == ObjectStatus.ACTIVE)
	{
	    currentStatus = ObjectStatus.PASSIVE_NEW;
	    myType = ObjectType.ANDPERSISTENT;
	}
    }
    
    /**
     * Object cleanup.
     * Attempt sane cleanup when object is deleted. Handle perverse cases
     * where multiple actions are still active as object dies.
     *
     * @param fromTerminate indicates whether this method is being called
     * from the <code>terminate</code> method, or from elsewhere.
     * @see StateManager#terminate
     */
    
protected final synchronized void cleanup (boolean fromTerminate)
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_STATE_MAN, "StateManager::cleanup() for object-id "+get_uid());
	}
	
	if (myType == ObjectType.NEITHER)
	    return;

	BasicAction action = null;

	synchronized (usingActions)
	{
	    if (usingActions != null)
	    {
		Enumeration e = usingActions.keys();
	    
		while (e.hasMoreElements())
		{
		    action = (BasicAction) usingActions.remove(e.nextElement());
		    
		    if (action != null)
		    {
			/*
			 * Pop actions off using list.
			 * 
			 * Don't check if action is running below so that
			 * cadavers can be created in commit protocol too.
			 */

			AbstractRecord record = null;
			int rStatus = AddOutcome.AR_ADDED;

			if ((currentStatus == ObjectStatus.ACTIVE_NEW) ||
			    (currentStatus == ObjectStatus.ACTIVE))
			{
			    OutputObjectState state = null;

			    if (tsLogger.arjLoggerI18N.isWarnEnabled())
			    {
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_11",
							    new Object[]{objectUid, type()});
			    }

			    /*
			     * If we get here via terminate its ok to do
			     * a save_state.
			     */
	    
			    if (fromTerminate)
			    {
				state = new OutputObjectState(objectUid, type());
		
				if (!save_state(state, myType))
				{
				    if (tsLogger.arjLoggerI18N.isWarnEnabled())
					tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_12");
				    /* force action abort */
				    
				    action.preventCommit();
				}
			    }
			    else
			    {
				/* otherwise force action abort */
				
				action.preventCommit();
			    }
			    
			    /*
			     * This should be unnecessary - but just in
			     * case.
			     */
	    
			    setupStore(storeRoot);
				
			    record = new CadaverRecord(state, objectStore, this);
			
			    if ((rStatus = action.add(record)) != AddOutcome.AR_ADDED)
				record = null;
			}

			if (currentlyActivated && (currentStatus != ObjectStatus.DESTROYED))
			{
			    record = new CadaverActivationRecord(this);
			    
			    if ((rStatus = action.add(record)) == AddOutcome.AR_ADDED)
			    {
				currentStatus = ObjectStatus.PASSIVE;
			    }
			    else
				record = null;
			}
		    }
		}
	    }
	}
	
	/*
	 * Here the object must be either RECOVERABLE or PERSISTENT.
	 * Whether or not an action exists we still need to reset the
	 * object status to avoid possible later confusion
	 * What it gets set to is not important really as long as it gets
	 * changed from ACTIVE_NEW which might cause any running action to
	 * abort.
	 */
    
	if (currentStatus == ObjectStatus.ACTIVE_NEW)
	{
	    if ((myType == ObjectType.RECOVERABLE) && (smAttributes.objectModel == ObjectModel.SINGLE))
	    {
		currentStatus = ObjectStatus.ACTIVE;
	    }
	    else
	    {
		currentStatus = ObjectStatus.PASSIVE;
	    }
	}

	currentlyActivated = false;
    }

    /**
     * Make sure the object store is set up, if required.
     * The root of the object store is assumed to be <code>null</code>.
     */

protected final void setupStore ()
    {
	setupStore(null);
    }

    /**
     * Make sure the object store is set up, if required.
     *
     * @param rootName indicates the root of the object store.
     */
    
protected synchronized void setupStore (String rootName)
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_STATE_MAN, 
				     "StateManager::setupStore ( "+((rootName != null) ? rootName : "null")+" )");
	}
	
	if (!loadObjectState())
	    return;
	
	/*
	 * Already setup?
	 * Assume type will not change once object is created.
	 */

	if (objectStore != null)
	    return;

	if (rootName == null)
	{
	    if ( smAttributes.objectStoreRoot != null )
	    {
		rootName = smAttributes.objectStoreRoot;
	    }
	    else
	    {
		rootName = "";
	    }
	}

	/* Check if we have a store */

	if (storeRoot != null)
	{
	    /* Attempting to reuse it ? */

	    if ((rootName == null) || (rootName.compareTo("") == 0) ||
		(rootName.compareTo(storeRoot) == 0))
	    {
		return;
	    }

	    /* No - destroy old store and create new */
	    
	    objectStore = null;
	}

	if (rootName == null)
	{
	    rootName = "";
	}

	/* Create store now */

	storeRoot = new String(rootName);

	if ((myType == ObjectType.ANDPERSISTENT) || (myType == ObjectType.NEITHER))
	{
	    /*
	     * If null, default object store type is obtained by the
	     * interface.
	     */

	    int sharedStatus = ((smAttributes.objectModel == ObjectModel.SINGLE) ? ObjectStore.OS_UNSHARED : ObjectStore.OS_SHARED);
	    ObjectName osObjName = null;
	    
	    if (objectName != null)
	    {
		try
		{
		    osObjName = objectName.getObjectNameAttribute(ArjunaNames.ObjectStore_implementationObjectName());
		}
		catch (Exception ex)
		{
		    osObjName = null;
		}
	    }

	    if (osObjName == null)
	    {
		objectStore = new ObjectStore(smAttributes.objectStoreType, storeRoot, sharedStatus);
	    }
	    else
	    {
		objectStore = new ObjectStore(smAttributes.objectStoreType, osObjName);
	    }
	}
	else
	{
	    /*
	     * Currently we should never get here!
	     * However, since Arjuna supports a volatile (in memory)
	     * object store we will also eventually, probably through a set
	     * of native methods.
	     */

	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.StateManager_13");
	    
	    throw new FatalError(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.StateManager_14"));
		
	    //		objectStore = new ObjectStore(ArjunaNames.Implementation_ObjectStore_VolatileStore(), storeRoot);
	}

	/*
	 * Do any work needed to initialise the object store.
	 * Really only makes sense for replicated object store where
	 * we attempt to do early binding to the replicas.
	 */

	objectStore.initialise(get_uid(), type());
    }

    /**
     * Do we need to load the object's state?
     *
     * @return <code>true</code> if the object state should be loaded,
     * <code>false</code> otherwise.
     */
    
protected final boolean loadObjectState ()
    {
	boolean load = (smAttributes.objectModel != ObjectModel.SINGLE);
    
	/*
	 * MULTIPLE object model requires loading of state every
	 * time, even if we are RECOVERABLE - we use the volatile
	 * store.
	 */

	if (!load)
	{
	    /*
	     * Must be SINGLE object model. So, is this the first
	     * time? If so, load state.
	     */
		
	    if ((myType != ObjectType.RECOVERABLE) && (!activated))
		load = true;
	}

	return load;
    }
    
    /*
     * Called ONLY by ActivationRecords!
     */

    /**
     * Remove action from list of using actions. If the action list empties
     * reset state to PASSIVE.
     * The second param tells why the action should be forgotten.
     * This aids in resetting the state correctly.
     */
      
protected final synchronized boolean forgetAction (BasicAction action,
						   boolean committed,
						   int recordType)
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_STATE_MAN, "StateManager::forgetAction("
				     +((action != null) ? action.get_uid() : Uid.nullUid())+")"
				     +" for object-id "+objectUid);
	}
	
	synchronized (modifyingActions)
	{
	    modifyingActions.remove(action.get_uid());
	}

	if (recordType != RecordType.RECOVERY)
	{
	    synchronized (usingActions)
	    {
		if (usingActions != null)
		{
		    usingActions.remove(action.get_uid());
	
		    if (usingActions.size() == 0)
		    {
			if (committed)
			{
			    if ((myType == ObjectType.RECOVERABLE) && (smAttributes.objectModel == ObjectModel.SINGLE))
			    {
				initialStatus = currentStatus = ObjectStatus.ACTIVE;
			    }
			    else
			    {
				initialStatus = currentStatus = ObjectStatus.PASSIVE;
			    }
			}
			else
			    currentStatus = initialStatus;
		    }
		}
	    }
	}

	return true;
    }

    /**
     * Remember that the specified transaction is using the object.
     */

protected final synchronized boolean rememberAction (BasicAction action, int recordType)
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_STATE_MAN, "StateManager::rememberAction("
				     +((action != null) ? action.get_uid() : Uid.nullUid())+")"
				     +" for object-id "+objectUid);
	}
	
	boolean result = false;

	if (recordType != RecordType.RECOVERY)
	{
	    if ((action != null) && (action.status() == ActionStatus.RUNNING))
	    {
		synchronized (usingActions)
		{
		    if (usingActions.get(action.get_uid()) == null)
			usingActions.put(action.get_uid(), action);
		}
	    }
	}

	result = true;

	return result;
    }

    /**
     * @return the mutex object used to lock this object.
     *
     * @since JTS 2.1.
     */

protected final Mutex getMutex ()
    {
	return mutex;
    }

    /**
     * @return the result of the attempt to lock this object.
     *
     * @since JTS 2.1.
     */

protected final boolean lockMutex ()
    {
	if (mutex.lock() == Mutex.LOCKED)
	    return true;
	else
	    return false;
    }

    /**
     * @return the result of the attempt to unlock this object.
     *
     * @since JTS 2.1.
     */

protected final boolean unlockMutex ()
    {
	if (mutex.unlock() == Mutex.UNLOCKED)
	    return true;
	else
	    return false;
    }
    
    /**
     * @return <code>true</code> if the object was locked,
     * <code>false</code> if the attempt would cause the thread to block.
     *
     * @since JTS 2.1.
     */

protected final boolean tryLockMutex ()
    {
	if (mutex.tryLock() == Mutex.LOCKED)
	    return true;
	else
	    return false;
    }

    /*
     * Package scope.
     */

    /*
     * Set the status of the object to destroyed so that we can no
     * longer use it.
     */

synchronized final void destroyed ()
    {
	currentStatus = ObjectStatus.DESTROYED;
    }

    /*
     * Private functions
     */

private void parseObjectName ()
    {
	smAttributes = new StateManagerAttribute();

	if (objectName != null)
	{
	    try
	    {
		objectUid = objectName.getUidAttribute(ArjunaNames.StateManager_uid());
	    }
	    catch (Exception e)
	    {
		// assume not present

		objectUid = null;
	    }

	    try
	    {
		myType = (int) objectName.getLongAttribute(ArjunaNames.StateManager_objectType());
	    }
	    catch (Exception e)
	    {
		myType = ObjectType.ANDPERSISTENT;
	    }

	    try
	    {
		smAttributes.objectModel = (int) objectName.getLongAttribute(ArjunaNames.StateManager_objectModel());
	    }
	    catch (Exception e)
	    {
		// assume not present.
	    }

	    try
	    {
		/*
		 * For uniformity use the same attribute name as the
		 * environment name.
		 */
		smAttributes.objectStoreType = objectName.getClassNameAttribute(com.arjuna.ats.arjuna.common.Environment.OBJECTSTORE_TYPE);
	    }
	    catch (Exception e)
	    {
		// assume not present.
	    }

        try
        {
            smAttributes.objectStoreRoot = objectName.getStringAttribute(ArjunaNames.StateManager_objectStoreRoot());
        }
        catch (Exception e)
        {
        // assume not present.
        }
	}
	else
	{
	    objectUid = null;
	}
    }

protected StateManagerAttribute smAttributes;
protected ObjectName	        objectName;
protected Hashtable	        modifyingActions;
protected Hashtable	        usingActions;
protected Uid	                objectUid;

private boolean     activated;
private boolean     currentlyActivated;
private int	    currentStatus;
private int	    initialStatus;    
private int	    myType;
private ObjectStore objectStore;
private String      storeRoot;
private Mutex       mutex = new Mutex();
    
private static final String marker = "#ARJUNA#";


}
