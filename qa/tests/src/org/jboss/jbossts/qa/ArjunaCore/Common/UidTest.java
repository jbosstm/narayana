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
package org.jboss.jbossts.qa.ArjunaCore.Common;

import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Simple test to see if Uid generation is unique.
 */
public class UidTest
{
	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			qautil.qadebug("runnig defaults");
			UidTest ut = new UidTest();
		}
		else
		{
			try
			{
				UidTest ut = new UidTest(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
			catch (NumberFormatException e)
			{
				qautil.debug("runnig defaults", e);
				UidTest ut = new UidTest();
			}
		}
	}

	public UidTest()
	{
		this(10, 1000);
	}

	/**
	 * To check this method we are going to start sub processes create a bunch of
	 * Uid's then check that the final list is unique.
	 */
	public UidTest(int threads, int uids)
	{
		qautil.qadebug("Running Uid test with param " + threads + ", " + uids);
		mNumberOfThreads = threads;
		mNumberOfUids = uids;

		mUidSubProcess = new UidTestProcess[mNumberOfThreads];

		//this will create the uidprocess objects and start
		//the sub process uidtestworker.
		qautil.qadebug("createing workers");
		for (int i = 0; i < mNumberOfThreads; i++)
		{
			mUidSubProcess[i] = new UidTestProcess(mCommand, mNumberOfUids);
		}

		qautil.qadebug("wait until workers finished");
		//wait until all subprocesses have finished
		boolean allfinished = false;
		while (!allfinished)
		{
			for (int i = 0; i < mNumberOfThreads; i++)
			{
				if (!mUidSubProcess[i].mFinished)
				{
					allfinished = false;
					break;
				}
				else
				{
					qautil.qadebug(i + ": Finished = " + mUidSubProcess[i].mFinished);
					allfinished = true;
				}
			}
			//go to sleep let other processes run
			try
			{
				Thread.currentThread().sleep(100);
			}
			catch (Exception e)
			{
			}
		}

		//gather all results
		for (int i = 0; i < mNumberOfThreads; i++)
		{
			Iterator iter = mUidSubProcess[i].mResults.iterator();
			while (iter.hasNext())
			{
				String s = (String) iter.next();
				mUidList.add(s);
			}
		}

		boolean correct = true;
		ArrayList unique = new ArrayList();
		//we now have a full list lets check for uniqueness.
		int uniqueSize = mUidList.size();
		qautil.qadebug("Number of Uids = " + uniqueSize);
		for (int i = 0; i < uniqueSize; i++)
		{
			String s = (String) mUidList.get(i);
			if (unique.contains(s))
			{
				qautil.debug("Found non unique uid = " + s + " at index: " + unique.indexOf(s));
				correct = false;
				break;
			}
			else
			{
				qautil.qadebug(s);
				unique.add(s);
			}
		}

		//just in case sub process has not run
		if (correct && uniqueSize == 0)
		{
			correct = false;
			qautil.debug("Sub process has returned zero lenth array");
		}

		if (correct)
		{
			System.out.println("Passed");
		}
		else
		{
			System.out.println("Failed");
		}
	}

	public int mNumberOfThreads;
	public int mNumberOfUids;
	public String mCommand = "java -Demma.verbosity.level=silent -cp " + System.getProperty("java.class.path") + " org.jboss.jbossts.qa.ArjunaCore.Common.UidTestWorker";
	public ArrayList mUidList = new ArrayList();
	private UidTestProcess[] mUidSubProcess;
}
