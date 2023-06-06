/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.arjuna.orbportability.orb;

/**
 * Objects which should be invoked after the ORB has shutdown should
 * be derived from this class.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: PostShutdown.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public abstract class PostShutdown extends Shutdown
{
    
public abstract void work ();

protected PostShutdown ()
    {
	super("PostShutdown");
    }
    
protected PostShutdown (String name)
    {
	super(name);
    }

}