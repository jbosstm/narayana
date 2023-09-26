/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.completionstatus;

/**
 * The completion status is unknown. This is a transient and should eventually
 * become determined.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Unknown.java,v 1.1 2002/11/25 10:51:42 nmcl Exp $
 * @since 1.0.
 */

public class Unknown implements CompletionStatus
{

    /**
     * Two statuses are equal if their targets are the same.
     */

    public boolean equals (Object param)
    {
        if (this == param)
       	    return true;
       	else
       	{
       	    if (param instanceof Unknown)
       		    return true;
       	    else
       		    return false;
       	}
    }	

    public String toString ()
    {
	return "CompletionStatus.Unknown";
    }
 
}