/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.Service01;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

public class RestoreClient001a extends BaseTestClient
{
	public static void main(String[] args)
	{
		RestoreClient001a test = new RestoreClient001a(args);
	}

	private RestoreClient001a(String[] args)
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
			//restore objects from uid's
			mService.restoreUIDs(getUniquePrefix());
			//check if objects and final values have been restored.
			mCorrect = mService.checkRestore();

			mService.clearUIDs(getUniquePrefix());

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in RestoreClient001a.test() :", e);
		}
	}

}