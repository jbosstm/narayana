/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;

public class ErrorTestProcess
{
	public ErrorTestProcess(String command, int numberofresources, int crashpoint, int[] crashtype)
	{
		mComand = command + " " + numberofresources + " " + crashpoint;
		for (int i = 0; i < crashtype.length; i++)
		{
			mComand += " ";
			mComand += crashtype[i];
		}
		System.err.println("comand = " + mComand);

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
	 * or we would be if we did not have the clf error.
	 */
	private void getResults()
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
						checkResult(line);
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
						checkResult(line);
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
			System.out.println("process exception");
		}
		mFinished = true;
	}

	private void checkResult(String s)
	{
		int result = 0;
		try
		{
			result = Integer.parseInt(s);
		}
		catch (NumberFormatException nfe)
		{
			System.err.println(s);
			return;
		}
		mStatus = ActionStatus.stringForm(result);
	}

	public String getActionStatus()
	{
		return mStatus;
	}

	private String mStatus;
	private Process mProcess;
	private String mComand;

	public boolean mFinished = false;
}