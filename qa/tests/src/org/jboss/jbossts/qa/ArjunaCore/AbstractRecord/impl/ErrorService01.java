/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

/*
 * Created by IntelliJ IDEA.
 * User: peter craddock
 * Date: 12-Mar-02
 * Time: 15:05:18
 */
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class ErrorService01
{
	public ErrorService01(int i)
	{
		mNumberOfResources = i;
	}

	public void setupOper()
			throws Exception
	{
		mTransaction = (AtomicAction) AtomicAction.Current();
		if (mTransaction == null)
		{
			throw new Exception("Transaction must be running");
		}

		qautil.qadebug("createing abstract records and enlisting them");
		mAbstractRecordList = new ErrorAbstractRecord[mNumberOfResources];
		//set up abstract records
		for (int i = 0; i < mNumberOfResources; i++)
		{
			mAbstractRecordList[i] = new ErrorAbstractRecord();
			if (mTransaction.add(mAbstractRecordList[i]) != AddOutcome.AR_ADDED)
			{
				qautil.debug("Error when adding: " + i + " to atomic action");
				mCorrect = false;
			}
		}
	}

	/**
	 * set all abstract records up the same.
	 */
	public void setCrash(int point, int type)
	{
		for (int i = 0; i < mNumberOfResources; i++)
		{
			mAbstractRecordList[i].setCrashPoint(point);
			mAbstractRecordList[i].setCrashType(type);
		}
	}

	/**
	 * set all records up with a different
	 */
	public void setCrash(int point, int[] type)
	{
		for (int i = 0; i < mNumberOfResources; i++)
		{
			mAbstractRecordList[i].setCrashPoint(point);
			if (i > type.length)
			{
				mAbstractRecordList[i].setCrashType(7);//default 'finished_ok'
			}
			else
			{
				mAbstractRecordList[i].setCrashType(type[i]);
			}
		}
	}

	private int mNumberOfResources = 0;
	private boolean mCorrect = true;
	private ErrorAbstractRecord[] mAbstractRecordList;
	private AtomicAction mTransaction = null;
}