/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

/*
 * Created by IntelliJ IDEA.
 * User: peter craddock
 * Date: 12-Mar-02
 * Time: 11:28:36
 */
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class CrashService02
{
	public CrashService02(int res, int point, int type)
	{
		mNumberOfResources = res;
		mCrashPoint = point;
		mCrashType = type;
	}

	public void setupOper(String uniquePrefix)
	{
		mTransaction = (AtomicAction) AtomicAction.Current();

		mAbstractRecordList = new CrashAbstractRecord02[mNumberOfResources];
		for (int i = 0; i < mNumberOfResources; i++)
		{
			mAbstractRecordList[i] = new CrashAbstractRecord02(i, mCrashPoint, mCrashType, uniquePrefix);
			if (mTransaction.add(mAbstractRecordList[i]) != AddOutcome.AR_ADDED)
			{
				qautil.qadebug("Error when adding: " + i + " to atomic action");
				mCorrect = false;
			}
		}

	}

	public void doWork(int work)
	{
		for (int j = 0; j < mNumberOfResources; j++)
		{
			for (int i = 0; i < work; i++)
			{
				mAbstractRecordList[j].increase();
			}
			mAbstractRecordList[j].setAction(1);
		}
	}

	public int mCrashPoint;
	public int mCrashType;
	public int mNumberOfResources;
	public CrashAbstractRecord02[] mAbstractRecordList;
	public AtomicAction mTransaction;
	public boolean mCorrect = true;
}