/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.StateManager.client;

import com.arjuna.ats.arjuna.AtomicAction;
import org.jboss.jbossts.qa.ArjunaCore.StateManager.impl.TXBasicStateRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class Worker004 extends Thread
{
	public Worker004(int iterations, int resources)
	{
		this(iterations, resources, 1);
	}

	public Worker004(int iterations, int resources, int id)
	{
		mMaxIteration = iterations;
		mNumberOfResources = resources;

		//set up abstract records
		mStatetRecordList = new TXBasicStateRecord[mNumberOfResources];
		for (int i = 0; i < mNumberOfResources; i++)
		{
			mStatetRecordList[i] = new TXBasicStateRecord();
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
					//perform increase (this will enlist resource)
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
					//perform increase(this will enlist resource)
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

	private TXBasicStateRecord[] mStatetRecordList;
	private int mMaxIteration;
	private int mNumberOfResources;
	private boolean mCorrect = true;
	private int mId = 0;
}