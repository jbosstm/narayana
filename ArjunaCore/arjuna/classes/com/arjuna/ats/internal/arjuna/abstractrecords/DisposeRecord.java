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
 * $Id: DisposeRecord.java 2342 2006-03-30 13:06:17Z  $
 */

/*
 *
 * Dipose Record Class.
 *
 */

package com.arjuna.ats.internal.arjuna.abstractrecords;

import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import java.io.PrintWriter;

import java.io.IOException;

public class DisposeRecord extends CadaverRecord
{

    public DisposeRecord (ParticipantStore participantStore, StateManager sm)
    {
	super(null, participantStore, sm);
	
	this.targetParticipantStore = participantStore;
	
	if (sm != null)
	{
	    objectUid = sm.get_uid();
	    typeName = sm.type();
	}
	else
	{
	    objectUid = Uid.nullUid();
	    typeName = null;
	}

	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("DisposeRecord::DisposeRecord(" + participantStore + ", " + objectUid + ")");
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
	
	if ((targetParticipantStore != null) && (objectUid.notEquals(Uid.nullUid())))
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

	if ((targetParticipantStore != null) && (objectUid.notEquals(Uid.nullUid())))
	{
	    try
	    {
		if (targetParticipantStore.remove_committed(objectUid, typeName))
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

        if ((targetParticipantStore != null) && (objectUid.notEquals(Uid.nullUid())))
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

        if ((targetParticipantStore != null) && (objectUid.notEquals(Uid.nullUid())))
        {
            try
            {
                UidHelper.packInto(objectUid, os);
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
            objectUid = UidHelper.unpackFrom(os);
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

	objectUid = new Uid(Uid.nullUid());
	typeName = null;
	targetParticipantStore = null;
    }
    
    private Uid         objectUid;
    private String      typeName;
}

