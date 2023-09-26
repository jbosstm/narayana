/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.StateManager.client;

import org.jboss.jbossts.qa.ArjunaCore.StateManager.impl.BasicStateRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

public class Client003 extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client003 test = new Client003(args);
	}

	private Client003(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(2);
			setNumberOfResources(1);

			BasicStateRecord[] mStateRecordList = new BasicStateRecord[mNumberOfResources];
			//set up abstract records
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mStateRecordList[i] = new BasicStateRecord();
			}

			//start first loop
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					//start transaction
					startTx();
					//perform increase
					mStateRecordList[j].increase();
					if (i % 2 == 0)
					{
						commit();
					}
					else
					{
						abort();
					}
				}
			}

			//start second loop
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					//start transaction
					startTx();
					//perform increase
					mStateRecordList[j].increase();
					if (i % 2 != 0)
					{
						commit();
					}
					else
					{
						abort();
					}
				}
			}

			//check final values
			for (int i = 0; i < mNumberOfResources; i++)
			{
				//first test to see if increases have been run
				if (mStateRecordList[i].getValue() != mMaxIteration)
				{
					Debug("whilst checking the " + i + " resource the getvalue was: " + mStateRecordList[i].getValue() + " and we expected: " + mMaxIteration);
					mCorrect = false;
					break;
				}
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client003.test() :", e);
		}
	}

}