/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.client;

import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
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
			BasicLockRecord[] mLockRecordList = new BasicLockRecord[mNumberOfResources];
			int[] expectedValue = new int[mNumberOfResources];

			//set up abstract records
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mLockRecordList[i] = new BasicLockRecord();
				expectedValue[i] = 0;
			}

			startTx();
			//add abstract record
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					expectedValue[j] += mLockRecordList[j].increase();
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
			Fail("Error in Client001.test() :", e);
		}
	}

}