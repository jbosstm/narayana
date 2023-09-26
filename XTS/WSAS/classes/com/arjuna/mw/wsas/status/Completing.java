/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.status;

/**
 * The activity is in the process of completing.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Completing.java,v 1.1 2002/11/25 10:51:44 nmcl Exp $
 * @since 1.0.
 */

public class Completing implements Status
{

    public static Completing instance ()
    {
	return _instance;
    }

    public String toString ()
    {
	return "Status.Completing";
    }

    private Completing ()
    {
    }

    private static final Completing _instance = new Completing();
    
}