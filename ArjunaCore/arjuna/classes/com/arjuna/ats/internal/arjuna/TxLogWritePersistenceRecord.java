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
 * Copyright (C) 2005,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: TxLogWritePersistenceRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.arjuna.PersistenceRecord;
import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.exceptions.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;

/**
 */

public class TxLogWritePersistenceRecord extends PersistenceRecord
{

    public TxLogWritePersistenceRecord (OutputObjectState state, ObjectStore store, StateManager sm)
    {
	super(state, store, sm);
    }
    
    public int typeIs ()
    {
	return RecordType.TXLOG_PERSISTENCE;
    }
    
    public ClassName className ()
    {
	return ArjunaNames.Implementation_AbstractRecord_TxLogPersistenceRecord();
    }

    /**
     * commit the state saved during the prepare phase.
     */

    public int topLevelCommit ()
    {
	boolean result = false;
	LogWriteStateManager sm = null;
	boolean writeToLog = true;
	
	try
	{
	    sm = (LogWriteStateManager) super.objectAddr;

	    writeToLog = sm.writeOptimisation();
	}
	catch (ClassCastException ex)
	{
	    writeToLog = false;
	}
	
	if (store != null)
	{
	    try
	    {
		if (shadowMade)
		{
		    result = store.commit_state(order(), super.getTypeOfObject());
			    
		    if (!result)
		    {
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
			    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_2", new Object[] {order()});
			}
		    }
		}
		else
		{
		    if (topLevelState != null)
		    {
			if (!writeToLog)
			    result = store.write_committed(order(), super.getTypeOfObject(), topLevelState);
			else
			    result = true;
		    }
		}
	    }
	    catch (ObjectStoreException e)
	    {
		result = false;
	    }
	}
	else
	{
	}
	
	if (!result)
	{
	}
	
	super.forgetAction(true);
	
	return ((result) ? TwoPhaseOutcome.FINISH_OK : TwoPhaseOutcome.FINISH_ERROR);
    }
	
    /**
     * topLevelPrepare attempts to save the object.
     * It will either do this in the action intention list or directly
     * in the object store by using the 'deactivate' function of the object
     * depending upon the size of the state.
     * To ensure that objects are correctly hidden while they are in an
     * uncommitted state if we use the abbreviated protocol then we write an
     * EMPTY object state as the shadow state - THIS MUST NOT BE COMMITTED.
     * Instead we write_committed the one saved in the intention list.
     * If the store cannot cope with being given an empty state we revert to
     * the old protocol.
     */

    public int topLevelPrepare ()
    {
	int result = TwoPhaseOutcome.PREPARE_NOTOK;
	StateManager sm = super.objectAddr;
	LogWriteStateManager lwsm = null;
	boolean writeToLog = true;

	try
	{
	    lwsm = (LogWriteStateManager) sm;
	    
	    writeToLog = lwsm.writeOptimisation();
	}
	catch (ClassCastException ex)
	{
	    writeToLog = false;
	}
	
	if ((sm != null) && (store != null))
	{
	    topLevelState = new OutputObjectState(sm.get_uid(), sm.type());
	    
	    if (writeToLog || (!store.fullCommitNeeded() &&
			       (sm.save_state(topLevelState, ObjectType.ANDPERSISTENT)) &&
			       (topLevelState.size() <= PersistenceRecord.MAX_OBJECT_SIZE)))
	    {
		if (PersistenceRecord.classicPrepare)
		{
		    OutputObjectState dummy = new OutputObjectState(Uid.nullUid(), null);

		    /*
		     * Write an empty shadow state to the store to indicate
		     * one exists, and to prevent bogus activation in the case
		     * where crash recovery hasn't run yet.
		     */
		    
		    try
		    {
			store.write_uncommitted(sm.get_uid(), sm.type(), dummy);
			result = TwoPhaseOutcome.PREPARE_OK;
		    }
		    catch (ObjectStoreException e)
		    {
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_21",e);
		    }
		
		    dummy = null;
		}
		else
	        {
		    result = TwoPhaseOutcome.PREPARE_OK;
		}
	    }
	    else
	    {
		if (sm.deactivate(store.getStoreName(), false))
		{
 		    shadowMade = true;
		    
		    result = TwoPhaseOutcome.PREPARE_OK;
		}
		else
		{
		    if (tsLogger.arjLoggerI18N.isWarnEnabled())
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_7");
		}
	    }
	}
	else
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.PersistenceRecord_8");
	}

	return result;
    }

    public String type ()
    {
	return "/StateManager/AbstractRecord/RecoveryRecord/PersistenceRecord/TxLogPersistenceRecord";
    }

    public static AbstractRecord create ()
    {
	return new TxLogWritePersistenceRecord();
    }

    protected TxLogWritePersistenceRecord ()
    {
	super();
    }

}
