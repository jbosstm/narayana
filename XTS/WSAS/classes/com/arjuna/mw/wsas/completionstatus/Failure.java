/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.completionstatus;

/**
 * The activity has terminated in a failed state. The activity state can
 * toggle between this and any other state.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Failure.java,v 1.1 2002/11/25 10:51:41 nmcl Exp $
 * @since 1.0.
 */

public class Failure implements CompletionStatus
{

    public static Failure instance ()
    {
	return _instance;
    }

    /**
     * Two statuses are equal if their targets are the same.
     */

    public boolean equals (Object param)
    {
	if (this == param)
	    return true;
	else
	{
	    if (param instanceof Failure)
		return true;
	    else
		return false;
	}
    }	

    public String toString ()
    {
	return "CompletionStatus.Failure";
    }

    private Failure ()
    {
    }

    private static final Failure _instance = new Failure();
 
}