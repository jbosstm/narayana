/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: UserActivityFactory.java,v 1.7 2005/05/19 12:13:15 nmcl Exp $
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

