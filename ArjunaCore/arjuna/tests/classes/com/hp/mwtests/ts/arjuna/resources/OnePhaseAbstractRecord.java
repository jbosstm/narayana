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

public class OnePhaseAbstractRecord extends AbstractRecord
{

    public OnePhaseAbstractRecord()
    {
        super(new Uid());
    }

    public boolean onePhaseCalled ()
    {
        return _onePhaseCalled;
    }
    
    public int typeIs()
    {
        return RecordType.USER_DEF_FIRST0;
    }
    
    public int nestedOnePhaseCommit ()
    {
        _onePhaseCalled = true;
        
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelOnePhaseCommit ()
    {
        _onePhaseCalled = true;
        
        return TwoPhaseOutcome.FINISH_OK;
    }
    
    public int nestedAbort()
    {
        return TwoPhaseOutcome.FINISH_ERROR;
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
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    public int topLevelCommit()
    {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    public int topLevelPrepare()
    {
        return TwoPhaseOutcome.PREPARE_READONLY;
    }

    public void print(PrintWriter strm)
    {
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
        return "/StateManager/AbstractRecord/OnePhaseAbstractRecord";
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

    private boolean _onePhaseCalled = false;
}