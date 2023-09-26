/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;



import java.io.IOException;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

public class CrashAbstractRecordImpl extends AbstractRecord
{
    public CrashAbstractRecordImpl(int crashBehaviour)
    {
        super(new Uid("-7FFFFFF" + _count + ":0:0:0:0"),
                "com.hp.mwtests.ts.arjuna.recovery.CrashAbstractRecordImpl",
                ObjectType.ANDPERSISTENT);
        _count++;
        _crash_behaviour = crashBehaviour;
    }

    // for crash recovery
    public CrashAbstractRecordImpl()
    {
    }

    public int typeIs()
    {
        return RecordType.USER_DEF_FIRST0;
    }

    public boolean doSave()
    {
        return true;
    }

    public String type()
    {
        return "/StateManager/AbstractRecord/com.hp.mwtests.ts.arjuna.recovery.CrashAbstractRecordImpl";
    }

    public boolean save_state(OutputObjectState os, int i)
    {
        boolean result = super.save_state(os, i);

        if (result) {
            try {
                os.packInt(_save_param);
            }
            catch (IOException ex) {
                result = false;
            }
        }

        return result;
    }

    public boolean restore_state(InputObjectState os, int i)
    {
        boolean result = super.restore_state(os, i);

        try {
            _save_param = os.unpackInt();
            _save_param++;
        }
        catch (IOException ex) {
            result = false;
        }

        return result;
    }

    public Object value()
    {
        return null;
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
        int two_phase_outcome = TwoPhaseOutcome.FINISH_OK;

        if (_crash_behaviour == CRASH_IN_COMMIT) {
            two_phase_outcome = TwoPhaseOutcome.FINISH_ERROR;
        }

        return two_phase_outcome;
    }

    public int topLevelPrepare()
    {
        int two_phase_outcome = TwoPhaseOutcome.PREPARE_OK;

        if (_crash_behaviour == CRASH_IN_PREPARE) {
            two_phase_outcome = TwoPhaseOutcome.PREPARE_NOTOK;
        }

        return two_phase_outcome;
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

    public static final int NO_CRASH = 0;
    public static final int CRASH_IN_PREPARE = 1;
    public static final int CRASH_IN_COMMIT = 2;
    public static final int CRASH_IN_ABORT = 3;
    private static int _save_param = 0;
    private static int _count = 1;
    private static int _crash_behaviour = NO_CRASH;
}