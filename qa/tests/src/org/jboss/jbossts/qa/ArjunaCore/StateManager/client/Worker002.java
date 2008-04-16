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
package org.jboss.jbossts.qa.ArjunaCore.StateManager.client;

import com.arjuna.ats.arjuna.AtomicAction;
import org.jboss.jbossts.qa.ArjunaCore.StateManager.impl.TXBasicStateRecord;
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
			qautil.debug("exception in worker002: ", e);
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
