/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
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
