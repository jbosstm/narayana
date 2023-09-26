/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

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
					Debug("Error when creating ior store");
					mCorrect = false;
				}
			}

			//check if objects and final values have been restored.
			for (int j = 0; j < mNumberOfResources; j++)
			{
				if (mLockRecordList[j].getValue() != mMaxIteration)
				{
					mCorrect = false;
					Debug("the value has not been retored: " + mLockRecordList[j].getValue());
					break;
				}
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in RestoreClient001a.test() :", e);
		}
	}

}