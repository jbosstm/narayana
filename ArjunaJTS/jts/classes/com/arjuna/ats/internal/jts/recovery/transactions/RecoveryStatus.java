/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



/*
 * Status flags for recovered transactions.
 * <P>
 * @author Dave Ingham (dave@arjuna.com)
 * @version $Id: RecoveryStatus.java 2342 2006-03-30 13:06:17Z  $ */

package com.arjuna.ats.internal.jts.recovery.transactions;

public class RecoveryStatus
{
    public static final int NEW = 0;
    public static final int ACTIVATED = 1;
    public static final int ACTIVATE_FAILED = 2;
    public static final int REPLAYING = 3;
    public static final int REPLAYED = 4;
    public static final int REPLAY_FAILED = 5; 
    
    /**
     * @return <code>String</code> representation of the status.
     */

    public static String stringForm (int res)
    {
        switch (res)
        {
        case NEW:
            return "RecoveryStatus.NEW";
        case ACTIVATED:
            return "RecoveryStatus.ACTIVATED";
        case ACTIVATE_FAILED:
            return "RecoveryStatus.ACTIVATE_FAILED";
        case REPLAYING:
            return "RecoveryStatus.REPLAYING";
        case REPLAYED:
            return "RecoveryStatus.REPLAYED";
        case REPLAY_FAILED:
            return "RecoveryStatus.REPLAY_FAILED";
        default:
            return "Unknown";
        }
    }
}