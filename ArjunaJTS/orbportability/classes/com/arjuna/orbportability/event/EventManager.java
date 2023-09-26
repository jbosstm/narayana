/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.event;

import java.util.Enumeration;
import java.util.Hashtable;

import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.opLogger;

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
            opLogger.i18NLogger.warn_event_EventManager_forhandlethrewexception(
                    "com.arjuna.orbportability.event.EventManager.connected()", h.name(), ex);
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
            opLogger.i18NLogger.warn_event_EventManager_forhandlethrewexception(
                     "com.arjuna.orbportability.event.EventManager.disconnected(Object)", h.name(), ex);
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

    protected EventManager ()
    {
        _handlers = new Hashtable();

        for(EventHandler eventHandler : opPropertyManager.getOrbPortabilityEnvironmentBean().getEventHandlers()) {
            addHandler(eventHandler);
        }
    }
    
private Hashtable _handlers;
    
private static EventManager _theManager = null;
        
}