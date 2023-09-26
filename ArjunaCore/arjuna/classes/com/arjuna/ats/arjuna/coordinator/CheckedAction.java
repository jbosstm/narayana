/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import java.util.Hashtable;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * If an action attempts to terminate with threads still active we call an
 * instance of this class to determine what to do. The default simply prints a
 * warning and relies upon the outstanding threads to find out the state of the
 * action later. However, this can be overridden, e.g., the thread attempting to
 * terminate the action may be made to block. WARNING: watch out for deadlock!
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: CheckedAction.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.2.4.
 */

public class CheckedAction
{
    /**
     * Called during transaction termination if more than one thread is
     * associated with the transaction. The supplied information should be
     * sufficient for application specific implementations to do useful work
     * (such as synchronizing on the threads).
     * 
     * The default implementation simply prints a warning.
     */

    public void check (boolean isCommit, Uid actUid, Hashtable list)
    {
        if (isCommit)
            tsLogger.i18NLogger.warn_coordinator_CheckedAction_1(actUid, Integer.toString(list.size()));
        else
            tsLogger.i18NLogger.warn_coordinator_CheckedAction_2(actUid, Integer.toString(list.size()));
    }

}