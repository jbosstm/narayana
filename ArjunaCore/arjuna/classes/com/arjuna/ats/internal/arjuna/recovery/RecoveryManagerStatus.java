/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.recovery;

/**
 * Exposes the current status of the periodic recovery thread as reported by {@link RecoveryManagerImple#trySuspendScan(boolean)}
 */
public enum RecoveryManagerStatus {
    /**
     * state value indicating that new scans may proceed ({@link PeriodicRecovery.Mode#ENABLED})
     */
    ENABLED,
    /**
     * state value indicating that new scans may not proceed and the periodic recovery thread should
     * suspend ({@link PeriodicRecovery.Mode#SUSPENDED})
     */
    SUSPENDED,
    /**
     * state value indicating that new scans may not proceed and that the singleton
     * PeriodicRecovery thread instance should exit if it is still running ({@link PeriodicRecovery.Mode#TERMINATED})
     */
    TERMINATED;
}