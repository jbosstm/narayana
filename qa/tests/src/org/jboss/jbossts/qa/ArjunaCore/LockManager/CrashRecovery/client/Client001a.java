/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;
import org.jboss.jbossts.qa.Utils.CrashRecoveryDelays;

public class Client001a extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client001a test = new Client001a(args);
	}

	private Client001a(String[] args)
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

			//record should have been commited by recovery manager
			for (int j = 0; j < mNumberOfResources; j++)
			{
				if (mLockRecordList[j].getValue() != (mMaxIteration + 1))
				{
					Debug("value is incorrect: " + mLockRecordList[j].getValue());
					mCorrect = false;
				}
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client001a.test() :", e);
		}
	}

}