/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.StateManager.client;

import org.jboss.jbossts.qa.ArjunaCore.StateManager.impl.TXBasicStateRecord;
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

			TXBasicStateRecord[] mStateRecordList = new TXBasicStateRecord[mNumberOfResources];
			//set up abstract records
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mStateRecordList[i] = new TXBasicStateRecord();
			}

			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					//start transaction
					startTx();
					mStateRecordList[j].increase();
					if (i % 2 == 0)
					{
						commit();
					}
					else
					{
						abort();
					}
				}
			}

			//get first memory reading.
			getFirstReading();

			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					startTx();
					mStateRecordList[j].increase();
					if (i % 2 == 0)
					{
						commit();
					}
					else
					{
						abort();
					}
				}
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