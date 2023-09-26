/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client;

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

			//create container
			Service01 mService = new Service01(mNumberOfResources);

			startTx();
			mService.setupOper();
			mService.doWork(mMaxIteration);
			//comit transaction
			commit();
			mCorrect = mService.checkCommitOper();

			mService = new Service01(mNumberOfResources);

			//start new AtomicAction
			startTx();
			mService.setupOper();
			mService.doWork(mMaxIteration);
			//abort transaction
			abort();
			//check final values
			mCorrect = mCorrect && mService.checkAbortOper();

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client001.test() :", e);
		}
	}

}