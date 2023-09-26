/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.objectstore;

import java.io.PrintWriter;

/**
 * The type of the state in the ObjectStore.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class StateType
{
    /**
     * StateType.
     */

    public static final int OS_SHADOW = 10;
    public static final int OS_ORIGINAL = 11;
    public static final int OS_INVISIBLE = 12;

    public static final int OS_SHARED = 13;
    public static final int OS_UNSHARED = 14;
    
    public static void printStateType (PrintWriter strm, int res)
    {
        strm.print(stateTypeString(res));
    }

    public static String stateTypeString (int res)
    {
        switch (res)
        {
        case StateType.OS_SHADOW:
            return "StateType.OS_SHADOW";
        case StateType.OS_ORIGINAL:
            return "StateType.OS_ORIGINAL";
        case StateType.OS_INVISIBLE:
            return "StateType.OS_INVISIBLE";
        case StateType.OS_SHARED:
            return "StateType.OS_SHARED";
        case StateType.OS_UNSHARED:
            return "StateType.OS_UNSHARED";
        default:
            return "Illegal";
        }
    }
    
    private StateType ()
    {
    }
}