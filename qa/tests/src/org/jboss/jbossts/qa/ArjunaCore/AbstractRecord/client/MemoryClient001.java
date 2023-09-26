/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.BasicAbstractRecord;
import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.Service01;
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

			BasicAbstractRecord[] mAbstractRecordList = new BasicAbstractRecord[mNumberOfResources];
			//set up abstract records
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mAbstractRecordList[i] = new BasicAbstractRecord();
			}

			//create container
			Service01 mService = new Service01(mNumberOfResources);

			startTx();
			mService.setupOper();
			mService.doWork(mMaxIteration);
			//comit transaction
			commit();

			//get first memory reading.
			getFirstReading();

			mService = new Service01(mNumberOfResources);

			//start new AtomicAction
			startTx();
			mService.setupOper();
			mService.doWork(mMaxIteration);
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