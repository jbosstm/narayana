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
package org.jboss.jbossts.qa.ArjunaCore.LockManager.client;

import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord2;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

public class WorkerClient001 extends BaseTestClient
{
	public static void main(String[] args)
	{
		WorkerClient001 test = new WorkerClient001(args);
	}

	private WorkerClient001(String[] args)
	{
		super(args);

		if (args.length > 0 && args[0].equals("-newlock"))
		{
			System.out.println("Creating a lock per attempt");
			_newLock = true;
		}
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(3);
			setNumberOfResources(2);
			setNumberOfWorkers(1);

			//set up lockmanager records - if newLock use the implementation with new lock per iteration
			BasicLockRecord[] mLockRecordList = _newLock ? new BasicLockRecord[mNumberOfResources] : new BasicLockRecord2[mNumberOfResources];
			int expectedValue[] = new int[mNumberOfResources];
			for (int i = 0; i < mNumberOfResources; i++)
			{
				mLockRecordList[i] = _newLock ? new BasicLockRecord() : new BasicLockRecord2();
				expectedValue[i] = 0;
			}

			Worker001[] mWorkers = new Worker001[mNumberOfWorkers];
			for (int i = 0; i < mNumberOfWorkers; i++)
			{
				mWorkers[i] = new Worker001(mMaxIteration, mNumberOfResources, mLockRecordList, i);
				mWorkers[i].start();
			}

			try
			{
				//wait for threads to complete
				for (int i = 0; i < mNumberOfWorkers; i++)
				{
					mWorkers[i].join();
					//check for any exceptions
					if (!mWorkers[i].isCorrect())
					{
						Debug("worker " + i + " has encountered an exception");
						mCorrect = false;
					}

					int[] workerExpectedValue = mWorkers[i].getExpectedValues();

					for (int j = 0; j < expectedValue.length; j++)
					{
						expectedValue[j] += workerExpectedValue[j];
					}
				}
			}
			catch (Exception e)
			{
				mCorrect = false;
				Debug("exception in worker thread ", e);
			}

			//now check final values we are not expecting all increases
			// to be done but we do expect at least (mPercent) to have worked.
			for (int i = 0; i < mNumberOfResources; i++)
			{
				int endValue = mLockRecordList[i].getValue();
				double result = Math.abs(endValue - expectedValue[i]) / (double) expectedValue[i];
				if (result > mPercent)
				{
					Debug("resource " + i + " final value is incorrect: value =" + mLockRecordList[i].getValue() + " we expected = " + expectedValue[i] + " does not fall into the " + (mPercent * 100) + "% margin");
					mCorrect = false;
				}
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in WorkerClient001.test() :", e);
		}
	}

	private boolean _newLock = false;
}
