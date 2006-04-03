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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTAOrder.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.basic;

import com.arjuna.ats.internal.jts.ORBManager;

import com.hp.mwtests.ts.jta.jts.common.*;

import com.arjuna.ats.jta.common.*;
import com.arjuna.ats.jta.*;
import com.arjuna.ats.jta.utils.*;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.orbportability.*;

import javax.transaction.*;
import javax.transaction.xa.*;

import java.lang.IllegalAccessException;

public class JTAOrder
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

	    System.exit(0);
	}

	boolean passed = false;

	jtaPropertyManager.propertyManager.setProperty(com.arjuna.ats.jta.common.Environment.JTA_TM_IMPLEMENTATION, "com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple");
	jtaPropertyManager.propertyManager.setProperty(com.arjuna.ats.jta.common.Environment.JTA_UT_IMPLEMENTATION, "com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple");
	
	try
	{
	    javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

	    XAResource theResource = new TestResource();
	    FirstXAResource first = new FirstXAResource();
	    LastXAResource last = new LastXAResource();
		
	    System.out.println("Starting top-level transaction.");

	    tm.begin();

	    javax.transaction.Transaction theTransaction = tm.getTransaction();

	    theTransaction.enlistResource(theResource);
	    theTransaction.enlistResource(last);
	    theTransaction.enlistResource(first);

	    System.err.println("Committing transaction.");
		
	    tm.commit();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	myOA.destroy();
	myORB.shutdown();
    }

}
