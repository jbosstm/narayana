/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability;

/**
 * The various types of ORB that are supported.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ORBType.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class ORBType
{

    /**
     * Returns the enumerated value for the orb with the given name.
     * If this ORB is not known it will return -1.
     *
     * @param name The name of the ORB to find.
     * @return The enumerated value for this ORB.
     */
    static int getORBEnum(String name)
    {
        for (int count=0;count<ORB_NAME.length;count++)
        {
            if ( ORB_NAME[count].equals( name ) )
            {
                return ORB_ENUM[count];
            }
        }

        return -1;
    }


    public static final int JAVAIDL = 1;

    public static final String javaidl = "JAVAIDL";

    private final static String[]           ORB_NAME = { javaidl };
    private final static int[]              ORB_ENUM = { JAVAIDL };
}