/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

/*
 * Created by IntelliJ IDEA.
 * User: peter craddock
 * Date: 12-Mar-02
 * Time: 11:05:00
 */
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class CrashService01
{
	public CrashService01(int i)
	{
		mNumberOfResources = i;
	}

	public void createCrashRecord(int crashpoint, int crashtype)
	{
		mCrashRecord = new CrashAbstractRecord(crashpoint, crashtype);
	}

	public void setupOper(String unqiueId)
	{
		mTransaction = (AtomicAction) AtomicAction.Current();
		if (mCrashRecord != null)
		{
			mTransaction.add(mCrashRecord);
		}

		mAbstractRecordList = new BasicAbstractRecord[mNumberOfResources];
		for (int i = 0; i < mNumberOfResources; i++)
		{
			mAbstractRecordList[i] = new BasicAbstractRecord(i, unqiueId);
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
		}
	}

	private int mNumberOfResources = 0;
	private BasicAbstractRecord[] mAbstractRecordList;
	private CrashAbstractRecord mCrashRecord = null;
	private AtomicAction mTransaction;
	private boolean mCorrect = true;
}