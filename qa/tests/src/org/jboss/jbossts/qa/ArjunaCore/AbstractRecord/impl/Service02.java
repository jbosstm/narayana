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
