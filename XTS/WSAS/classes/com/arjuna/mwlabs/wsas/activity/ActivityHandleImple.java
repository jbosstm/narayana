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
 * $Id: ActivityHandleImple.java,v 1.1 2002/11/25 10:51:45 nmcl Exp $
 */

package com.arjuna.mwlabs.wsas.activity;

import com.arjuna.mwlabs.wsas.activity.ActivityImple;

import com.arjuna.mw.wsas.activity.ActivityHandle;

/**
 * ActivityHandle is used as a representation of a single activity
 * when it is suspended from a running thread and may be later
 * resumed. The implementation of the token can be as lightweight
 * as required by the underlying implementation in order that it
 * can uniquely represent all activity instances.
 *
 * Since this is a client-facing class, it is unlikely that the
 * application user will typically want to see the entire activity
 * context in order to simply suspend it from the thread.
 */

public class ActivityHandleImple implements ActivityHandle
{

    public ActivityHandleImple (ActivityImple curr)
    {
	_theActivity = curr;
	_valid = ((_theActivity == null) ? false : true);
    }
    
    /**
     * Although users won't typically care what the underlying implementation
     * of a context is, they will need to do comparisons.
     * So, although this method is provided by Java.Object we have it here
     * to ensure that we don't forget to implement it!
     *
     * Two instances are equal if the refer to the same transaction.
     */

    public boolean equals (Object obj)
    {
	if (obj != null)
	{
	    if (obj == this)
		return true;
	    else
	    {
		if (obj instanceof ActivityHandleImple)
		{
		    if (_theActivity.equals(obj))
			return true;
		}
	    }
	}
	
	return false;
    }
    
    /**
     * Although users won't typically care what the underlying implementation
     * of a context is, they will need to do comparisons.
     * So, although this method is provided by Java.Object we have it here
     * to ensure that we don't forget to implement it!
     */

    public int hashCode ()
    {
	return ((_theActivity == null) ? 0 : _theActivity.hashCode());
    }

    /**
     * @return whether or not this is a valid handle.
     */

    public boolean valid ()
    {
	return _valid;
    }

    public int getTimeout ()
    {
	try
	{
	    return _theActivity.getTimeout();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	    
	    return -1;
	}
    }
    
    /**
     * @return the activity identifier.
     */

    public String tid ()
    {
	return ((_theActivity == null) ? null : new String(_theActivity.getGlobalId().value()));
    }

    public final ActivityImple getActivity ()
    {
	return _theActivity;
    }

    public String toString ()
    {
	return tid();
    }

    private ActivityImple _theActivity;
    private boolean       _valid;
    
}
