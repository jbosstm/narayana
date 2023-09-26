/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna;

import java.io.PrintWriter;

/**
 * The various types of StateManager object which can exist.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectType.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class ObjectType
{

    public static final int RECOVERABLE = 0;

    public static final int ANDPERSISTENT = 1;

    public static final int NEITHER = 2;

    public static final int UNKNOWN_TYPE = 3;

    /**
     * Print a human-readable form of the object type.
     */

    public static void print (PrintWriter strm, int ot)
    {
        strm.print(toString(ot));
    }

    public static String toString(int ot)
    {
        switch (ot)
        {
            case RECOVERABLE:
                return "RECOVERABLE";
            case ANDPERSISTENT:
                return"ANDPERSISTENT";
            case NEITHER:
                return"NEITHER";
            default:
                return"UNKNOWN_TYPE";
        }
    }
}