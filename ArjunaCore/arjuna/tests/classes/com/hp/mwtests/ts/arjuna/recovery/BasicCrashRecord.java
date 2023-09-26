/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.recovery;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

public class BasicCrashRecord extends AbstractRecord
{

    public BasicCrashRecord()
    {
    }

    public int typeIs()
    {
        return RecordType.USER_DEF_FIRST0;
    }

    public boolean doSave()
    {
        return false;
    }

    public String type()
    {
        return "/StateManager/AbstractRecord/com.hp.mwtests.ts.arjuna.recovery.BasicCrashRecord";
    }

    public boolean save_state(OutputObjectState os, int i)
    {
        return false;
    }

    public boolean restore_state(InputObjectState os, int i)
    {
        return false;
    }

    public Object value()
    {
        return _id;
    }

    public void setValue(Object object)
    {
    }

    public int nestedAbort()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedPrepare()
    {
        return TwoPhaseOutcome.PREPARE_OK;
    }

    public int topLevelAbort()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelCommit()
    {
        throw new com.arjuna.ats.arjuna.exceptions.FatalError();
    }

    public int topLevelPrepare()
    {
        return TwoPhaseOutcome.PREPARE_OK;
    }

    public void alter(AbstractRecord abstractRecord)
    {
    }

    public void merge(AbstractRecord abstractRecord)
    {
    }

    public boolean shouldAdd(AbstractRecord abstractRecord)
    {
        return false;
    }

    public boolean shouldAlter(AbstractRecord abstractRecord)
    {
        return false;
    }

    public boolean shouldMerge(AbstractRecord abstractRecord)
    {
        return false;
    }

    public boolean shouldReplace(AbstractRecord abstractRecord)
    {
        return false;
    }

    private Uid _id = new Uid();

}