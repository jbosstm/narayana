/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TMTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.transactionserver;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.extensions.*;

import com.arjuna.ats.jts.common.jtsPropertyManager;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.mwlabs.testframework.unittest.Test;

import com.arjuna.common.util.propertyservice.PropertyManager;

import org.omg.CosTransactions.*;

import org.omg.CORBA.IntHolder;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;

public class TMTest extends Test
{

    public void run (String[] args)
    {
	ORB myORB = null;
	RootOA myOA = null;

	try
	{
	    myORB = ORB.getInstance("test");
	    myOA = OA.getRootOA(myORB);
	    
	    myORB.initORB(args, null);
	    myOA.initOA();

	    ORBManager.setORB(myORB);
	    ORBManager.setPOA(myOA);
	}
	catch (Exception e)
	{
	    System.err.println("Initialisation failed: "+e);
	    e.printStackTrace(System.err);
	    assertFailure();
	}
	
	TransactionFactory theOTS = null;
	Control topLevelControl = null;
	Services serv = new Services(myORB);
	int resolver = com.arjuna.orbportability.common.Configuration.bindDefault();
	String resolveService = jtsPropertyManager.propertyManager.getProperty(com.arjuna.orbportability.common.Environment.RESOLVE_SERVICE);

	if (resolveService != null)
	{
	    if (resolveService.compareTo("NAME_SERVICE") == 0)
		resolver = com.arjuna.orbportability.Services.NAME_SERVICE;
	    else
	    {
		if (resolveService.compareTo("BIND_CONNECT") == 0)
		    resolver = com.arjuna.orbportability.Services.BIND_CONNECT;
		else
		{
		    if (resolveService.compareTo("FILE") == 0)
			resolver = com.arjuna.orbportability.Services.FILE;
		    else
		    {
			if (resolveService.compareTo("RESOLVE_INITIAL_REFERENCES") == 0)
			    resolver = com.arjuna.orbportability.Services.RESOLVE_INITIAL_REFERENCES;
		    }
		}
	    }
	}

	try
	{
	    String[] params = new String[1];

	    params[0] = Services.otsKind;

	    org.omg.CORBA.Object obj = serv.getService(Services.transactionService, params, resolver);

	    params = null;
	    theOTS = TransactionFactoryHelper.narrow(obj);
	}
	catch (Exception e)
	{
	    System.err.println("Unexpected bind exception: "+e);
	    e.printStackTrace(System.err);
	    assertFailure();
	}

	System.out.println("Creating transaction.");
	
	try
	{
	    topLevelControl = theOTS.create(0);
	}
	catch (Exception e)
	{
	    System.err.println("Create call failed: "+e);
	    e.printStackTrace(System.err);
	    assertFailure();
	}

	assertSuccess();
	
	myOA.destroy();
	myORB.shutdown();
    }

    public static void main (String[] args)
    {
	TMTest test = new TMTest();
	
	test.run(args);
    }
    
}
