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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionTest2.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.transactions;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.extensions.*;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.common.Environment;
import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.ORBManager;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CosTransactions.WrongTransaction;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

public class TransactionTest2
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
	}
	catch (Exception e)
	{
	    System.err.println("Initialisation failed: "+e);
	}

	int count = 0;

	System.out.println("Testing memory allocation.");
	System.out.println("Creating as many transactions as possible.\n");
	    
	try
	{
	    for (;;)
	    {
		OTSManager.get_current().begin();
		count++;
	    }
	}
	catch (Exception e)
	{
	    System.err.println("begin caught: "+e);

	    System.gc();
	}
	catch (Error e)
	{
	    System.err.println("begin caught: "+e);
	    e.printStackTrace();

	    System.gc();
	}	

	System.out.println("\nbegan: "+count);

	try
	{
	    int created = count;

	    System.out.println("\nNow rolling back transactions.");
	    
	    for (int i = 0; i < created; i++)
	    {
		try
		{
		    System.out.println(""+count);
		    OTSManager.get_current().rollback();
		    count--;
		}
		catch (OutOfMemoryError em)
		{
		    em.printStackTrace();
		    
		    System.gc();
		}
	    }
	}
	catch (Exception e)
	{
	    System.err.println("rollback caught: "+e);

	    System.gc();
	}
	catch (Error e)
	{
	    System.err.println("rollback caught: "+e);
	    e.printStackTrace();

	    System.gc();
	}

	System.out.println("\nStill to rollback: "+count);

	myOA.destroy();
	myORB.shutdown();
    }
 
}
