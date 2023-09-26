/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.Service01;
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

			Service01 mService = new Service01(mNumberOfResources);
			startTx();
			mService.setupOper(true);
			mService.doWork(mMaxIteration);
			commit();

			//get first memory reading.
			getFirstReading();

			mService = new Service01(mNumberOfResources);
			startTx();
			mService.setupOper(true);
			mService.doWork(mMaxIteration);
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