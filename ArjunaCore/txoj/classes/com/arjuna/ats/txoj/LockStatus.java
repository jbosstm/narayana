/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.txoj;

import java.io.PrintWriter;

/**
 * Essentially an enumeration of the status a lock may
 * be in.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: LockStatus.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class LockStatus
{

public static final int LOCKFREE = 0;
public static final int LOCKHELD = 1;
public static final int LOCKRETAINED = 2;

    /**
     * Print a human-readable form.
     */

public static String printString (int ls)
    {
	switch (ls)
	{
	case LOCKFREE:
	    return "LockStatus.LOCKFREE";
	case LOCKHELD:
	    return "LockStatus.LOCKHELD";
	case LOCKRETAINED:
	    return "LockStatus.LOCKRETAINED";
	default:
	    return "Unknown";
	}
    }

public static void print (PrintWriter strm, int ls)
    {
	strm.print(LockStatus.printString(ls));
    }
    
};