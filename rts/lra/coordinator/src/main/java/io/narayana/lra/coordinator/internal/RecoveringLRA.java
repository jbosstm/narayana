/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.coordinator.internal;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import io.narayana.lra.logging.LRALogger;
import io.narayana.lra.coordinator.domain.model.LongRunningAction;
import io.narayana.lra.coordinator.domain.service.LRAService;
import jakarta.ws.rs.ServiceUnavailableException;
import org.eclipse.microprofile.lra.annotation.LRAStatus;

import java.util.concurrent.locks.ReentrantLock;

class RecoveringLRA extends LongRunningAction {
    /**
     * Re-creates/activates an LRA for the specified transaction Uid.
     */
    RecoveringLRA(LRAService lraService, Uid rcvUid, int theStatus) {
        super(lraService, rcvUid);

        _theStatus = theStatus;
        _activated = activate(); // this should initialize the state
    }

    public boolean isActivated() {
        return _activated;
    }

    public void replayPhase2() {
        // protect against recovery and application threads both trying to finish the LRA
        ReentrantLock lock = tryLockTransaction();

        if (lock == null) {
            return;
        }

        try {
            tryReplayPhase2();
        } catch (ServiceUnavailableException ignore) {
            // it's already been logged so ignore and retry on the next round
        } finally {
            lock.unlock();
        }
    }

    /**
     * Replays phase 2 of the commit protocol.
     */
    private void tryReplayPhase2() {
        if (LRALogger.logger.isDebugEnabled()) {
            LRALogger.logger.debugf("RecoveringLRA.replayPhase2 recovering %s ActionStatus is %s",
                    get_uid(), ActionStatus.stringForm(_theStatus));
        }

        if (_activated) {
            if ((_theStatus == ActionStatus.PREPARED) ||
                    (_theStatus == ActionStatus.COMMITTING) ||
                    (_theStatus == ActionStatus.COMMITTED) ||
                    (_theStatus == ActionStatus.H_COMMIT) ||
                    (_theStatus == ActionStatus.H_MIXED) ||
                    (_theStatus == ActionStatus.H_HAZARD)) {
                if (heuristicList.size() != 0 || pendingList.size() != 0 || getLRAStatus() == LRAStatus.Active) {
                    // Note that we do not try to recover failed LRAs.
                    // Move any heuristics back onto the prepared list for another attempt:
                    moveTo(heuristicList, preparedList, false);
                    moveTo(pendingList, preparedList, false);

                    checkParticipant(preparedList);

                    // NB we don't Abort a BasicAction since that can bypass creation of a log
                    super.phase2Commit(true);

                    runPostLRAActions(); // nb the participant record may have already ran the after action

                    // if there are no more heuristics or failures then update the status of the LRA
                    if (heuristicList.size() == 0 && failedList.size() == 0) {
                        updateState(toLRAStatus(_theStatus));
                    }

                    switch (getLRAStatus()) {
                        case Closed:
                        case Cancelled:
                        case FailedToClose:
                        case FailedToCancel:
                            getLraService().finished(this, false);
                            break;
                        default:
                            if (LRALogger.logger.isInfoEnabled()) {
                                LRALogger.logger.infof("RecoveringLRA.replayPhase2 for %s ended with status: %s",
                                        getId().toASCIIString(), getLRAStatus());
                            }
                            break;
                    }
                }
            } else if (LRALogger.logger.isInfoEnabled()) {
                LRALogger.logger.info("RecoveringLRA.replayPhase2: Unexpected status: "
                        + ActionStatus.stringForm(_theStatus));
            }
        } else {
            if (LRALogger.logger.isInfoEnabled()) {
                LRALogger.logger.infof(
                        "RecoveringLRA: LRA %s not activated, unable to replay phase 2 commit, will retry later",
                        get_uid());
            }

            // Failure to activate (NB other types such as AtomicActionExpiryScanner move the log)
        }
    }

    private final int _theStatus; // Current transaction status

    // Flag to indicate that this transaction has been re-activated successfully.
    private boolean _activated = false;
}