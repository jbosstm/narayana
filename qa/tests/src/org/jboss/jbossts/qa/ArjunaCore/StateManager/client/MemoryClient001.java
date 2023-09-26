/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.StateManager.client;

import org.jboss.jbossts.qa.ArjunaCore.StateManager.impl.BasicStateRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class MemoryClient001 extends BaseTestClient
{
	public static void main(String[] args)
	{
		MemoryClient001 test = new MemoryClient001(args);
	}

	private MemoryClient001(String[] args)
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
			BasicStateRecord[] mStateRecordList = new BasicStateRecord[mNumberOfResources];
			//set up abstract records
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mStateRecordList[i] = new BasicStateRecord();
			}

			startTx();
			//add abstract record
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					mStateRecordList[j].increase();
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
					mStateRecordList[j].increase();
				}
			}
			//abort transaction
			abort();

			getSecondReading();

			qaMemoryAssert();
		}
		catch (Exception e)
		{
			Fail("Error in MemoryClient001.test() :", e);
		}
	}

}