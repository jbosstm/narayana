/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client;

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
			startTx();
			mService.setupOper(true);
			mService.doWork(mMaxIteration);
			commit();
			mCorrect = mService.checkCommitOper();

			mService = new Service01(mNumberOfResources);
			startTx();
			mService.setupOper(true);
			mService.doWork(mMaxIteration);
			abort();

			//check final values
			mCorrect = mCorrect && mService.checkAbortOper();

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client002.test() :", e);
		}
	}

}