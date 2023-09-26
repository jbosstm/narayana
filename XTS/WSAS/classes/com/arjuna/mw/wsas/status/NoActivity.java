/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.status;

/**
 * There is no activity associated with the invoking thread.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: NoActivity.java,v 1.1 2002/11/25 10:51:44 nmcl Exp $
 * @since 1.0.
 */

public class NoActivity implements Status
{

    public static NoActivity instance ()
    {
	return _instance;
    }

    public String toString ()
    {
	return "Status.NoActivity";
    }

    private NoActivity ()
    {
    }

    private static final NoActivity _instance = new NoActivity();
    
}