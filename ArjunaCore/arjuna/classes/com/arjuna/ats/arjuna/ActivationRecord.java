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
 * $Id: ActivationRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import java.io.PrintWriter;

import com.arjuna.common.util.logging.*;

/*
 * Default visibility.
 */

class ActivationRecord extends AbstractRecord
{

    /*
     * This constructor is used to create a new instance of an
     * ActivationRecord.
     */
    
    public ActivationRecord (int st, StateManager sm, BasicAction action)
    {
	super(sm.get_uid(), sm.type(), ObjectType.ANDPERSISTENT);
	
	objectAddr = sm;
	actionHandle = action;
	state = st;

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
				     FacilityCode.FAC_ABSTRACT_REC, 
				     "ActivationRecord::ActivationRecord("+state+", "+sm.get_uid()+")");
	}
    }
    
    public int typeIs ()
    {
	return RecordType.ACTIVATION;
    }
    
    public Object value ()
    {
	return (Object) new Integer(state);
    }
    
    /**
     * @message com.arjuna.ats.arjuna.ActivationRecord_1 [com.arjuna.ats.arjuna.ActivationRecord_1] ActivationRecord::set_value() called illegally
     */

    public void setValue (Object v)
    {
	if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.ActivationRecord_1");
    }
    
    /**
     * nestedAbort causes the reset_state function of the object to be invoked
     * passing it the saved ObjectStatus.
     */
    
    public int nestedAbort ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "ActivationRecord::nestedAbort() for "+order());
	}
	
	int outcome = TwoPhaseOutcome.FINISH_ERROR;
	
	if ((objectAddr != null) && (actionHandle != null))
	    outcome = (objectAddr.forgetAction(actionHandle, false, RecordType.ACTIVATION) ? TwoPhaseOutcome.FINISH_OK : TwoPhaseOutcome.FINISH_ERROR);

	return outcome;
    }

    /**
     * nestedCommit does nothing since the passing of the state up to
     * the parent action is handled by the record list merging system.
     * In fact since nested_prepare returns PREPARE_READONLY this function should
     * never actually be called.
     */
    
public int nestedCommit ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "ActivationRecord::nestedCommit() for "+order());
	}
	
	return TwoPhaseOutcome.FINISH_OK;
    }

public int nestedPrepare ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "ActivationRecord::nestedPrepare() for "+order());
	}
	
	return TwoPhaseOutcome.PREPARE_READONLY;
    }
    
    /**
     * topLevelAbort for Activation records is exactly like a nested
     * abort.
     */
    
    public int topLevelAbort ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, "ActivationRecord::topLevelAbort() for "+order());
	}
	
	return nestedAbort();		/* i.e., same as nested case */
    }
    
    /*
     * topLevelCommit has little to do for ActivationRecords other
     * than to ensure the object is forgotten by the object.
     */
    
    public int topLevelCommit ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, 
				     "ActivationRecord::topLevelCommit() for "+order());
	}
	
	if ((objectAddr != null) && (actionHandle != null))
	{
	    return (objectAddr.forgetAction(actionHandle, true, RecordType.ACTIVATION) ? TwoPhaseOutcome.FINISH_OK : TwoPhaseOutcome.FINISH_ERROR);
	}
	
	return TwoPhaseOutcome.FINISH_ERROR;
    }
    
    public int topLevelPrepare ()
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_ABSTRACT_REC, 
				     "ActivationRecord::topLevelPrepare() for "+order());
	}
	
	if (objectAddr == null)
	    return TwoPhaseOutcome.PREPARE_NOTOK;
	else
	    return TwoPhaseOutcome.PREPARE_OK;
    }
    
    /**
     * Saving of ActivationRecords is only undertaken during the Prepare
     * phase of the top level 2PC.
     *
     * @message com.arjuna.ats.arjuna.ActivationRecord_2 [com.arjuna.ats.arjuna.ActivationRecord_2] Invocation of ActivationRecord::restore_state for {0} inappropriate - ignored for {1}
     */
    
    public boolean restore_state (InputObjectState os, int v)
    {
	if (tsLogger.arjLoggerI18N.isWarnEnabled())
	{
	    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.ActivationRecord_2", new Object[]{type(), order()} );
	}
	
	return false;
    }
    
    public boolean save_state (OutputObjectState os, ObjectType v)
    {
	return true;
    }
    
    public void print (PrintWriter strm)
    {
	super.print(strm);
	strm.println("ActivationRecord with state:\n"+state);
    }
    
    public String type ()
    {
	return "/StateManager/AbstractRecord/ActivationRecord";
    }
    
    public void merge (AbstractRecord a)
    {
    }
    
    public void alter (AbstractRecord a)
    {
    }
    
    /*
     * should_merge and should_replace are invoked by the record list manager
     * to determine if two records should be merged togethor or if the
     * 'newer' should replace the older.
     * shouldAdd determines if the new record should be added in addition
     * to the existing record and is currently only invoked if both of
     * should_merge and should_replace return FALSE
     * Default implementations here always return FALSE - ie new records
     * do not override old
     */
    
    public boolean shouldAdd (AbstractRecord a)
    {
	return false;
    }
    
    public boolean shouldAlter (AbstractRecord a)
    {
	return false;
    }
    
    public boolean shouldMerge (AbstractRecord a)
    {
	return false;
    }
    
    public boolean shouldReplace (AbstractRecord a)
    {
	return false;
    }
    
    protected ActivationRecord ()
    {
	super();
	
	objectAddr = null;
	actionHandle = null;
	state = ObjectStatus.PASSIVE;

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_ABSTRACT_REC, "ActivationRecord::ActivationRecord()");
	}
    }
    
    private StateManager objectAddr;
    private BasicAction  actionHandle;
    private int          state;
     
}



