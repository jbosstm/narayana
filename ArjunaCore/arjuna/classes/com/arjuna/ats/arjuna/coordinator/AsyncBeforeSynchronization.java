/*
 *  JBoss, Home of Professional Open Source.
 *  Copyright 2013, Red Hat, Inc., and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
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

