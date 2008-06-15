/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
 * (C) 2008
 * @author JBoss Inc.
 */

package com.hp.mwtests.ts.arjuna.testframework;

import com.arjuna.mwlabs.testframework.unittest.Test;

import java.lang.reflect.Method;

public class Wrapper
{
    public static void main (String[] args)
    {
	String className = null;

	if ((args.length > 0) && args[0].equals("-class"))
	    className = args[1];

        if (className == null)
	{
	    System.err.println("No class specified. Usage: -class <test class> [args]");

	    return;
	}

	String[] params = new String[args.length-2];

	System.arraycopy(args, 2, params, 0, args.length-2);

	Class c = null;

	try
	{
	    c = Thread.currentThread().getContextClassLoader().loadClass(className);

	    Test theTest = (Test) c.newInstance();

	    theTest.initialise(null, null, params, new com.arjuna.mwlabs.testframework.unittest.LocalHarness());
	    theTest.run(params);
	}
	catch (ClassCastException ex)
	{
	    try
	    {
		Method mainMethod = c.getMethod("main", String[].class);

		if (mainMethod == null)
		{
		    System.err.println("No main found for non-Test class.");
		   
		    return;
		}

		mainMethod.invoke(null, (Object) params);
	    }
	    catch (Exception x)
	    {
		x.printStackTrace();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

}
