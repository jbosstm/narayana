/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.StateManager.client;

import com.arjuna.ats.arjuna.AtomicAction;
import org.jboss.jbossts.qa.ArjunaCore.StateManager.impl.BasicStateRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class Worker003 extends Thread
{
	public Worker003(int iterations, int resources)
	{
		this(iterations, resources, 1);
	}

	public Worker003(int iterations, int resources, int id)
	{
		mMaxIteration = iterations;
		mNumberOfResources = resources;

		//set up abstract records
		mStatetRecordList = new BasicStateRecord[mNumberOfResources];
		for (int i = 0; i < mNumberOfResources; i++)
		{
			mStatetRecordList[i] = new BasicStateRecord();
		}
		mId = id;
	}

	/**
	 * The main method of the class that will perform the work.
	 */
	public void run()
	{
		try
		{
			//start first loop
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					//start transaction
					AtomicAction a = new AtomicAction();
					a.begin();
					//perform increase
					mStatetRecordList[j].increase();
					if (i % 2 == 0)
					{
						a.commit();
					}
					else
					{
						a.abort();
					}
				}
			}

			//start second loop
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					//start transaction
					AtomicAction b = new AtomicAction();
					b.begin();
					//perform increase
					mStatetRecordList[j].increase();
					if (i % 2 != 0)
					{
						b.commit();
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

	private BasicStateRecord[] mStatetRecordList;
	private int mMaxIteration;
	private int mNumberOfResources;
	private boolean mCorrect = true;
	private int mId = 0;
}