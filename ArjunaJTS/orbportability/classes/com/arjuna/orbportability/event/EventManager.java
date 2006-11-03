/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: EventManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.event;

import com.arjuna.orbportability.common.Environment;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.opLogger;

import org.omg.CORBA.Object;
import java.util.*;

/**
 * The current implementation will invoke all registered handlers
 * whenever an object is connected and disconnected. These handlers
 * can then determine whether they want to do anything about it by
 * checking the type of the object (using narrow).
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: EventManager.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

/*
 * If we were to allow a registration of a handler on a per type basis
 * then we would have to do narrow ourselves (possibly for
 * every type in a classes hierarchy). A user would also then have to
 * register one instance for each type the object may have, or we would
 * have a more complicated registration process, possibly requiring a
 * "hierarchy" string to be passed in.
 *
 * Until we know how (and if) this class is used, it's better to start
 * off simple and work our way up to extremely complicated!
 */

public class EventManager
{

    /**
     * The object has been connected to the ORB.
     * @message com.arjuna.orbportability.event.EventManager.forhandlethrewexception {0} - for: {1} threw: {2}
     */

public void connected (org.omg.CORBA.Object obj)
    {
	Enumeration e = _handlers.elements();
	
	while (e.hasMoreElements())
	{
	    EventHandler h = (EventHandler) e.nextElement();

	    try
	    {
		h.connected(obj);
	    }
	    catch (Throwable ex)
	    {
                if (opLogger.loggerI18N.isWarnEnabled())
                {
                    opLogger.loggerI18N.warn( "com.arjuna.orbportability.event.EventManager.forhandlethrewexception",
                                    new java.lang.Object[] { "com.arjuna.orbportability.event.EventManager.connected()",
                                                   h.name(), ex } );
                }
	    }
	}
    }

    /**
     * The object has been disconnected from the ORB.
     */

public void disconnected (org.omg.CORBA.Object obj)
    {
	Enumeration e = _handlers.elements();
	
	while (e.hasMoreElements())
	{
	    EventHandler h = (EventHandler) e.nextElement();

	    try
	    {
		h.disconnected(obj);
	    }
	    catch (Throwable ex)
	    {
                if (opLogger.loggerI18N.isWarnEnabled())
                {
                    opLogger.loggerI18N.warn( "com.arjuna.orbportability.event.EventManager.forhandlethrewexception",
                                    new java.lang.Object[] { "com.arjuna.orbportability.event.EventManager.disconnected(Object)",
                                                             h.name(), ex } );
                }
	    }
	}
    }

    /**
     * Add the specified handler. If the handler has already been
     * registered then this operation will fail.
     */

public boolean addHandler (EventHandler h)
    {
	if (_handlers.get(h) == null)
	{
	    _handlers.put(h, h);

	    return true;
	}
	else
	    return false;
    }

    /**
     * Remove the specified handler. If the handler has not been
     * registered then this operation will fail.
     */

public boolean removeHandler (EventHandler h)
    {
	if (_handlers.remove(h) != null)
	    return true;
	else
	    return false;
    }

    /**
     * @return the EventManager instance.
     */

public static synchronized EventManager getManager ()
    {
	if (_theManager == null)
	    _theManager = new EventManager();
	
	return _theManager;
    }

    /**
     * @message com.arjuna.orbportability.event.EventManager.caughtexceptionfor {0} - caught exception: {1} for {2}
     * @message com.arjuna.orbportability.event.EventManager.novalue {0} - no value for: {1}
     */
protected EventManager ()
    {
	_handlers = new Hashtable();

	/*
	 * Now scan through the list of properties and see if there
	 * are any classes for us.
	 */

	Enumeration e = opPropertyManager.propertyManager.propertyNames();
	
	while (e.hasMoreElements())
	{
	    String name = (String) e.nextElement();
	    
	    if (name.startsWith(Environment.EVENT_HANDLER))
	    {
		String val = (String) opPropertyManager.propertyManager.getProperty(name);
		
		if (val != null)
		{
		    try
		    {
			Class c = Thread.currentThread().getContextClassLoader().loadClass(val);
			EventHandler h = (EventHandler) c.newInstance();

			addHandler(h);
		    }
		    catch (Exception ex)
		    {
                        if (opLogger.loggerI18N.isWarnEnabled())
                        {
                            opLogger.loggerI18N.warn( "com.arjuna.orbportability.event.EventManager.caughtexceptionfor",
                                            new java.lang.Object[] { "EventManager", ex, val } );
                        }
		    }
		}
		else
                {
                    if (opLogger.loggerI18N.isWarnEnabled())
                    {
                        opLogger.loggerI18N.warn( "com.arjuna.orbportability.event.EventManager.caughtexceptionfor",
                                        new java.lang.Object[] { "EventManager", name } );
                    }
                }
	    }
	}
    }
    
private Hashtable _handlers;
    
private static EventManager _theManager = null;
        
}
