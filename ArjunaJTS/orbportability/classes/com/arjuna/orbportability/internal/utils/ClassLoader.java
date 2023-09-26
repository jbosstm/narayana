/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.internal.utils;

import java.util.Hashtable;

class ClassLoader extends java.lang.ClassLoader
{

public ClassLoader ()
    {
	loadedClasses = new Hashtable();
    }
    
protected Class loadClass (String className, boolean resolve) throws ClassNotFoundException
    {
	Class c = (Class) loadedClasses.get(className);

	if (c == null)
	{
	    c = findSystemClass(className);

	    // put it into hash table for later.
	    
	    loadedClasses.put(className, c);
	}

	// c must be set to get here!
	
	if (resolve)
	    resolveClass(c);

	return c;
    }
    
private Hashtable loadedClasses;
    
}