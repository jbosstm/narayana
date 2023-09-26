/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna;

import java.io.PrintWriter;

/**
 * A transactional object may go through a number of different states
 * once it has been created.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectStatus.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ObjectStatus
{
    public static final int PASSIVE = 0;
    public static final int PASSIVE_NEW = 1;
    public static final int ACTIVE = 2;
    public static final int ACTIVE_NEW = 3;
    public static final int DESTROYED = 4;
    public static final int UNKNOWN_STATUS = 5;

    public static void print (PrintWriter strm, int os)
    {
        strm.print(toString(os));
    }

    public static String toString(int os)
    {
        switch (os)
        {
            case PASSIVE:
                return "PASSIVE";
            case PASSIVE_NEW:
                return "PASSIVE_NEW";
            case ACTIVE:
                return "ACTIVE";
            case ACTIVE_NEW:
                return "ACTIVE_NEW";
            case DESTROYED:
                return "DESTROYED";
            default:
                return "UNKNOWN_STATUS";
        }
    }
}