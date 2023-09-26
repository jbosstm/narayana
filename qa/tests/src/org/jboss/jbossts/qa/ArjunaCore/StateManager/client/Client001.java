/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.StateManager.client;

import org.jboss.jbossts.qa.ArjunaCore.StateManager.impl.BasicStateRecord;
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

			BasicStateRecord[] mStateRecordList = new BasicStateRecord[mNumberOfResources];
			//set up abstract records
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mStateRecordList[i] = new BasicStateRecord();
			}

			startTx();
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					mStateRecordList[j].increase();
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
					mStateRecordList[j].increase();
				}
			}
			//abort transaction
			abort();

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
			Fail("Error in Client001.test() :", e);
		}
	}

}