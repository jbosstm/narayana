/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.util;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

public enum TxStatus {
    TransactionRollbackOnly(ActionStatus.ABORT_ONLY, TwoPhaseOutcome.FINISH_OK),
    TransactionRollingBack(ActionStatus.ABORTING, TwoPhaseOutcome.FINISH_ERROR),
    TransactionRolledBack(ActionStatus.ABORTED, TwoPhaseOutcome.FINISH_OK),
    TransactionCommitting(ActionStatus.COMMITTING, TwoPhaseOutcome.FINISH_ERROR),
    TransactionCommitted(ActionStatus.COMMITTED, TwoPhaseOutcome.FINISH_OK),
    TransactionHeuristicRollback(ActionStatus.H_ROLLBACK, TwoPhaseOutcome.HEURISTIC_ROLLBACK),
    TransactionHeuristicCommit(ActionStatus.H_COMMIT, TwoPhaseOutcome.HEURISTIC_COMMIT),
    TransactionHeuristicHazard(ActionStatus.H_HAZARD, TwoPhaseOutcome.HEURISTIC_HAZARD),
    TransactionHeuristicMixed(ActionStatus.H_MIXED, TwoPhaseOutcome.HEURISTIC_MIXED),
    TransactionPreparing(ActionStatus.PREPARING, TwoPhaseOutcome.FINISH_ERROR),  // TwoPhaseOutcome.PREPARE_NOTOK
    TransactionPrepared(ActionStatus.PREPARED, TwoPhaseOutcome.PREPARE_OK),
    TransactionActive(ActionStatus.RUNNING, TwoPhaseOutcome.FINISH_ERROR),
    TransactionCommittedOnePhase(ActionStatus.NO_ACTION + 20, TwoPhaseOutcome.FINISH_OK),
    TransactionReadOnly(ActionStatus.NO_ACTION + 21, TwoPhaseOutcome.PREPARE_READONLY),
    TransactionStatusUnknown(ActionStatus.NO_ACTION + 23, TwoPhaseOutcome.FINISH_ERROR); // is this correct

    private final int status;
    private final int twoPhaseOutcome;

    TxStatus(int status, int twoPhaseOutcome) {
        this.status = status;
        this.twoPhaseOutcome = twoPhaseOutcome;
    }

    public int status() {
        return status;
    }
    public int twoPhaseOutcome() {
        return twoPhaseOutcome;
    }
    public static boolean isPrepare(String status) {
        return fromStatus(status).equals(TxStatus.TransactionPrepared);
    }
    public static boolean isCommit(String status) {
        return fromStatus(status).equals(TxStatus.TransactionCommitted);
    }
    public static boolean isAbort(String status) {
        return fromStatus(status).equals(TxStatus.TransactionRolledBack);
    }
    public static boolean isReadOnly(String status) {
        return fromStatus(status).equals(TxStatus.TransactionReadOnly);
    }
    public static boolean isCommitOnePhase(String status) {
        return fromStatus(status).equals(TxStatus.TransactionCommittedOnePhase);
    }

    public boolean isPrepare() {
        return this.equals(TransactionPrepared);
    }

    public boolean isCommit() {
        return this.equals(TransactionCommitted);
    }

    public boolean isCommitOnePhase() {
        return this.equals(TxStatus.TransactionCommittedOnePhase);
    }

    public boolean isAbort() {
        return this.equals(TransactionRolledBack);
    }

    public boolean isReadOnly() {
        return this.equals(TxStatus.TransactionReadOnly);
    }

    public boolean isRollbackOnly() {
        return this.equals(TxStatus.TransactionRollbackOnly);
    }

    public boolean isRunning() {
        return this.equals(TransactionActive);
    }

    public boolean isHeuristic() {
        switch (status) {
            case ActionStatus.H_COMMIT:
            case ActionStatus.H_HAZARD:
            case ActionStatus.H_MIXED:
            case ActionStatus.H_ROLLBACK:
                return true;
            default:
                return false;
        }
    }

    public boolean isComplete() {
        switch (status) {
            case ActionStatus.COMMITTED:
            case ActionStatus.ABORTED:
                return true;
            default:
                return false;
        }
    }

    public boolean isGone() {
        return isComplete();
    }

    public boolean isFinished() {
        switch (status) {
            case ActionStatus.COMMITTED  :
            case ActionStatus.H_COMMIT   :
            case ActionStatus.H_MIXED    :
            case ActionStatus.H_HAZARD   :
            case ActionStatus.ABORTED    :
            case ActionStatus.H_ROLLBACK :
                return true;

                //case ActionStatus.INVALID: throw ...
            default:
                return false;
        }
    }

    public boolean isActive() {
        switch (status) {
            case ActionStatus.ABORT_ONLY:
            case ActionStatus.ABORTING:
            case ActionStatus.COMMITTING:
            case ActionStatus.PREPARING:
            case ActionStatus.PREPARED:
            case ActionStatus.RUNNING:
                return true;
            default:
                return false;
        }
    }


    public boolean isFinishing() {
        switch (status) {
            case ActionStatus.PREPARING  :
            case ActionStatus.COMMITTING   :
            case ActionStatus.ABORTING    :
                return true;
            default:
                return false;
        }
    }

    public boolean hasHeuristic() {
        switch (status) {
            case ActionStatus.H_COMMIT   :
            case ActionStatus.H_MIXED    :
            case ActionStatus.H_HAZARD   :
            case ActionStatus.H_ROLLBACK :
                return true;

            default:
                return false;
        }
    }

    /**
     * convert a string into an enum type.
     * @param status  the name of the enum type
     * @throws IllegalArgumentException if the input status value does not correspond to an enum name
     * @return enum type corresponding to status
     */
    public static TxStatus fromStatus(String status) {
        try {
            return TxStatus.valueOf(TxStatus.class, status);
        } catch (Exception e) {
            return TxStatus.TransactionStatusUnknown;
        }
    }

    public static TxStatus fromActionStatus(int actionStatus) {
        switch (actionStatus) {
        case ActionStatus.ABORT_ONLY:
            return TransactionRollbackOnly;
        case ActionStatus.ABORTING:
            return TransactionRollingBack;
        case ActionStatus.ABORTED:
            return TransactionRolledBack;
        case ActionStatus.COMMITTING:
            return TransactionCommitting;
        case ActionStatus.COMMITTED:
            return TransactionCommitted;
        case ActionStatus.H_ROLLBACK:
            return TransactionHeuristicRollback;
        case ActionStatus.H_COMMIT:
            return TransactionHeuristicCommit;
        case ActionStatus.H_HAZARD:
            return TransactionHeuristicHazard;
        case ActionStatus.H_MIXED:
            return TransactionHeuristicMixed;
        case ActionStatus.PREPARING:
            return TransactionPreparing;
        case ActionStatus.PREPARED:
            return TransactionPrepared;
        case ActionStatus.RUNNING:
            return TransactionActive;
        default:
            return TransactionStatusUnknown;
        }
    }
}