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
