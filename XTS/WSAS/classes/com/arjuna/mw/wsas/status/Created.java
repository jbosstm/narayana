/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.status;

/**
 * The activity has been created and is yet to start executing.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Created.java,v 1.1 2002/11/25 10:51:44 nmcl Exp $
 * @since 1.0.
 */

public class Created implements Status
{

    public static Created instance ()
    {
	return _instance;
    }

    public String toString ()
    {
	return "Status.Created";
    }

    private Created ()
    {
    }

    private static final Created _instance = new Created();

}