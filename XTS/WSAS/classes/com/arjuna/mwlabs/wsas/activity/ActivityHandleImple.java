/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wsas.activity;

import com.arjuna.mwlabs.wsas.activity.ActivityImple;
import java.nio.charset.StandardCharsets;

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
			ActivityImple compare = ((ActivityHandleImple) obj).getActivity();
			if (_theActivity.equals(compare))
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
	return ((_theActivity == null) ? null : new String(_theActivity.getGlobalId().value(), StandardCharsets.UTF_8));
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