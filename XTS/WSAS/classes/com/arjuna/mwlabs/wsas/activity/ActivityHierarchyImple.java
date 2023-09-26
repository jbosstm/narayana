/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wsas.activity;

import com.arjuna.mwlabs.wsas.activity.ActivityImple;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.activity.ActivityHandle;

import java.util.Stack;

/**
 * This class represents a handle on a stack of activities.
 * It should only be used for suspending and resuming the
 * thread-to-activity association.
 *
 * The activity at the top of the stack is the current activity.
 */

public class ActivityHierarchyImple implements ActivityHierarchy
{

    public ActivityHierarchyImple (ActivityImple current)
    {
	_hierarchy = new Stack();
	
	if (current != null)
	{
	    ActivityImple[] hierarchy = current.hierarchy();

	    for (int i = 0; i < hierarchy.length; i++)
		_hierarchy.push(new ActivityHandleImple(hierarchy[i]));

	    _valid = true;
	}
	else
	    _valid = false;
    }
    
    /**
     * @return the number of activities associated with this stack.
     */

    public int size ()
    {
	return _hierarchy.size();
    }
    
    /**
     * @return whether or not this is a valid context. If a single entry in
     * the context is invalid then we assume the entire context is.
     */

    public boolean valid ()
    {
	return _valid;
    }

    /**
     * @param the specific activity in the stack.
     * @exception IndexOutOfBoundsException Thrown if the activity number
     * is too large or small.
     * @return the activity handle.
     */

    public ActivityHandle activity (int index) throws IndexOutOfBoundsException
    {
	try
	{
	    return (ActivityHandle) _hierarchy.elementAt(index);
	}
	catch (ArrayIndexOutOfBoundsException ex)
	{
	    throw new IndexOutOfBoundsException();
	}
    }

    /**
     * @return the current activity in the context.
     */

    public ActivityHandle current ()
    {
	try
	{
	    return (ActivityHandle) _hierarchy.peek();
	}
	catch (Exception ex)
	{
	    return null;
	}
    }

    /**
     * Obtain a copy of this context. Although the context may be shared
     * between different threads, the same instance of the context should
     * not be.
     *
     * @return the context copy.
     */

    public ActivityHierarchy copy ()
    {
	return new ActivityHierarchyImple(this);
    }

    /**
     * Overrides Object.equals
     *
     * Two contexts are equal if both hierarchies are identical.
     */

    public boolean equals (Object obj)
    {
	if (obj != null)
	{
	    if (obj == this)
		return true;
	    else
	    {
		if (obj instanceof ActivityHierarchyImple)
		{
		    ActivityHierarchyImple compare = (ActivityHierarchyImple) obj;
		    
		    if (_hierarchy.size() == compare._hierarchy.size())
		    {
			for (int i = 0; i < _hierarchy.size(); i++)
			{
			    if (!_hierarchy.elementAt(i).equals(compare._hierarchy.elementAt(i)))
			    {
				return false;
			    }
			}
			
			return true;
		    }
		}
		else
		    return true;
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
	return _hierarchy.hashCode();
    }

    public String toString ()
    {
	String toReturn = "Activity context:";
	
	if ((_hierarchy == null) || (_hierarchy.isEmpty()))
	    toReturn += " null";
	else
	{
	    for (int i = 0; i < _hierarchy.size(); i++)
		toReturn += " "+_hierarchy.elementAt(i);
	}

	return toReturn;
    }
	
    protected ActivityHierarchyImple (ActivityHierarchyImple toCopy)
    {
	_hierarchy = null;
	_valid = false;
	
	if (toCopy != null)
	{
	    int copySize = toCopy._hierarchy.size();
	    
	    if (copySize > 0)
	    {
		_hierarchy = new Stack();
	    
		for (int i = 0; i < copySize; i++)
		{
		    /*
		     * Do we want to create copies of the elements?
		     */

		    _hierarchy.push(toCopy._hierarchy.elementAt(i));
		}

		_valid = true;
	    }
	}
    }
    
    private Stack   _hierarchy;
    private boolean _valid;
    
}