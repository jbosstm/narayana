/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * $Id: JTATest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.basic;

import com.hp.mwtests.ts.jta.jts.common.*;

import com.arjuna.ats.jta.common.*;

import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.orbportability.*;

import javax.transaction.xa.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class JTATest
{
    @Test
    public void test() throws Exception
    {
	ORB myORB = null;
	RootOA myOA = null;

	    myORB = ORB.getInstance("test");
	    myOA = OA.getRootOA(myORB);
	    
	    myORB.initORB(new String[] {}, null);
	    myOA.initOA();

	    ORBManager.setORB(myORB);
	    ORBManager.setPOA(myOA);

	String xaResource = "com.hp.mwtests.ts.jta.common.DummyCreator";
	String connectionString = null;
	boolean tmCommit = true;

	jtaPropertyManager.getPropertyManager().setProperty(com.arjuna.ats.jta.common.Environment.JTA_TM_IMPLEMENTATION, "com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple");
	jtaPropertyManager.getPropertyManager().setProperty(com.arjuna.ats.jta.common.Environment.JTA_UT_IMPLEMENTATION, "com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple");

	/*
	 * We should have a reference to a factory object (see JTA
	 * specification). However, for simplicity we will ignore this.
	 */
	
	try
	{
	    XACreator creator = (XACreator) Thread.currentThread().getContextClassLoader().loadClass(xaResource).newInstance();
	    XAResource theResource = creator.create(connectionString, true);

	    if (theResource == null)
	    {
    		fail("Error - creator "+xaResource+" returned null resource.");
	    }

	    javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

	    if (tm != null)
	    {
		System.out.println("Starting top-level transaction.");
		
		tm.begin();
	    
		javax.transaction.Transaction theTransaction = tm.getTransaction();

		if (theTransaction != null)
		{
		    System.out.println("\nTrying to register resource with transaction.");
		    
		    if (!theTransaction.enlistResource(theResource))
		    {
			tm.rollback();
                fail("Error - could not enlist resource in transaction!");
		    }
		    else
			System.out.println("\nResource enlisted successfully.");
		    /*
		     * XA does not support subtransactions.
		     * By default we ignore any attempts to create such
		     * transactions. Appropriate settings can be made which
		     * will cause currently running transactions to also
		     * rollback, if required.
		     */
		    
		    System.out.println("\nTrying to start another transaction - should fail!");

		    try
		    {
			tm.begin();

			fail("Error - transaction started!");
		    }
		    catch (Exception e)
		    {
			System.out.println("Transaction did not begin: "+e);
		    }
		    
		    /*
		     * Do some work and decide whether to commit or rollback.
		     * (Assume commit for example.)
		     */

		    com.hp.mwtests.ts.jta.jts.common.Synchronization s = new com.hp.mwtests.ts.jta.jts.common.Synchronization();

		    tm.getTransaction().registerSynchronization(s);
		    
		    System.out.println("\nCommitting transaction.");

		    if (tmCommit)
			System.out.println("Using transaction manager.\n");
		    else
			System.out.println("Using transaction.\n");
		    
		    if (tmCommit)
			tm.commit();
		    else
			tm.getTransaction().commit();
		}
		else
		{
		    tm.rollback();
            fail("Error - could not get transaction!");
		}

		System.out.println("\nTest completed successfully.");
	    }
	    else
		System.err.println("Error - could not get transaction manager!");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	myOA.destroy();
	myORB.shutdown();
    }

}
