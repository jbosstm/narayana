/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas;

import com.arjuna.mwlabs.wsas.ActivityManagerImple;

/**
 * Return the ActivityManager implementation to use.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ActivityManagerFactory.java,v 1.1 2002/11/25 10:51:40 nmcl Exp $
 * @since 1.0.
 */

public class ActivityManagerFactory
{

    /**
     * @return The ActivityManager for applications to use.
     */

    public static ActivityManager activityManager ()
    {
	return _imple;
    }

    private static ActivityManagerImple _imple = new ActivityManagerImple();
    
}