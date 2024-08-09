/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.recovery ;

/**
 * Interface for Recovery manager plug-in module.
 * RecoveryModules are registered via the properties mechanisms.
 * The periodicWorkFirstPass of each module is called, then RecoveryManager
 * waits for the time interval RECOVERY_BACKOFF_PERIOD (seconds), then the
 * periodicWorkSecondPass of each module are called.
 * The RecoveryManager then waits for period PERIODIC_RECOVERY_PERIOD (seconds)
 * before starting the first pass again
 * The backoff period between the first and second pass is intended to allow
 * transactions that were in-flight during the first pass to be completed normally,
 * without requiring the status of each one to be checked. The recovery period 
 * will typically be appreciably longer.
 */

public interface RecoveryModule
{
    /**
     * Called by the RecoveryManager at start up, and then
     * PERIODIC_RECOVERY_PERIOD seconds after the completion, for all RecoveryModules,
     * of the second pass
     */

    public void periodicWorkFirstPass ();
    
    /**
     * Called by the RecoveryManager RECOVERY_BACKOFF_PERIOD seconds
     * after the completion of the first pass
     */

    public void periodicWorkSecondPass ();

    /**
     * <p>This method determines whether there is work left to do at the end of the
     * second pass (i.e. {@link RecoveryModule#periodicWorkSecondPass()})
     * <p>Note: This method should be invoked only at the end of the recovery cycle,
     * i.e. at the end of {@link RecoveryModule#periodicWorkSecondPass()}.
     * Any invocation that happens before this point does not guarantee to return
     * the correct value
     *
     * @return true if there is work left to do, false otherwise
     */
    default boolean hasWork() {
        return false;
    }
}