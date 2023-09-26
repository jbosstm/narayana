/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import java.util.concurrent.Callable;

public class AsyncAfterSynchronization implements Callable<Boolean> {
    private TwoPhaseCoordinator coordinator;
    private SynchronizationRecord synchronization;
    private int                 _status;

    public AsyncAfterSynchronization(TwoPhaseCoordinator coordinator, SynchronizationRecord synchronization, int status) {
        this.coordinator = coordinator;
        this.synchronization = synchronization;
        this._status = status;
    }

    /**
     * Run the call
     * @return true if the call was successful and false otherwise
     * @throws Exception if the wrapped synchronisation throws an exception
     */
    public Boolean call() throws Exception {
        // Synchronisations are executed with undefined transaction context.
        ThreadActionData.pushAction(coordinator, false);

        try {
            if (!synchronization.afterCompletion(_status)) {
                tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_4(synchronization.toString());

                return false;
            }

            return true;
        } catch (Exception ex) {
            tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_4a(synchronization.toString(), ex);
            throw ex;
        } catch (Error er) {
            tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_4b(synchronization.toString(), er);
            throw er;
        } finally {
            ThreadActionData.popAction(false);
        }
    }
}