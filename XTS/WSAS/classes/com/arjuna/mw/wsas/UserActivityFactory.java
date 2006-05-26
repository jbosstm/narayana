/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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

import java.io.FileNotFoundException;

/**
 * Return the UserActivity implementation to use.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: UserActivityFactory.java,v 1.7 2005/05/19 12:13:15 nmcl Exp $
 * @since 1.0.
 */

/*
 * TODO
 *
 * DOH! This is severely broken/restricted, because we can only ever have
 * a single activity service per process in this model. All HLS-es are added
 * to the same service and run simultaneously, even if that doesn't actually
 * make sense! What we need is to have multiple activity services with
 * different HLS-es allowed for each. Very similar to having multiple POAs.
 *
 * At the moment it works as is because we know there is a very limited
 * set of HLS-es that are running and that they don't conflict. However, this
 * isn't guaranteed in general, so we need to fix this in the refactoring!
 *
 * The reason we can say with certainty that it currently works is: we only
 * have either AtomicTransaction (at) and/or BusinessActivity (ba) HLS-es
 * registered for AXTS. If both are registered then a start on an activity
 * will create an activity that has both running! However, the start was the
 * result of a specific incoming SOAP message on a specific ActivationService
 * (either an at or a ba). So, we give back a context that only contains
 * an at or a ba RegistrationService, not both. This means that participants
 * can only be registered with the at *or* the ba; the other is a redundant
 * coordinator and will just terminate in a success mode. We end up with
 * two coordinators for each activity, but only the right one will ever be
 * used. Not ideal, but one of the limitations of the original WS-AS model.
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

    static
    {
	try
	{
	    com.arjuna.mw.wsas.utils.Configuration.initialise("/wsas.xml");
	}
	catch (FileNotFoundException ex)
	{
	}
	catch (Exception ex)
	{
	    throw new ExceptionInInitializerError(ex.toString());
	}
    }
    
}

