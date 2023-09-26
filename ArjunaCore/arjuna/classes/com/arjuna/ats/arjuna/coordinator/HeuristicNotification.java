/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.Uid;

/*
 * @author Mark Little (mark@arjuna.com)
 * @since 3.0.
 */

/*
 * If the caller doesn't want to be informed of heuristics during completion
 * then it's possible the application (or admin) may still want to be informed.
 * So special participants can be registered with the transaction which are
 * triggered during the Synchronization phase and given the true outcome of
 * the transaction. We do not dictate a specific implementation for what these
 * participants do with the information (e.g., OTS allows for the CORBA Notification Service
 * to be used).
 */

public abstract class HeuristicNotification implements SynchronizationRecord
{
    public abstract void heuristicOutcome (int actionStatus);
    
    public Uid get_uid ()
    {
        return _uid;
    }

    public boolean beforeCompletion ()
    {
        return true;
    }

    public boolean afterCompletion (int status)
    {
        return true;
    }

    @Override
    public boolean isInterposed() {
        return false;
    }

    private Uid _uid = new Uid();
}