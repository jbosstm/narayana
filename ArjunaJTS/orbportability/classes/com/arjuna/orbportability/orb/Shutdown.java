/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.arjuna.orbportability.orb;

/**
 * Some common methods for the ORB shutdown classes.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Shutdown.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public abstract class Shutdown
{
    
public abstract void work ();

public final String name ()
    {
	return _name;
    }
    
protected Shutdown (String theName)
    {
	_name = theName;
    }

private String _name;

}