/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.Stats;

import com.arjuna.ats.arjuna.coordinator.TxStats;
import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.Service01;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

public class Client002 extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client002 test = new Client002(args);
	}

	private Client002(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(2);
			setNumberOfResources(1);

			Service01 mService = new Service01(mNumberOfResources);
			TxStats mStats = TxStats.getInstance();

			startTx();
			mService.setupOper(true);
			mService.doWork(mMaxIteration);
			commit();

			mService = new Service01(mNumberOfResources);
			//start new AtomicAction
			startTx();
			mService.setupOper(true);
			mService.doWork(mMaxIteration);
			abort();

			//test what the final stat values are
			if (mStats.getNumberOfAbortedTransactions() != 1)
			{
				Debug("error in number of aborted transactions: " + mStats.getNumberOfAbortedTransactions());
				mCorrect = false;
			}

			if (mStats.getNumberOfCommittedTransactions() != 3)
			{
				Debug("error in number of commited transactions: " + mStats.getNumberOfCommittedTransactions());
				mCorrect = false;
			}

			if (mStats.getNumberOfNestedTransactions() != 2)
			{
				Debug("error in number of nested transactions: " + mStats.getNumberOfNestedTransactions());
				mCorrect = false;
			}

			if (mStats.getNumberOfTransactions() != 4)
			{
				Debug("error in number of transactions: " + mStats.getNumberOfTransactions());
				mCorrect = false;
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client002.test() :", e);
		}
	}

}