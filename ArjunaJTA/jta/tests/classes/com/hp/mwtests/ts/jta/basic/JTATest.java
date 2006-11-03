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

package com.hp.mwtests.ts.jta.basic;

import com.hp.mwtests.ts.jta.common.*;

import com.arjuna.ats.jta.*;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

import javax.transaction.*;
import javax.transaction.xa.*;

import java.lang.IllegalAccessException;

public class JTATest extends Test
{
	public void run(String[] args)
	{
		String xaResource = "com.hp.mwtests.ts.jta.common.DummyCreator";
		String connectionString = null;
		boolean tmCommit = true;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].compareTo("-connect") == 0)
				connectionString = args[i + 1];
			if (args[i].compareTo("-creator") == 0)
				xaResource = args[i + 1];
			if (args[i].equals("-txcommit"))
				tmCommit = false;
			if (args[i].compareTo("-help") == 0)
			{
				System.out.println("Usage: JTATest -creator <name> [-connect <string>] [-txcommit] [-help]");
				assertFailure();
			}
		}

		if (xaResource == null)
		{
			System.err.println("Error - no resource creator specified.");
			assertFailure();
		}

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
				System.err.println("Error - creator " + xaResource + " returned null resource.");
				assertFailure();
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
						System.err.println("Error - could not enlist resource in transaction!");
						tm.rollback();

						assertFailure();
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

						System.err.println("Error - transaction started!");
						assertFailure();
					}
					catch (Exception e)
					{
						System.out.println("Transaction did not begin: " + e);
					}

					/*
					 * Do some work and decide whether to commit or rollback.
					 * (Assume commit for example.)
					 */

					com.hp.mwtests.ts.jta.common.Synchronization s = new com.hp.mwtests.ts.jta.common.Synchronization();

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

					if ( s.getCurrentStatus() != com.hp.mwtests.ts.jta.common.Synchronization.AFTER_COMPLETION_STATUS )
					{
						System.err.println("Unexpected synchronization status: " + s.getCurrentStatus());
						assertFailure();
					}
				}
				else
				{
					System.err.println("Error - could not get transaction!");
					tm.rollback();
					assertFailure();
				}

				System.out.println("\nTest completed successfully.");
				assertSuccess();
			}
			else
			{
				System.err.println("Error - could not get transaction manager!");
				assertFailure();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFailure();
		}
	}

	public static void main(String[] args)
	{
		JTATest test = new JTATest();
		test.initialise(null, null, args, new LocalHarness());
		test.runTest();
	}

}
