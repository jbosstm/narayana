/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.resources;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

class DummyMap2 implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return CrashRecord.class;
    }

    public int getType ()
    {
        return RecordType.USER_DEF_FIRST0;
    }    
}

public class CrashRecord extends AbstractRecord
{
    public enum CrashLocation { NoCrash, CrashInPrepare, CrashInCommit, CrashInAbort };
    public enum CrashType { Normal, HeuristicHazard };

    public CrashRecord ()
    {
        _cl = CrashLocation.NoCrash;
        _ct = CrashType.Normal;
    }
    
    public CrashRecord (CrashLocation cl, CrashType ct)
    {
        super(new Uid());
        
        _cl = cl;
        _ct = ct;
    }

    public int typeIs()
    {
        return RecordType.USER_DEF_FIRST0;
    }

    public int nestedAbort()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit()
    {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    public int nestedPrepare()
    {
        return TwoPhaseOutcome.PREPARE_NOTOK;
    }

    public int topLevelAbort()
    {
        if (_cl == CrashLocation.CrashInAbort)
        {
            if (_ct == CrashType.Normal)
                return TwoPhaseOutcome.FINISH_ERROR;
            else
                return TwoPhaseOutcome.HEURISTIC_HAZARD;
        }
        else
            return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelCommit()
    {
        if (_cl == CrashLocation.CrashInCommit)
        {
            if (_ct == CrashType.Normal)
                return TwoPhaseOutcome.FINISH_ERROR;
            else
                return TwoPhaseOutcome.HEURISTIC_HAZARD;
        }
        else
            return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelPrepare()
    {
        if (_cl == CrashLocation.CrashInAbort)
        {
            if (_ct == CrashType.Normal)
                return TwoPhaseOutcome.PREPARE_NOTOK;
            else
                return TwoPhaseOutcome.HEURISTIC_HAZARD;
        }
        else
            return TwoPhaseOutcome.PREPARE_OK;
    }

    public boolean doSave()
    {
        return true;
    }

    public boolean save_state(OutputObjectState os, int ot)
    {
        return super.save_state(os, ot);
    }

    public boolean restore_state(InputObjectState os, int ot)
    {
        return super.restore_state(os, ot);
    }

    public String type()
    {
        return "/StateManager/AbstractRecord/CrashRecord";
    }

    public boolean shouldAdd(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldMerge(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldReplace(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldAlter(AbstractRecord a)
    {
        return false;
    }

    public void merge(AbstractRecord a)
    {
    }

    public void alter(AbstractRecord a)
    {
    }   

    public String toString ()
    {
        return "CrashRecord. No state. "+super.toString();
    }

    /**
     * @return <code>Object</code> to be used to order.
     */

    public Object value()
    {
        return null;
    }

    public void setValue(Object o)
    {
    }

    private CrashLocation _cl;
    private CrashType _ct;
}