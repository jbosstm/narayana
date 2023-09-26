/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.abstractrecords;

import java.io.IOException;
import java.io.PrintWriter;

import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

public class DisposeRecord extends CadaverRecord
{

    public DisposeRecord (ParticipantStore participantStore, StateManager sm)
    {
	super(null, participantStore, sm);
	
	this.targetParticipantStore = participantStore;
	
	if (sm != null)
	{
	    originalInstanceID = sm.get_uid();
	    typeName = sm.type();
	}
	else
	{
	    originalInstanceID = Uid.nullUid();
	    typeName = null;
	}

	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("DisposeRecord::DisposeRecord(" + participantStore + ", " + originalInstanceID + ")");
    }
    }

    public boolean propagateOnAbort ()
    {
	return false;
    }

    public int typeIs ()
    {
	return RecordType.DISPOSE;
    }
    
    public int nestedAbort ()
    {
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("DisposeRecord::nestedAbort() for " + order());
    }
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    public int nestedCommit ()
    {
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("DisposeRecord::nestedCommit() for " + order());
    }
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    public int nestedPrepare ()
    {
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("DisposeRecord::nestedPrepare() for " + order());
    }
	
	if ((targetParticipantStore != null) && (originalInstanceID.notEquals(Uid.nullUid())))
	    return TwoPhaseOutcome.PREPARE_OK;
	else
	    return TwoPhaseOutcome.PREPARE_NOTOK;
    }
    
    public int topLevelAbort ()
    {
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("DisposeRecord::topLevelAbort() for " + order());
    }
	
	return TwoPhaseOutcome.FINISH_OK;
    }
    
    /**
     * At topLevelCommit we remove the state from the object participantStore.
     */
    
    public int topLevelCommit ()
    {
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("DisposeRecord::topLevelCommit() for " + order());
    }

	if ((targetParticipantStore != null) && (originalInstanceID.notEquals(Uid.nullUid())))
	{
	    try
	    {
		if (targetParticipantStore.remove_committed(originalInstanceID, typeName))
		{
		    // only valid if not doing recovery

		    if (super.objectAddr != null)
		    {
		        StateManagerFriend.destroyed(super.objectAddr);
			//super.objectAddr.destroyed();
		    }
		    
		    return TwoPhaseOutcome.FINISH_OK;
		}
	    }
	    catch (final Throwable e) {
            tsLogger.i18NLogger.warn_DisposeRecord_5(e);
        }
	}
	
	return TwoPhaseOutcome.FINISH_ERROR;
    }

    public int topLevelPrepare ()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("DisposeRecord::topLevelPrepare() for " + order());
        }

        if ((targetParticipantStore != null) && (originalInstanceID.notEquals(Uid.nullUid())))
        {
            // force PersistenceRecord.save_state to ignore topLevelState:
            shadowForced();
            return TwoPhaseOutcome.PREPARE_OK;
        }
        else
            return TwoPhaseOutcome.PREPARE_NOTOK;
    }
    
    public void print (PrintWriter strm)
    {
	strm.println("Dispose for:");
	super.print(strm);
    }
    
    public boolean doSave ()
    {
	return true;
    }

    public boolean save_state (OutputObjectState os, int ot)
    {
        boolean res = true;

        if ((targetParticipantStore != null) && (originalInstanceID.notEquals(Uid.nullUid())))
        {
            try
            {
                UidHelper.packInto(originalInstanceID, os);
                os.packString(typeName);

                res = (res && super.save_state(os, ot));
            }
            catch (IOException e) {
                tsLogger.i18NLogger.warn_DisposeRecord_2();
                res = false;
            }
        }
        else {
            tsLogger.i18NLogger.warn_DisposeRecord_3();

            res = false;
        }

        return res;
    }

    public boolean restore_state (InputObjectState os, int ot)
    {
        boolean res = true;

        try
        {
            originalInstanceID = UidHelper.unpackFrom(os);
            typeName = os.unpackString();

            res = (res && super.restore_state(os, ot));

        }
        catch (final Exception e)
        {
            res = false;
        }

        return res;
    }
    
    public String type ()
    {
	return "/StateManager/AbstractRecord/RecoveryRecord/PersistenceRecord/CadaverRecord/DisposeRecord";
    }
    
    public boolean shouldAdd (AbstractRecord a)
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
    
    public boolean shouldAlter (AbstractRecord a)
    {
	return false;
    }

    public DisposeRecord ()
    {
	super();

    originalInstanceID = new Uid(Uid.nullUid());
	typeName = null;
	targetParticipantStore = null;
    }

    private Uid originalInstanceID;
    private String      typeName;
}