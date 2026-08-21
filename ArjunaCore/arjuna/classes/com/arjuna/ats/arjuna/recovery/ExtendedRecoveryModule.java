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
     * Report whether the last periodic recovery pass completed
     * without errors. Implementations should return {@code false}
     * when an exception was caught or a failure was detected
     * during the recovery pass (for example, an
     * {@link com.arjuna.ats.arjuna.objectstore.ObjectStore ObjectStore}
     * access failure or an {@code XAResource} recovery error).
     *
     * <p>The default implementation returns {@code true}, indicating
     * no problems. Implementations that override this method should
     * reset the flag at the start of each recovery cycle and set it
     * to {@code false} whenever an error condition is encountered.
     *
     * @return {@code true} if the last recovery pass completed
     * without errors, {@code false} if any error was encountered
     */
    default boolean isPeriodicWorkSuccessful() {
        return true;
    }
}