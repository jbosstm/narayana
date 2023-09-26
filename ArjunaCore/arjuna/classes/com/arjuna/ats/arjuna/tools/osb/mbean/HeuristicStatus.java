/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

import javax.transaction.xa.XAException;

/**
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public enum HeuristicStatus {
    HEURISTIC_ROLLBACK, // after prepare decided to roll back without waiting for coordinator
    HEURISTIC_COMMIT,  // after prepare decided to commit without waiting for coordinator
    HEURISTIC_MIXED,  // after prepare some sub-participants committed and some rolled back without waiting for coordinator
    HEURISTIC_HAZARD,  // after prepare some sub-participants committed, some rolled back and some we don't know
    UNKNOWN;

    public static final int UNKNOWN_XA_ERROR_CODE =  XAException.XA_RBEND + 1000; // must be large than any legitimate XA error code

    public static HeuristicStatus intToStatus(int heuristic) {
        switch (heuristic) {
            case TwoPhaseOutcome.HEURISTIC_ROLLBACK: return HEURISTIC_ROLLBACK;
            case TwoPhaseOutcome.HEURISTIC_COMMIT: return HEURISTIC_COMMIT;
            case TwoPhaseOutcome.HEURISTIC_MIXED: return HEURISTIC_MIXED;
            case TwoPhaseOutcome.HEURISTIC_HAZARD: return HEURISTIC_HAZARD;
            default: return UNKNOWN;
        }
    }

    public int getXAErrorCode() {
        if (this.equals(HEURISTIC_ROLLBACK))
            return XAException.XA_HEURRB;
        if (this.equals(HEURISTIC_COMMIT))
            return XAException.XA_HEURCOM;
        if (this.equals(HEURISTIC_MIXED))
            return XAException.XA_HEURMIX;
        if (this.equals(HEURISTIC_HAZARD))
            return XAException.XA_HEURHAZ;

        return UNKNOWN_XA_ERROR_CODE;
    }
}
