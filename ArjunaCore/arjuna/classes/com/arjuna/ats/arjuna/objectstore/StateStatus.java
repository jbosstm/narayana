/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.objectstore;

import java.io.PrintWriter;

/**
 * The status of states in the ObjectStore.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class StateStatus
{
    /**
     * StateStatus
     */
    
    public static final int OS_UNKNOWN = -1;  // means no state present.
    
    public static final int OS_COMMITTED = 1;
    public static final int OS_UNCOMMITTED = 2;
    public static final int OS_HIDDEN = 4;
    public static final int OS_COMMITTED_HIDDEN = StateStatus.OS_COMMITTED | StateStatus.OS_HIDDEN;
    public static final int OS_UNCOMMITTED_HIDDEN = StateStatus.OS_UNCOMMITTED | StateStatus.OS_HIDDEN;

    public static void printStateStatus (PrintWriter strm, int res)
    {
        strm.print(stateStatusString(res));
    }

    public static String stateStatusString (int res)
    {
        switch (res)
        {
        case StateStatus.OS_UNKNOWN:
            return "StateStatus.OS_UNKNOWN";
        case StateStatus.OS_COMMITTED:
            return "StateStatus.OS_COMMITTED";
        case StateStatus.OS_UNCOMMITTED:
            return "StateStatus.OS_UNCOMMITTED";
        case StateStatus.OS_HIDDEN:
            return "StateStatus.OS_HIDDEN";
        case StateStatus.OS_COMMITTED_HIDDEN:
            return "StateStatus.OS_COMMITTED_HIDDEN";
        case StateStatus.OS_UNCOMMITTED_HIDDEN:
            return "StateStatus.OS_UNCOMMITTED_HIDDEN";
        default:
            return "Illegal";
        }
    }
    
    private StateStatus ()
    {
    }
}