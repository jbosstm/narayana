/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



/**
 * Insances of classes derived from this interface can be registered with
 * the system and do any tidy-up necessary after the ORB has
 * been shutdown.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: PreShutdown.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

package com.arjuna.orbportability.orb;

public abstract class PreShutdown extends Shutdown
{
    
public abstract void work ();

protected PreShutdown ()
    {
	super("PreShutdown");
    }
    
protected PreShutdown (String name)
    {
	super(name);
    };

};