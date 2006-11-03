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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DefaultTimeout.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.timeout;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.extensions.*;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.common.Environment;

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

public class DefaultTimeout
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

	    String timeout = jtsPropertyManager.propertyManager.getProperty(Environment.DEFAULT_TIMEOUT);
	    int sleepTime = 0;
	    
	    if (timeout != null)
	    {
		try
		{
		    Integer i = new Integer(timeout);

		    sleepTime = i.intValue();
		}
		catch (Exception e)
		{
		    System.err.println("Invalid default transaction timeout "+timeout);
		    System.err.println("Caught exception: "+e);
		
		    System.exit(0);
		}
	    }
	    
	    System.out.println("Thread "+Thread.currentThread()+" starting transaction.");
	    
	    OTSManager.get_current().begin();

	    Thread.sleep(sleepTime*1000*2, 0);

	    System.out.println("Thread "+Thread.currentThread()+" committing transaction.");

	    OTSManager.get_current().commit(false);

	    System.out.println("Transaction committed. Timeout did not go off.");
	    System.out.println("Test did not complete successfully.");
	}
	catch (Exception e)
	{
	    System.out.println("Caught exception: "+e);
	    System.out.println("Timeout went off.");

	    System.out.println("Test completed successfully.");
	}

	myOA.destroy();
	myORB.shutdown();
    }

}

