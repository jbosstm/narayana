/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;
import org.jboss.jbossts.qa.Utils.CrashRecoveryDelays;

public class Client002a extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client002a test = new Client002a(args);
	}

	private Client002a(String[] args)
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

            CrashRecoveryDelays.awaitRecoveryArjunaCore();			

			//restore objects from uid's
			BasicLockRecord[] mLockRecordList = new BasicLockRecord[mNumberOfResources];
			for (int j = 0; j < mNumberOfResources; j++)
			{
				String key = getResourceName("resource_" + j);
				try
				{
					mLockRecordList[j] = new BasicLockRecord(qautil.loadUid(key));
					qautil.clearUid(key);
				}
				catch (Exception e)
				{
					Debug("Error when reading uid store");
					mCorrect = false;
				}
			}

			//record should have been rolledback by recovery manager
			for (int j = 0; j < mNumberOfResources; j++)
			{
				if (mLockRecordList[j].getValue() != 1)
				{
					Debug("value is incorrect: " + mLockRecordList[j].getValue());
					mCorrect = false;
				}
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client002a.test() :", e);
		}
	}

}