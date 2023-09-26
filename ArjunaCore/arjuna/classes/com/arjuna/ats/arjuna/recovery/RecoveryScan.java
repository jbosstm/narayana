/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.recovery ;

/**
 * Interface for users to receive a callback whenever a recovery
 * scan completes.
 */

public interface RecoveryScan
{
    /**
     * This operation is invoked by the recovery manager when a
     * recovery scan completes. It does not indicate that all inflight
     * transactions and resources have been recovered (some may have failed
     * to recovery, for instance), only that all of the recovery modules
     * have finished and indicated they have done as much as they can on
     * this scan.
     */

    public void completed ();
}