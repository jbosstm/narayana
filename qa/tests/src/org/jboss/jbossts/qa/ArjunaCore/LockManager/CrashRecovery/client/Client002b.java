/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.CrashAbstractRecord;
import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class Client002b extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client002b test = new Client002b(args);
	}

	private Client002b(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(5);
			setNumberOfResources(4);
			setCrashPoint(3);
			setCrashType(2);
			setUniquePrefix(1);

			BasicLockRecord[] mLockRecordList = new BasicLockRecord[mNumberOfResources];
			//set up lock records and store away uids
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mLockRecordList[i] = new BasicLockRecord(i);
				String key = getResourceName("resource_" + i);
				try
				{
					qautil.storeUid(key, mLockRecordList[i].get_uid());
				}
				catch (Exception e)
				{
					Debug("Error when creating ior store", e);
					mCorrect = false;
				}
			}

			// Create crash record last so record is processed last. We want the
			// crash to occur after prepare has been called on the lockmanager objects.
			CrashAbstractRecord mCrashObject = new CrashAbstractRecord(mCrashPoint, mCrashType);

			//start transaction	to check all is ok.
			startTx();
			for (int j = 0; j < mNumberOfResources; j++)
			{
				mLockRecordList[j].increase();
			}
			commit();

			//start new AtomicAction
			startTx();
			add(mCrashObject);
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					mLockRecordList[j].increase();
				}
			}
			commit();

			//we do not need to do anything else it should finish here if not print failed
			Fail();
		}
		catch (Exception e)
		{
			Fail("Error in Client002b.test() :", e);
		}
	}

}