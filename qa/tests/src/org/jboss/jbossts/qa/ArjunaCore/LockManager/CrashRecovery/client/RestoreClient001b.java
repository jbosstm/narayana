/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

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

			BasicLockRecord[] mLockRecordList = new BasicLockRecord[mNumberOfResources];
			//set up abstract records
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mLockRecordList[i] = new BasicLockRecord();
			}

			startTx();
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					mLockRecordList[j].increase();
				}
			}
			//comit transaction
			commit();

			//start new AtomicAction
			startTx();
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					mLockRecordList[j].increase();
				}
			}
			//abort transaction
			abort();

			//store uid of remote objects in ServerIORStore
			for (int j = 0; j < mNumberOfResources; j++)
			{
				String key = getResourceName("resource_" + j);
				try
				{
					qautil.storeUid(key, mLockRecordList[j].get_uid());
				}
				catch (Exception e)
				{
					Debug("Error when creating ior store");
					mCorrect = false;
				}
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in RestoreClient001b.test() :", e);
		}
	}

}