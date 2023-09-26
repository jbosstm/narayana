/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.common;

import java.io.IOException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

// Mostly a duplication of com.hp.mwtests.ts.arjuna.resources.CrashRecord

public class ExtendedCrashRecord extends AbstractRecord // XAResourceRecord
{
    public enum CrashLocation { NoCrash, CrashInPrepare, CrashInCommit, CrashInAbort }
    public enum CrashType { Normal, HeuristicHazard }

    // Need a default constructor to keep the RecordTypeManager happy
    public ExtendedCrashRecord()
    {
        _cl = CrashLocation.NoCrash;
        _ct = CrashType.Normal;
    }

    public ExtendedCrashRecord(CrashLocation cl, CrashType ct)
    {
        super(new Uid());

        _cl = cl;
        _ct = ct;
    }

    public void forget()
    {
        _forgotten = true;
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
        if (!_forgotten && _cl == CrashLocation.CrashInAbort)
        {
            _forgotten = true;
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
        if (!_forgotten && _cl == CrashLocation.CrashInCommit)
        {
            _forgotten = true;
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
        if (!_forgotten && _cl == CrashLocation.CrashInAbort)
        {
            _forgotten = true;
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
        try {
            os.packBoolean(_forgotten);
        } catch (IOException e) {
            return false;
        }
        return super.save_state(os, ot);
    }

    public boolean restore_state(InputObjectState os, int ot)
    {
        try {
            _forgotten = os.unpackBoolean();
        } catch (IOException e) {
            return false;
        }

        return super.restore_state(os, ot);
    }

    public String type()
    {
        return "/StateManager/AbstractRecord/ExtendedCrashRecord";
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
        return "CrashRecord. forgotten: " + _forgotten + ": " + super.toString();
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

    private boolean _forgotten;
    private CrashLocation _cl;
    private CrashType _ct;
}