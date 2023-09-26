/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.recovery;

/**
 * An interface that adds extra behaviour to RecoveryModules.
 * An extra behaviour should provide a default method
 * to ensure binary compatibility with older code.
 */
public interface ExtendedRecoveryModule extends RecoveryModule {
    /**
     * Report whether or not the last recovery pass was successful.
     * A successful recovery pass means that no warnings or errors
     * were logged. This means that any failure conditions are
     * guaranteed to be obtainable by inspecting the logs.
     *
     * @return false if any RecoveryModule logged a warning or error
     * on the previous recovery pass.
     */
    default boolean isPeriodicWorkSuccessful() {
        return true;
    }
}