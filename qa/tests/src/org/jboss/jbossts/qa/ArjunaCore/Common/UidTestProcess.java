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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;

public class UidTestProcess
{
	public UidTestProcess(String command, int numberofuids)
	{
		mComand = command + " " + numberofuids;

		Thread mMainThread = new Thread("comand thread")
		{
			public void run()
			{
				try
				{
					mProcess = Runtime.getRuntime().exec(mComand);
				}
				catch (OutOfMemoryError ome)
				{
					System.out.println("Out of memeory end test = " + ome);
				}
				catch (IOException io)
				{
					System.out.println("runtime exception " + io);
				}
				getResults();
			}
		};
		mMainThread.start();
	}

	/**
	 * We are only bothered about the output stream.
	 */
	public void getResults()
	{
		Thread mOutReader = new Thread()
		{
			public void run()
			{
				try
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
					String line = br.readLine();
					while ((!isInterrupted() && line != null))
					{
						line = line.trim();
						mResults.add(line);
						line = br.readLine();
					}
				}
				catch (InterruptedIOException e)
				{
				}
				catch (Exception e)
				{
				}
			}
		};

		//we will process the error stream just in case
		Thread mErrReader = new Thread()
		{
			public void run()
			{
				try
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
					String line = br.readLine();
					while ((!isInterrupted() && line != null))
					{
						line = line.trim();
						System.out.println(line);
						line = br.readLine();
					}
				}
				catch (InterruptedIOException e)
				{
				}
				catch (Exception e)
				{
				}
			}
		};

		mOutReader.start();
		mErrReader.start();

		//wait for process to end.
		try
		{
			mProcess.waitFor();
		}
		catch (Exception e)
		{
			System.err.println("process exception");
		}
		mFinished = true;
	}

	private Process mProcess;
	private String mComand;

	public boolean mFinished = false;
	public ArrayList mResults = new ArrayList();
}
