/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jbossts.qa.ArjunaCore.Stats;

import com.arjuna.ats.arjuna.coordinator.TxStats;
import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.Service02;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

public class Client004 extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client004 test = new Client004(args);
	}

	private Client004(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(2);
			setNumberOfResources(1);

			TxStats mStats = new TxStats();
			int expectedCommitted = 0, expectedRolledback = 0, expectedNested = 0, expectedTx = 0;

			for (int j = 0; j < mNumberOfResources; j++)
			{
				//start transaction
				startTx();
				Service02 mService = new Service02(mNumberOfResources);
				mService.dowork(mMaxIteration);
				expectedCommitted += mMaxIteration / 2;
				expectedRolledback += mMaxIteration / 2;
				expectedNested += mMaxIteration;
				expectedTx += mMaxIteration + 1;
				if (j % 2 == 0)
				{
					commit();
					expectedCommitted++;
				}
				else
				{
					abort();
					expectedRolledback++;
				}
			}

			System.err.println("Number of resources = " + mNumberOfResources);
			System.err.println("Number of iterations = " + mMaxIteration);

			//test what the final stat values are
			if (mStats.numberOfAbortedTransactions() != expectedRolledback)
			{
				Debug("error in number of aborted transactions: " + mStats.numberOfAbortedTransactions() + " expected = " + expectedRolledback);
				mCorrect = false;
			}

			if (mStats.numberOfCommittedTransactions() != expectedCommitted)
			{
				Debug("error in number of commited transactions: " + mStats.numberOfCommittedTransactions() + " expected = " + expectedCommitted);
				mCorrect = false;
			}

			if (mStats.numberOfNestedTransactions() != expectedNested)
			{
				Debug("error in number of nested transactions: " + mStats.numberOfNestedTransactions() + " expected = " + expectedNested);
				mCorrect = false;
			}

			if (mStats.numberOfTransactions() != expectedTx)
			{
				Debug("error in number of transactions: " + mStats.numberOfTransactions() + " expected = " + expectedTx);
				mCorrect = false;
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client004.test() :", e);
		}
	}

}
