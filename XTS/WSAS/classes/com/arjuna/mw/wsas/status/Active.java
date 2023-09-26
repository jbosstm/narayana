/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.status;

/**
 * The activity is active.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Active.java,v 1.1 2002/11/25 10:51:44 nmcl Exp $
 * @since 1.0.
 */

public class Active implements Status
{

    public static Active instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "Status.Active";
    }

    private Active ()
    {
    }

    private static final Active _instance = new Active();
    
}