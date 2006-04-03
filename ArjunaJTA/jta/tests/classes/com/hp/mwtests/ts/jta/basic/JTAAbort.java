/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTAAbort.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.basic;

import com.hp.mwtests.ts.jta.common.*;

import com.arjuna.ats.jta.*;
import com.arjuna.ats.jta.utils.*;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

import javax.transaction.*;
import javax.transaction.xa.*;

import java.lang.IllegalAccessException;

public class JTAAbort extends Test
{
	public void run(String[] args)
	{
		boolean passed = false;

		try
		{
			javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

			if (tm != null)
			{
				System.out.println("Starting top-level transaction.");

				tm.begin();

				javax.transaction.Transaction theTransaction = tm.getTransaction();

				if (theTransaction != null)
				{
					System.out.println("\nRolling back transaction.");

					theTransaction.rollback();

					System.out.println("\nTransaction now: " + theTransaction);

					System.out.println("\nThread associated: " + JTAHelper.stringForm(tm.getStatus()));

					theTransaction = tm.suspend();

					System.out.println("\nSuspended: " + theTransaction);

					try
					{
						tm.resume(theTransaction);

						System.out.println("\nResumed: " + tm.getTransaction());
						passed = true;
					}
					catch (InvalidTransactionException ite)
					{
						System.out.println("\nCould not resume a dead transaction.");
					}
				}
				else
				{
					System.err.println("Error - could not get transaction!");
					tm.rollback();
				}
			}
			else
				System.err.println("Error - could not get transaction manager!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (passed)
		{
			System.out.println("\nTest completed successfully.");
			assertSuccess();
		}
		else
		{
			System.out.println("\nTest did not complete successfully.");
			assertFailure();
		}
	}

	public static void main(String[] args)
	{
		JTAAbort test = new JTAAbort();
		test.initialise(null, null, args, new LocalHarness());
		test.runTest();
	}

}
