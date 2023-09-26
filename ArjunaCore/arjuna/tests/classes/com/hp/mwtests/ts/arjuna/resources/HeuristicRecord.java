/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.resources;

import java.io.PrintWriter;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

public class HeuristicRecord extends AbstractRecord
{

    public HeuristicRecord()
    {
        super(new Uid());
    }

    public int typeIs()
    {
        return RecordType.USER_DEF_FIRST1;
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
        return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
    }

    public int topLevelCommit()
    {
        return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
    }

    public int topLevelPrepare()
    {
        return TwoPhaseOutcome.PREPARE_OK;
    }

    public void print(PrintWriter strm)
    {
        strm.println("Shutdown for:");
        super.print(strm);
    }

    public boolean doSave()
    {
        return true;
    }

    public boolean save_state(OutputObjectState os, int ot)
    {
        return true;
    }

    public boolean restore_state(InputObjectState os, int ot)
    {
        return true;
    }

    public String type()
    {
        return "/StateManager/AbstractRecord/HeuristicRecord";
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
}