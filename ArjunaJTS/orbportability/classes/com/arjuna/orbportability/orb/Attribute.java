/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.orb;

/**
 * Classes to be called before or after the ORB is initialised.
 * Override the postORBInit method to determine where in the
 * initialisation the class should be called.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Attribute.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public abstract class Attribute
{
    
public abstract void initialise (String[] params);

public boolean postORBInit ()
    {
	return true;
    }
 
};