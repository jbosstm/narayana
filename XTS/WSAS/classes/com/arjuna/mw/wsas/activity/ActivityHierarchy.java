/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.activity;

/**
 * This class represents a handle on a stack of activities.
 * It should only be used for suspending and resuming the
 * thread-to-activity association.
 *
 * The activity at the top of the stack is the current activity.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ActivityHierarchy.java,v 1.1 2002/11/25 10:51:41 nmcl Exp $
 * @since 1.0.
 */

public interface ActivityHierarchy
{
    /**
     * @return the number of activities associated with this stack.
     */

    public int size ();
    
    /**
     * @return whether or not this is a valid context. If a single entry in
     * the context is invalid then we assume the entire context is.
     */

    public boolean valid ();

    /**
     * @param index index of the specific activity in the stack.
     * @exception IndexOutOfBoundsException Thrown if the activity number
     * is too large or small.
     * @return the activity handle.
     */

    public ActivityHandle activity (int index) throws IndexOutOfBoundsException;

    /**
     * @return the current activity in the context.
     */

    public ActivityHandle current ();

    /**
     * Obtain a copy of this context. Although the context may be shared
     * between different threads, the same instance of the context should
     * not be.
     *
     * @return the context copy.
     */

    public ActivityHierarchy copy ();

    /**
     * Overrides Object.equals
     *
     * Two contexts are equal if both hierarchies are identical.
     *
     * @return <code>true</code> if the two objects are the same,
     * <code>false</code> otherwise.
     */

    public boolean equals (Object obj);

    /**
     * Although users won't typically care what the underlying implementation
     * of a context is, they will need to do comparisons.
     * So, although this method is provided by Java.Object we have it here
     * to ensure that we don't forget to implement it!
     *
     * @return the hash value for the target.
     */

    public int hashCode ();
    
}