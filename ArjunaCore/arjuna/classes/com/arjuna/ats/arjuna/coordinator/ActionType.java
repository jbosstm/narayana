/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import java.io.PrintWriter;

/**
 * The two types of transactions, nested, and top-level.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ActionType.java 2342 2006-03-30 13:06:17Z $
 * @since 1.0.
 */

public class ActionType
{

    public static final int TOP_LEVEL = 0;

    public static final int NESTED = 1;

    public static String stringForm (int type)
    {
        switch (type)
        {
        case TOP_LEVEL:
            return "ActionType.TOP_LEVEL";
        case NESTED:
            return "ActionType.NESTED";
        default:
            return "Unknown";
        }
    }
    
    /**
     * Print a human-readable version of the type.
     */

    public static void print (PrintWriter strm, int res)
    {
        strm.print(stringForm(res));
    }

}