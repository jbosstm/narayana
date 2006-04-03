/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: GridTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.local.grid;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.ORBManager;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;

public class GridTest
{
    
    public static void main (String[] args)
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
	    
	    TransactionFactoryImple theOTS = new TransactionFactoryImple();
	    Control myControl;
	    grid_i localGrid = new grid_i(100, 100);
	    int h, w, v;

	    myControl = theOTS.create(0);

	    if (myControl == null)
	    {
		System.err.println("Could not create transaction!");
		System.exit(-1);
	    }

	    h = localGrid.height();
	    w = localGrid.width();

	    localGrid.set( 2, 4, 123, myControl);
	    v = localGrid.get(2, 4, myControl);

	    // no problem setting and getting the elememt:
	    
	    System.out.println("grid[2,4] is "+v);

	    // sanity check: make sure we got the value 123 back:
	    if (v != 123)
	    {
		// oops - we didn't:
		System.err.println("something went seriously wrong");
		
		myControl.get_terminator().rollback();
	
		System.exit(1);
	    }
	    else
	    {
		Terminator handle = myControl.get_terminator();

		try
		{
		    if (handle != null)
		    {
			handle.commit(false);
		    }
		    else
			System.err.println("Error - no transaction terminator!");
		}
		catch (Exception ex)
		{
		    System.out.println("Test error! Caught: "+ex);
		}
	    }

	    ORBManager.getPOA().shutdownObject(theOTS);
	    ORBManager.getPOA().shutdownObject(localGrid);
	}
	catch (UserException e)
	{
	    System.err.println("Caught UserException: "+e);

	    e.printStackTrace();
	    System.exit(0);
	}
	catch (SystemException e)
	{
	    System.err.println("Caught SystemException: "+e);

	    e.printStackTrace();
	    System.exit(0);
	}

	System.out.println("\nWill now try different thread terminating transaction.\n");

	try
	{
	    org.omg.CosTransactions.Current current = OTSManager.get_current();
	    
	    System.out.println("Starting new transaction.");
	    
	    current.begin();

	    Control tc = current.get_control();

	    if (tc != null)
	    {
		System.out.println("Creating new thread.");
		
		TransactionalThread tranThread = new TransactionalThread(tc);

		System.out.println("Waiting for thread to terminate transaction.\n");
		
		tranThread.start();

		while (!tranThread.finished())
		    Thread.yield();

		System.out.println("\nCreator will now attempt to rollback transaction. Should fail.");

		try
		{
		    current.rollback();

		    System.out.println("Error - managed to rollback transaction!");
		}
		catch (NoTransaction e1)
		{
		    System.out.println("Correct termination - caught: "+e1);
		}
		catch (INVALID_TRANSACTION e2)
		{
		    System.out.println("Correct termination - caught: "+e2);
		}
		catch (Exception e3)
		{
		    System.out.println("Wrong termination - caught unexpected exception: "+e3);
		    e3.printStackTrace();
		    
		    System.exit(0);
		}

		System.out.println("Test completed successfully.");
	    }
	    else
		System.err.println("Error - null transaction control!");
	}
	catch (Exception e)
	{
	    System.out.println("Caught unexpected exception: "+e);
	}

	myOA.destroy();
	myORB.shutdown();
    }

}

