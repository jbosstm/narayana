/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.client;

import com.arjuna.ats.arjuna.AtomicAction;
import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class Worker003 extends Thread
{
	public Worker003(int iterations, int resources, BasicLockRecord[] records)
	{
		this(iterations, resources, records, 1);
	}

	public Worker003(int iterations, int resources, BasicLockRecord[] records, int id)
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
		for (int j = 0; j < expectedValue.length; j++)
		{
			expectedValue[j] = 0;
		}

		try
		{
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					//start transaction
					AtomicAction a = new AtomicAction();
					a.begin();
					int incValue = mLockRecordList[j].increase();
					if (i % 2 == 0)
					{
						a.commit();
						expectedValue[j] += incValue;
					}
					else
					{
						a.abort();
					}
				}
			}

			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					AtomicAction b = new AtomicAction();
					b.begin();
					int incValue = mLockRecordList[j].increase();
					if (i % 2 == 0)
					{
						b.commit();
						expectedValue[j] += incValue;
					}
					else
					{
						b.abort();
					}
				}
			}
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
	private int[] expectedValue;
	private int mMaxIteration;
	private int mNumberOfResources;
	private boolean mCorrect = true;
	private int mId = 0;//default value
}