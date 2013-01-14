package com.hp.mwtests.ts.arjuna.resources;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.OnePhaseResource;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.abstractrecords.LastResourceRecord;

import java.io.PrintWriter;

public class LastResourceShutdownRecord extends LastResourceRecord
{
    private final boolean failInPrepare;

    public LastResourceShutdownRecord(OnePhaseResource onePhaseResource) {
        this(onePhaseResource, false);
    }

    public LastResourceShutdownRecord(OnePhaseResource onePhaseResource, boolean failInPrepare) {
        super(onePhaseResource);
        this.failInPrepare = failInPrepare;
    }

    public int topLevelPrepare()
    {
        if (failInPrepare) {
            super.topLevelAbort();
            return TwoPhaseOutcome.PREPARE_NOTOK;
        } else {
            return super.topLevelPrepare();
        }
    }

    public void print(PrintWriter strm)
    {
        strm.println("LastResourceShutdownRecord for:");
        super.print(strm);
    }

    public String type()
    {
        return "/StateManager/AbstractRecord/LastResourceShutdownRecord";
    }

}


