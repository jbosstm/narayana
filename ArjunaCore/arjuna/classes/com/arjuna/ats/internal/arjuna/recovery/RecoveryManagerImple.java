/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.recovery;

import java.io.IOException;
import java.util.Vector;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;

/**
 * The RecoveryManagerImple - does the real work. Currently we can have only one
 * of these per node, so each instance checks it's the only one running. If it
 * isn't it will kill itself before doing any work.
 */
public class RecoveryManagerImple {
    private PeriodicRecovery _periodicRecovery = null;

    private RecActivatorLoader _recActivatorLoader = null;

    /**
     * Does the work of setting up crash recovery.
     *
     * @param threaded if <code>true</code> then the manager will start a separate
     * thread to run recovery periodically.
     */
    public RecoveryManagerImple(boolean threaded) {
        // by default we do not use a socket based listener, but it can be turned on if not required.
        boolean useListener = recoveryPropertyManager.getRecoveryEnvironmentBean().isRecoveryListener();

        /*
         * Check whether there is a recovery daemon running - only allow one per
         * object store
         *
         * Note: this does not actually check if a recovery manager is running for the same ObjectStore,
         * only if one is on the same port as our configuration. Thus it's not particularly robust.
         * TODO: add a lock file to the ObjectStore as a belt and braces approach?
         *
         * This check works by trying to bind the server socket, so don't do it if we are running local only
         * (yup, that means there is a greater chance of winding up with more than one recovery manager if
         * we are running without a listener. See comment on robustness and file locking.)
         */

        if (useListener && isRecoveryManagerEndPointInUse()) {
            try {
                // JBTM-3990 log a message because the caller is not guaranteed to log it
                tsLogger.i18NLogger.fatal_recovery_fail(RecoveryManager.getRecoveryManagerHost().getHostAddress(),
                        Integer.toString(RecoveryManager.getRecoveryManagerPort()));
            } catch (Throwable t) {
                tsLogger.i18NLogger.fatal_recovery_fail("unknown", "unknown");
            }


            throw new FatalError("Recovery manager already active (or recovery port and address are in use)!");
        }

        // start the activator recovery loader

        _recActivatorLoader = new RecActivatorLoader();
        _recActivatorLoader.startRecoveryActivators();

        // start the periodic recovery thread
        // (don't start this until just about to go on to the other stuff)

        _periodicRecovery = new PeriodicRecovery(threaded, useListener);

        /*
         * Start the expiry scanner
         *
         * This has to happen after initiating periodic recovery, because periodic recovery registers record types used
         * by the expiry scanner
         */
        ExpiredEntryMonitor.startUp();

        try {
            if (tsLogger.logger.isInfoEnabled()) {
                if (useListener) {
                    tsLogger.i18NLogger.info_recovery_socketready(Integer.toString(_periodicRecovery.getServerSocket().getLocalPort()));
                } else {
                    tsLogger.logger.debug("RecoveryManagerImple is ready. Socket listener is turned off.");
                }
            }
        } catch (IOException ex) {
            tsLogger.i18NLogger.warn_recovery_RecoveryManagerImple_2(ex);
        }
    }

    public final void scan() {
        _periodicRecovery.doWork();
    }

    public final void addModule(RecoveryModule module) {
        _periodicRecovery.addModule(module);
    }

    public final void removeModule(RecoveryModule module, boolean waitOnScan) {
        _periodicRecovery.removeModule(module, waitOnScan);
    }

    public final void removeAllModules(boolean waitOnScan) {
        _periodicRecovery.removeAllModules(waitOnScan);
    }

    public final Vector<RecoveryModule> getModules() {
        return _periodicRecovery.getModules();
    }

    public void start() {
        if (!_periodicRecovery.isAlive()) {
            _periodicRecovery.start();
        }
    }

    /**
     * stop the recovery manager
     *
     * @param async false means wait for any recovery scan in progress to complete
     */
    public void stop(boolean async) {
        // must ensure we clean up dependent threads

        ExpiredEntryMonitor.shutdown();

        _periodicRecovery.shutdown(async);
    }

    /**
     * Suspend the recovery manager. If the recovery manager is in the process of
     * doing recovery scans then it will be suspended afterwards, in order to
     * preserve data integrity.
     *
     * @param async false means wait for the recovery manager to finish any scans before returning.
     * @param waitForWorkLeftToDo when true, it is important that, before invoking this method,
     * all transactions will either be terminated by the Transaction Reaper or they have prepared
     * and a log has been written, otherwise the suspend call may never return.
     */
    public PeriodicRecovery.Mode trySuspendScan(boolean async, boolean waitForWorkLeftToDo) {
        return _periodicRecovery.suspendScan(async, waitForWorkLeftToDo);
    }

    public PeriodicRecovery.Mode trySuspendScan(boolean async) {
        return trySuspendScan(async, false);
    }

    public void resumeScan() {
        _periodicRecovery.resumeScan();
    }

    /**
     * wait for the recovery implementation to be shut down.
     */
    public void waitForTermination() {
        try {
            _periodicRecovery.join();
        } catch (final Exception ex) {
        }
    }

    /**
     * Test whether the recovery manager (RM) port and address are available - if not assume that another
     * recovery manager is already active.
     * <p>
     * Ideally this method needs to discover whether or not another RM is already monitoring the object store
     *
     * @return true if the RM port and address are in use
     */
    private final boolean isRecoveryManagerEndPointInUse() {
        /*
         * attempt to create the server socket. If an exception is thrown then some other
         * process is using the RM endpoint
         */
        if (_periodicRecovery != null) {
            return _periodicRecovery.getMode() != PeriodicRecovery.Mode.TERMINATED;
        } else {
            return false;
        }
    }
}