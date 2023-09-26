/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.tools;

import com.arjuna.ats.arjuna.coordinator.HeuristicInformation;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.hp.mwtests.ts.jta.jts.common.ExtendedCrashRecord;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class UserExtendedCrashRecord extends ExtendedCrashRecord {

    HeuristicInformation hi;

    // AbstractRecords need an empty constructor
    public UserExtendedCrashRecord() {
        super();
    }

    public UserExtendedCrashRecord(ExtendedCrashRecord.CrashLocation cl, ExtendedCrashRecord.CrashType ct, HeuristicInformationOverride hi) {
        super(cl, ct);

        this.hi = hi;
    }

    @Override
    public Object value() {
        return hi;
    }

    @Override
    public void setValue(Object o) {
        if (o != null && o instanceof HeuristicInformation)
            hi = (HeuristicInformation) o;
    }

    @Override
    public String type() {
        return record_type();
    }

    public static final String record_type() {
        return "/StateManager/AbstractRecord/UserExtendedCrashRecord";
    }

    @Override
    public int typeIs() {
        return RecordType.USER_DEF_FIRST1;
    }

    @Override
    public boolean save_state(OutputObjectState os, int ot) {
        try {
            if (hi == null) {
                os.packBoolean(false);
            } else {
                os.packBoolean(true);
                os.packInt(hi.getHeuristicType());
            }
        } catch (IOException e) {
            return false;
        }

        return super.save_state(os, ot);
    }

    @Override
    public boolean restore_state(InputObjectState os, int ot) {
        try {
            if (os.unpackBoolean())
                hi = new HeuristicInformationOverride(os.unpackInt());
        } catch (IOException e) {
            return false;
        }

        return super.restore_state(os, ot);
    }

    public static class HeuristicInformationOverride implements HeuristicInformation, Serializable {
        int outcome;

        public HeuristicInformationOverride() {
            this(TwoPhaseOutcome.HEURISTIC_ROLLBACK);
        }

        public HeuristicInformationOverride(int outcome) {
            this.outcome = outcome;
        }

        @Override
        public int getHeuristicType() {
            return outcome;
        }

        @Override
        public Object getEntityReference() {
            return null;
        }
    }
}