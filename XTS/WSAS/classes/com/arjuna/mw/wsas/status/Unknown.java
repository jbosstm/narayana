/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.status;

/**
 * The current status of the activity is unknown. This should be a transient.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Unknown.java,v 1.1 2002/11/25 10:51:44 nmcl Exp $
 * @since 1.0.
 */

public class Unknown implements Status
{

    public static Unknown instance ()
    {
	return _instance;
    }

    public String toString ()
    {
	return "Status.Unknown";
    }

    private Unknown ()
    {
    }

    private static final Unknown _instance = new Unknown();

}