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
 * $Id: CadaverRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import java.io.PrintWriter;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

/**
 * Cadaver records are created whenever a persistent object is deleted while
 * still in the scope of an atomic action. This ensures that if the
 * action commits the state of the persistent objects gets properly
 * reflected back in the object store. For objects that are only
 * recoverable such work is unnecessary. Cadaver records replace
 * PersistenceRecords in the record list of an atomic action so they must
 * be merged with such records to enable both commits and aborts to occur.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: CadaverRecord.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class CadaverRecord extends PersistenceRecord
{

    /**
     * Create a new instance, passing in the object that is being managed.
     *
     * @param OutputObjectState os the state of the object that is being
     * removed.
     * @ObjectStore objStore the object store instance used to manipulate the
     * persistent state.
     * @param StateManager sm the object being removed.
     */

    public CadaverRecord (OutputObjectState os, ObjectStore objStore,
			  StateManager sm)
    {
	super(os, objStore, sm);
	
	newStateIsValid = ((os != null) ? true : false);
	oldState = null;
	oType = RecordType.NONE_RECORD;
	store = objStore;  // implicit ref count in Java
	
	if (store != null)
	{
	    /*
	     * If the object goes out of scope its object store may
	     * be inaccessable - increase reference count to compensate
	     */
		
	    /*
	     * Don't need this in Java.
	     */
		
	    //	    ObjectStore.reference(store);
	}

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
				     FacilityCode.FAC_ABSTRACT_REC, 
				     "CadaverRecord::CadaverRecord("+os+", "+sm.get_uid()+")");
	}
    }

    /**
     * Tidy-up the instance.
     */

    public void finalize ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "CadaverRecord::finalize() for "+order());
	}
	
	oldState = null;
	store = null;
	
	super.finalize();
    }

    /**
     * Override default AbstractRecord method. CadaverRecords are propagated
     * regardless of the termination condition.
     *
     * @return <code>true</code>
     */

    public boolean propagateOnAbort ()
    {
	return true;
    }

    /**
     * The type of the record.
     *
     * @return RecordType.PERSISTENT
     * @see com.arjuna.ats.arjuna.coordinator.RecordType
     */

    public int typeIs ()
    {
	return RecordType.PERSISTENCE;
    }
    
    /**
     * The ClassName representation of this class.
     */

    public ClassName className ()
    {
	return ArjunaNames.Implementation_AbstractRecord_CadaverRecord();
    }
    
    /**
     * The nested transaction has aborted. The record will invalidate any
     * new state.
     *
     * @message com.arjuna.ats.arjuna.CadaverRecord_1 [com.arjuna.ats.arjuna.CadaverRecord_1] Attempted abort operation on deleted object id {0} of type {1} ignored
     */

    public int nestedAbort ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "CadaverRecord::nestedAbort() for "+order());
	}
	
	if (oldState != null)
	    newStateIsValid = false;
	
	if (oType == RecordType.RECOVERY)
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.CadaverRecord_1",
					    new Object[]{ order(), getTypeOfObject() } );
	    }
	}
	
	/*
	 * No need to forget the action since this object is
	 * being deleted so it is unlikely to have modified called
	 * on it!
	 */
	
	//	super.forgetAction(false);
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    /**
     * The nested transaction is preparing. If there is any new state for
     * the object being removed, and that state is valid, then this record
     * will call nestedPrepare on the object being removed.
     *
     * If we have no new state then we cannot commit and must force an
     * abort. Do this by failing the prepare phase.
     */
    
    public int nestedPrepare ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "CadaverRecord::nestedPrepare() for "+order());
	}
	
	if (newStateIsValid)
	    return super.nestedPrepare();
	else
	    return TwoPhaseOutcome.PREPARE_NOTOK;
    }

    /**
     * The nested transaction has aborted. Invalidate any new state.
     */

    public int topLevelAbort ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "CadaverRecord::topLevelAbort() for "+order());
	}
	
	newStateIsValid = false;
	
	if (oType == RecordType.RECOVERY)
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.CadaverRecord_1",
					    new Object[]{ order(), getTypeOfObject() } );
	    }
	}
	
	// super.forgetAction(false);
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    /**
     * At topLevelCommit we commit the uncommitted version already saved
     * into object store.
     * Cannot use inherited version since that assumes object is alive
     * instead talk directly to the object store itself.
     */
    
    public int topLevelCommit ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "CadaverRecord::topLevelCommit() for "+order());
	}
	
	boolean res = true;
	OutputObjectState oState = super.state;
	
	if ((oState != null) && (oType == RecordType.PERSISTENCE))
	{
	    if (store == null)
		return TwoPhaseOutcome.FINISH_ERROR;
		
	    try
	    {
		res = store.commit_state(oState.stateUid(), oState.type());
	    }
	    catch (ObjectStoreException e)
	    {
		res = false;
	    }
	}
	
	// super.forgetAction(false);
	
	return ((res) ? TwoPhaseOutcome.FINISH_OK : TwoPhaseOutcome.FINISH_ERROR);
    }
    
    /**
     * At topLevelPrepare write uncommitted version into object store.
     * Cannot use inherited version since that assumes object is alive
     * instead talk directly to the object store itself.
     */
    
    public int topLevelPrepare ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "CadaverRecord::topLevelPrepare() for "+order());
	}
	
	int tlpOk = TwoPhaseOutcome.PREPARE_NOTOK;
	OutputObjectState oState = (newStateIsValid ? super.state : oldState);
	
	if (oState != null)
	{
	    if (oType == RecordType.PERSISTENCE)
	    {
		if (store == null)
		    return TwoPhaseOutcome.PREPARE_NOTOK;

		try
		{
		    if (store.write_uncommitted(oState.stateUid(), oState.type(), oState))
		    {
			if (shadowForced())
			    tlpOk = TwoPhaseOutcome.PREPARE_OK;
		    }
		}
		catch (ObjectStoreException e)
		{
		}
	    }
	    else
		tlpOk = TwoPhaseOutcome.PREPARE_OK;
	}
    
	return tlpOk;
    }

    /**
     * Override AbstractRecord.print to write specific information to
     * the specified stream.
     *
     * @param PrintWriter strm the stream to use.
     */

    public void print (PrintWriter strm)
    {
	strm.println("Cadaver for:");
	super.print(strm);
    }

    /**
     * The type of the class - may be used to save information in an
     * hierarchical manner in the object store.
     */

    public String type()
    {
	return "/StateManager/AbstractRecord/RecoveryRecord/PersistenceRecord/CadaverRecord";
    }

    /**
     * Override the AbstractRecord.doSave.
     *
     * @return <code>true</code> if the object being removed is a persistent
     * object (RecordType.PERSISTENT). <code>false</code> otherwise.
     * @see com.arjuna.ats.arjuna.coordinator.RecordType
     */

    public boolean doSave ()
    {
	if (oType == RecordType.PERSISTENCE)
	    return true;
	else
	    return false;
    }

    /**
     * Create a new instance of the CadaverRecord, using the default
     * constructor. Have to return as a AbstractStore because of
     * inheritence.
     *
     * @return a new CadaverRecord.
     */
    
    public static AbstractRecord create ()
    {
	return new CadaverRecord();
    }
    
    /**
     * merge takes the information from the incoming PersistenceRecord and
     * uses it to initialise the oldState information. This is required
     * for processing of action aborts since CadaverRecords maintain the
     * final state of an object normally - which is required if the action
     * commits.
     *
     * @param AbstractRecord mergewith The record to merge with.
     */
 
    public void merge (AbstractRecord mergewith)
    {
	/*
	 *  Following assumes that value returns a pointer to the 
	 *  old state maintained in the PersistenceRecord (as an ObjectState).
	 *  Here we create a copy of that state allowing the original
	 *  to be deleted
	 */

	oType = mergewith.typeIs();
    
	if (oldState != null)
	{
	    if (newStateIsValid)
	    {
		oldState = null;
	    }
	    else
	    { 
		setValue(oldState);
		newStateIsValid = true;
	    }
	}
    
	oldState = new OutputObjectState((OutputObjectState)(mergewith.value()));
    }

    /**
     * Overrides AbstractRecord.shouldMerge
     *
     * @param AbstractRecord ar the record to potentially merge with.
     *
     * @return <code>true</code> if this instance and the parameter have the
     * same id (order()) and the parameter is either persistent or recoverable.
     * <code>false</code> otherwise.
     * @see com.arjuna.ats.arjuna.coordinator.RecordType
     */

    public boolean shouldMerge (AbstractRecord ar)
    {
	return (((order().equals(ar.order())) &&
		 ((ar.typeIs() == RecordType.PERSISTENCE) ||
		  (ar.typeIs() == RecordType.RECOVERY)))
		? true : false);
    }

    /**
     * Overrides AbstractRecord.shouldReplace
     *
     * @param AbstractRecord ar the record to potentially replace this
     * instance.
     *
     * @return <code>true</code> if this instance and the parameter have the
     * same id (order()) and the parameter is either persistent or recoverable.
     * <code>false</code> otherwise.
     * @see com.arjuna.ats.arjuna.coordinator.RecordType
     */
    
    public boolean shouldReplace (AbstractRecord ar)
    {
	return (((order().equals(ar.order())) &&
		 ((ar.typeIs() == RecordType.PERSISTENCE) ||
		  (ar.typeIs() == RecordType.RECOVERY)))
		? true : false);
    }

    /**
     * Create a new instance using default values. Typically used during
     * failure recovery.
     */

    protected CadaverRecord ()
    {
	super();

	newStateIsValid = false;
	oldState = null;
	oType = RecordType.NONE_RECORD;
	store = null;

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_ABSTRACT_REC, "CadaverRecord::CadaverRecord ()");
	}
    }
    
    private boolean           newStateIsValid;
    private OutputObjectState oldState;
    private int               oType;
    private ObjectStore       store;
    
}
