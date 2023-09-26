/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.Service01;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

public class RestoreClient001b extends BaseTestClient
{
	public static void main(String[] args)
	{
		RestoreClient001b test = new RestoreClient001b(args);
	}

	private RestoreClient001b(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(3);
			setNumberOfResources(2);
			setUniquePrefix(1);

			Service01 mService = new Service01(mNumberOfResources);

			startTx();
			mService.setupOper();
			mService.doWork(mMaxIteration);
			//comit transaction
			commit();

			mService.storeUIDs(getUniquePrefix());

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in RestoreClient001b.test() :", e);
		}
	}

}