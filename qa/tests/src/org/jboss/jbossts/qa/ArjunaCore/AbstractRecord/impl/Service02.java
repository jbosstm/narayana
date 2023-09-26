/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

/*
 * Created by IntelliJ IDEA.
 * User: peter craddock
 * Date: 11-Mar-02
 * Time: 17:43:09
 */
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class Service02
{
	public Service02(int i)
	{
		mNumberOfResources = i;
	}

	/**
	 * do the same unit of work every time
	 */
	public void dowork(int workload)
	{
		for (int i = 0; i < workload; i++)
		{
			BasicAbstractRecord[] mAbstractRecordList = new BasicAbstractRecord[mNumberOfResources];
			mTransaction = new AtomicAction();
			mTransaction.begin();
			for (int j = 0; j < mNumberOfResources; j++)
			{
				mAbstractRecordList[j] = new BasicAbstractRecord();
				if (mTransaction.add(mAbstractRecordList[j]) != AddOutcome.AR_ADDED)
				{
					qautil.qadebug("Error when adding: " + i + " to atomic action");
					mCorrect = false;
				}
				mAbstractRecordList[j].increase();
			}
			if (i % 2 == 0)
			{
				mTransaction.commit();
			}
			else
			{
				mTransaction.abort();
			}
		}
	}

	private int mNumberOfResources = 0;
	private int mMaxIteration = 0;
	private boolean mCorrect = true;
	private BasicAbstractRecord[] mAbstractRecordList;
	private AtomicAction mTransaction = null;
}