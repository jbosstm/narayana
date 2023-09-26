/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.client;

import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.TXBasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class MemoryClient002 extends BaseTestClient
{
	public static void main(String[] args)
	{
		MemoryClient002 test = new MemoryClient002(args);
	}

	private MemoryClient002(String[] args)
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
			//set up abstract records
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mLockRecordList[i] = new TXBasicLockRecord();
			}

			startTx();
			//add abstract record
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					mLockRecordList[j].increase();
				}
			}
			//comit transaction
			commit();

			//get first memory reading.
			getFirstReading();

			//start new AtomicAction
			startTx();
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					mLockRecordList[j].increase();
				}
			}
			//abort transaction
			abort();

			getSecondReading();

			qaMemoryAssert();
		}
		catch (Exception e)
		{
			Fail("Error in MemoryClient002.test() :", e);
		}
	}

}