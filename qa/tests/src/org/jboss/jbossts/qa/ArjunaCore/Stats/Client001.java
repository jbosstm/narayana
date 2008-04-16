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
import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.Service01;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

public class Client001 extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client001 test = new Client001(args);
	}

	private Client001(String[] args)
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

			startTx();
			//add abstract record
			Service01 mService = new Service01(mNumberOfResources);
			mService.setupOper();
			mService.doWork(mMaxIteration);
			//comit transaction
			commit();

			mService = new Service01(mNumberOfResources);
			//start new AtomicAction
			startTx();
			mService.setupOper();
			mService.doWork(mMaxIteration);
			//abort transaction
			abort();

			if (mStats.numberOfAbortedTransactions() != 1)
			{
				Debug("error in number of aborted transactions: " + mStats.numberOfAbortedTransactions());
				mCorrect = false;
			}

			if (mStats.numberOfCommittedTransactions() != 1)
			{
				Debug("error in number of commited transactions: " + mStats.numberOfCommittedTransactions());
				mCorrect = false;
			}

			if (mStats.numberOfNestedTransactions() != 0)
			{
				Debug("error in number of nested transactions: " + mStats.numberOfNestedTransactions());
				mCorrect = false;
			}

			if (mStats.numberOfTransactions() != 2)
			{
				Debug("error in number of transactions: " + mStats.numberOfTransactions());
				mCorrect = false;
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client001.test() :", e);
		}
	}

}
