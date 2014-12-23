/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
 * @Deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
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
