/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.CrashAbstractRecord;
import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

public class Client007 extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client007 test = new Client007(args);
	}

	private Client007(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(2);
			setNumberOfResources(1);

			BasicLockRecord[] mLockRecordList = new BasicLockRecord[mNumberOfResources];
			int[] expectedValue = new int[mNumberOfResources];

			//set up abstract records
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mLockRecordList[i] = new BasicLockRecord();
				expectedValue[i] = 0;
			}

			//now create abstract record that will cause rollback
			CrashAbstractRecord mCrashObject = new CrashAbstractRecord(3, 1);

			//rather than commit/abort alternate transactions we will add this
			//record to alternate  transactions.
			//
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					//start transaction
					startTx();
					if (i % 2 == 0)
					{
						add(mCrashObject);
					}
					int incValue = mLockRecordList[j].increase();
					expectedValue[j] += (i % 2 == 0) ? 0 : incValue;
					commit();
				}
			}

			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					startTx();
					if (i % 2 == 0)
					{
						add(mCrashObject);
					}
					int incValue = mLockRecordList[j].increase();
					expectedValue[j] += (i % 2 == 0) ? 0 : incValue;
					commit();
				}
			}

			//check final values
			for (int i = 0; i < mNumberOfResources; i++)
			{
				//first test to see if increases have been run
				if (mLockRecordList[i].getValue() != expectedValue[i])
				{
					Debug("whilst checking the " + i + " resource the getvalue was: " + mLockRecordList[i].getValue() + " and we expected: " + expectedValue[i]);
					mCorrect = false;
					break;
				}
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client007.test() :", e);
		}
	}

}