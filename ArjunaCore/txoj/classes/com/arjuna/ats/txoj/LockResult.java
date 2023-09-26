/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.txoj;

import java.io.PrintWriter;

/**
 * The various results which can occur when setting a lock.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: LockResult.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class LockResult
{
    
    public static final int GRANTED = 0;
    public static final int REFUSED = 1;
    public static final int RELEASED = 2;

    public static String stringForm (int l)
    {
	switch (l)
	{
	case GRANTED:
	    return "LockResult.GRANTED";
	case REFUSED:
	    return "LockResult.REFUSED";
	case RELEASED:
	    return "LockResult.RELEASED";
	default:
	    return "Unknown";
	}
    }
    
    /**
     * Print a human-readable form of the lock result.
     */

    public static void print (PrintWriter strm, int l)
    {
	strm.print(stringForm(l));
    }
	
}