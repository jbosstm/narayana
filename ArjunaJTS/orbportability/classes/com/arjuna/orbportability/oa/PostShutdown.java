/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.oa;

/**
 * Objects to be called after the OA has shutdown should implement
 * this class.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: PostShutdown.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public abstract class PostShutdown extends Shutdown
{
    
public abstract void work ();

protected PostShutdown (String name)
    {
	super(name);
    };

}