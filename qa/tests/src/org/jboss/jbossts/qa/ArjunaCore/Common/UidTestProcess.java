/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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