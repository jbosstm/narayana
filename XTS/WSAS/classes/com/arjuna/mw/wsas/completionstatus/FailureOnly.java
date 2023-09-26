/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.completionstatus;

/**
 * The activity has terminated in a failure state. Once in this state, the
 * activity state cannot transition further.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: FailureOnly.java,v 1.1 2002/11/25 10:51:41 nmcl Exp $
 * @since 1.0.
 */

public class FailureOnly implements CompletionStatus
{

    public static FailureOnly instance ()
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
	    if (param instanceof FailureOnly)
		return true;
	    else
		return false;
	}
    }	

    public String toString ()
    {
	return "CompletionStatus.FailureOnly";
    }

    private FailureOnly ()
    {
    }

    private static final FailureOnly _instance = new FailureOnly();

}