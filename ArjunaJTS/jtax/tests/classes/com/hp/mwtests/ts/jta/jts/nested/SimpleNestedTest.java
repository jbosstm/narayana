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
 * $Id: SimpleNestedTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.nested;

import com.hp.mwtests.ts.jta.jts.common.TestResource;

import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.ats.jta.common.*;

import com.arjuna.orbportability.*;

import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

public class SimpleNestedTest
{
    public SimpleNestedTest ()
    {
        try
        {
            com.arjuna.ats.jta.common.jtaPropertyManager.propertyManager.setProperty(com.arjuna.ats.jta.common.Environment.SUPPORT_SUBTRANSACTIONS,"YES");

            javax.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

            transactionManager.begin();

	    transactionManager.begin();

            Transaction currentTrans = transactionManager.getTransaction();
            TestResource res1, res2;
            currentTrans.enlistResource( res1 = new TestResource() );
            currentTrans.enlistResource( res2 = new TestResource() );

            currentTrans.delistResource( res2, XAResource.TMSUCCESS );
            currentTrans.delistResource( res1, XAResource.TMSUCCESS );

            transactionManager.commit();

            transactionManager.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            System.err.println("ERROR - "+e);
        }
    }

    public static void main(String[] args)
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

	jtaPropertyManager.propertyManager.setProperty(com.arjuna.ats.jta.common.Environment.JTA_TM_IMPLEMENTATION, "com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple");
	jtaPropertyManager.propertyManager.setProperty(com.arjuna.ats.jta.common.Environment.JTA_UT_IMPLEMENTATION, "com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple");

        new SimpleNestedTest();

	myOA.destroy();
	myORB.shutdown();
    }
}
