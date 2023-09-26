/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.client;

import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.TXBasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class MemoryClient004 extends BaseTestClient
{
	public static void main(String[] args)
	{
		MemoryClient004 test = new MemoryClient004(args);
	}

	private MemoryClient004(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(3);
			setNumberOfResources(2);
			getClientThreshold(1);

			TXBasicLockRecord[] mLockRecordList = new TXBasicLockRecord[mNumberOfResources];

			startStopWatch();

			//set up abstract records
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mLockRecordList[i] = new TXBasicLockRecord();
			}

			long stopWatchTime = stopStopWatch();
			System.err.println("Time taken to create records: " + stopWatchTime + "ms");

			for (int j = 0; j < mNumberOfResources; j++)
			{
				startStopWatch();
				for (int i = 0; i < mMaxIteration; i++)
				{
					//start transaction
					startTx();
					mLockRecordList[j].increase();
					if (i % 2 == 0)
					{
						commit();
					}
					else
					{
						abort();
					}
				}
				stopWatchTime = stopStopWatch();
				System.err.println("Time taken to increase resource " + mMaxIteration + " iteration(s): " + stopWatchTime + "ms");
			}

			//get first memory reading.
			getFirstReading();

			for (int j = 0; j < mNumberOfResources; j++)
			{
				startStopWatch();
				for (int i = 0; i < mMaxIteration; i++)
				{
					startTx();
					mLockRecordList[j].increase();
					if (i % 2 == 0)
					{
						commit();
					}
					else
					{
						abort();
					}
				}
				stopWatchTime = stopStopWatch();
				System.err.println("Time taken to increase resource " + mMaxIteration + " iteration(s): " + stopWatchTime + "ms");
			}

			getSecondReading();

			qaMemoryAssert();
		}
		catch (Exception e)
		{
			Fail("Error in MemoryClient004.test() :", e);
		}
	}

}