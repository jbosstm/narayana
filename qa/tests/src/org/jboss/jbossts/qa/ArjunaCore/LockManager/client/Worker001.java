/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.client;

import com.arjuna.ats.arjuna.AtomicAction;
import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class Worker001 extends Thread
{
	public Worker001(int iterations, int resources, BasicLockRecord[] records)
	{
		this(iterations, resources, records, 1);
	}

	public Worker001(int iterations, int resources, BasicLockRecord[] records, int id)
	{
		mMaxIteration = iterations;
		mNumberOfResources = resources;
		mLockRecordList = records;
		mId = id;
	}

	/**
	 * The main method of the class that will perform the work.
	 */
	public void run()
	{
		expectedValue = new int[mNumberOfResources];
		for (int j = 0; j < mNumberOfResources; j++)
		{
			expectedValue[j] = 0;
		}

		try
		{
			AtomicAction a = new AtomicAction();
			//start transaction
			a.begin();
			//add abstract record
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					expectedValue[j] += mLockRecordList[j].increase();
				}
			}
			//comit transaction
			a.commit();

			//start new AtomicAction
			AtomicAction b = new AtomicAction();
			b.begin();
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					mLockRecordList[j].increase();
				}
			}
			//abort transaction
			b.abort();
		}
		catch (Exception e)
		{
			mCorrect = false;
			qautil.debug("exception in worker001: ", e);
		}
	}

	public boolean isCorrect()
	{
		return mCorrect;
	}

	public int[] getExpectedValues()
	{
		return expectedValue;
	}

	private BasicLockRecord[] mLockRecordList;
	private int mMaxIteration;
	private int mNumberOfResources;
	private int[] expectedValue;
	private boolean mCorrect = true;
	private int mId = 0;
}