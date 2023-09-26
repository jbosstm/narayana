/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import java.io.PrintWriter;

/**
 * The possible outcomes when trying to add an AbstractRecord as
 * a participant within a transaction.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: AddOutcome.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class AddOutcome
{
    
public static final int AR_ADDED = 2;
public static final int AR_REJECTED = 3;
public static final int AR_DUPLICATE = 4;

    /**
     * @since JTS 2.1.2.
     */

public static String printString (int res)
    {
	switch (res)
	{
	case AR_ADDED:
	    return "AddOutcome.AR_ADDED";
	case AR_REJECTED:
	    return "AddOutcome.AR_REJECTED";
	case AR_DUPLICATE:
	    return "AddOutcome.AR_DUPLICATE";
	default:
	    return "Unknown";
	}
    }

public static void print (PrintWriter strm, int res)
    {
	strm.print(printString(res));
    }
    
}