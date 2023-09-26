/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import java.util.concurrent.Callable;

public class AsyncBeforeSynchronization implements Callable<Boolean> {
    private TwoPhaseCoordinator coordinator;
    private SynchronizationRecord synchronization;

    public AsyncBeforeSynchronization(TwoPhaseCoordinator coordinator, SynchronizationRecord synchronization) {
        this.coordinator = coordinator;
        this.synchronization = synchronization;
    }

    /**
     * Run the call
     * @return true if the call was successful and false otherwise
     * @throws Exception if the wrapped synchronisation throws an exception
     */
    public Boolean call() throws Exception {
        // Synchronisations are executed with the transaction context of the transaction that is being committed.
        ThreadActionData.pushAction(coordinator, false);

        try {
           return synchronization.beforeCompletion();
        } catch (Exception e) {
            tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_2(synchronization.toString(), e);
            throw e;
        } catch (Error e) {
            tsLogger.i18NLogger.warn_coordinator_TwoPhaseCoordinator_2(synchronization.toString(), e);
            throw e;
        } finally {
            ThreadActionData.popAction(false);
        }
    }
}