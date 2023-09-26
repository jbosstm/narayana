/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client;

import com.arjuna.ats.arjuna.AtomicAction;
import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.Service01;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class Worker002 extends Thread
{
	public Worker002(int iterations, int resources)
	{
		this(iterations, resources, 1);
	}

	public Worker002(int iterations, int resources, int id)
	{
		mMaxIteration = iterations;
		mNumberOfResources = resources;

		mService = new Service01(mNumberOfResources);
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
			mService.setupOper(true);
			mService.doWork(mMaxIteration);
			a.commit();

			mService = new Service01(mNumberOfResources);
			//start new AtomicAction
			AtomicAction b = new AtomicAction();
			b.begin();
			mService.setupOper(true);
			mService.doWork(mMaxIteration);
			b.abort();
		}
		catch (Exception e)
		{
			mCorrect = false;
			qautil.debug("exception in worker002: ", e);
		}
	}

	public boolean isCorrect()
	{
		return mCorrect;
	}

	private Service01 mService;
	private int mMaxIteration;
	private int mNumberOfResources;
	private boolean mCorrect = true;
	private int mId = 0;
}