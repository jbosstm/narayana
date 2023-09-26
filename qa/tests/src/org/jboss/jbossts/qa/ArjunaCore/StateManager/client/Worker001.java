/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.StateManager.client;

import com.arjuna.ats.arjuna.AtomicAction;
import org.jboss.jbossts.qa.ArjunaCore.StateManager.impl.BasicStateRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class Worker001 extends Thread
{
	public Worker001(int iterations, int resources)
	{
		this(iterations, resources, 1);
	}

	public Worker001(int iterations, int resources, int id)
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
			AtomicAction a = new AtomicAction();
			//start transaction
			a.begin();
			for (int j = 0; j < mNumberOfResources; j++)
			{
				for (int i = 0; i < mMaxIteration; i++)
				{
					mStatetRecordList[j].increase();
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
					mStatetRecordList[j].increase();
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

	private BasicStateRecord[] mStatetRecordList;
	private int mMaxIteration;
	private int mNumberOfResources;
	private boolean mCorrect = true;
	private int mId = 0;
}