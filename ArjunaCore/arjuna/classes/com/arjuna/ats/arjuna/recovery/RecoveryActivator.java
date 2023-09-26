/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.recovery ;

/**
 * Interface for Recovery manager plug-in module.
 * RecoveryActivators are registered via the properties mechanisms.
 * The startRCservice of each Activator is called to create the appropriate 
 * Recovery Component able to receive recovery requests according to a particular
 * transaction protocol. For instance, when used with OTS, the RecoveryActivitor 
 * has the responsibility to create a RecoveryCoordinator object able to respond
 * to the replay_completion operation.
 *
 * @author Malik Saheb
 * @since ArjunaTS 3.0
 */

public interface RecoveryActivator
{
    /**
     * Called to create appropriate instance(s), specific to a standard transaction protocol,
     * able to receive inquiries for recovery
     *
     * @return true on success, false on failure
     */
    public boolean startRCservice();
}