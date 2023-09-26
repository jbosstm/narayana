/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas;

import com.arjuna.mwlabs.wsas.UserActivityImple;

/**
 * Return the UserActivity implementation to use.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: UserActivityFactory.java,v 1.7 2005/05/19 12:13:15 nmcl Exp $
 * @since 1.0.
 */

public class UserActivityFactory
{
    
    /**
     * @return The UserActivity for the application to use.
     */

    public static UserActivity userActivity ()
    {
	return _imple;
    }

    /*
    public static UserActivity userActivity (String name)
    {
	UserActivity imple = (UserActivity) _instances.get(name);
	
	if (imple == null)
	{
	    imple = new UserActivityImple();

	    _instances.put(name, imple);
	}
	
	_currentActivity.put(Thread.currentThread(), imple);
	
	return imple;
    }

    public static UserActivity currentActivityService ()
    {
	return (UserActivity) _currentActivity.get(Thread.currentThread());
    }

    private static Hashtable   _instances = new Hashtable();
    private static WeakHashMap _currentActivity = new WeakHashMap();
    */

    private static UserActivityImple _imple = new UserActivityImple();
}