/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package org.jboss.jbossts.qa.Utils;

import java.io.*;

public class CSFTestWrapper
{
	public static void main(String[] args)
	{
		CSFTestWrapper wrapper;

		wrapper = new CSFTestWrapper(args);
		wrapper.execute();
	}

	public CSFTestWrapper(String[] args)
	{
		if (args != null)
		{
			mDeploy = args[0];
		}

		mFile = new File(mDeploy);
		createFiles();
	}

	public void execute()
	{
		runComand();
	}

	public void createFiles()
	{
		String outputDirectory = mFile.getAbsolutePath();
		try
		{
			outputDirectory = outputDirectory.substring(0, outputDirectory.lastIndexOf("."));
			int startofchange = outputDirectory.indexOf("config");
			String s = outputDirectory.substring(0, startofchange);
			s = s + "res";
			s = s + outputDirectory.substring(startofchange + 6, outputDirectory.length());
			outputDirectory = s;
		}
		catch (StringIndexOutOfBoundsException siobe)
		{
			System.out.println("test name error");
		}

		try
		{
			File testDirectory = new File(outputDirectory);
			if (!testDirectory.isDirectory())
			{
				testDirectory.mkdirs();
			}

			mOutStream = new File(outputDirectory + File.separator + sTitle + "_out");
			if (!mOutStream.isFile())
			{
				mOutStream.createNewFile();
			}

			mErrStream = new File(outputDirectory + File.separator + sTitle + "_err");
			if (!mOutStream.isFile())
			{
				mErrStream.createNewFile();
			}
		}
		catch (IOException io)
		{
			System.out.println("create exception " + io);
		}

		try
		{
			mOutPrintWriter = new PrintStream(
					new BufferedOutputStream(
							new FileOutputStream(mOutStream)), true);
			mErrPrintWriter = new PrintStream(
					new BufferedOutputStream(
							new FileOutputStream(mErrStream)), true);
		}
		catch (Exception e)
		{
			System.out.println("print stream exception " + e);
		}

	}

	public void runComand()
	{
		Thread mMainThread = new Thread("comand thread " + sTitle)
		{
			public void run()
			{
				try
				{
					mComand = sEmbeddor + " " + mDeploy;
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
				outputToDisplay();
			}
		};
		mMainThread.start();
	}

	public void outputToDisplay()
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
						writeToLog(line, true);
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
						writeToLog(line, false);
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

		try
		{
			mProcess.waitFor();
		}
		catch (Exception e)
		{
			System.out.println("process exception");
		}

		mOutPrintWriter.close();
		mErrPrintWriter.close();
	}

	public void writeToLog(String s, boolean b)
	{
		if (b)
		{
			mOutPrintWriter.println(s);
		}
		else
		{
			mErrPrintWriter.println(s);
		}

		//all the qa system needs to see is passed
		if (s.endsWith(sPassedResult))
		{
			System.out.println("Passed");
			startTimer();
		}

		if (s.endsWith(sFailedResult))
		{
			System.out.println("Failed");
			startTimer();
		}
	}

	/**
	 * On some systems HP-UX the embeddor process is not ending so
	 * lets stop it here.
	 */
	private void startTimer()
	{
		try
		{
			//sleep for 15 seconds
			Thread.currentThread().sleep(15000);
			// if process has not stopped kill the process
			if (mProcess != null)
			{
				mProcess.destroy();
				mProcess = null;
			}
		}
		catch (Exception e)
		{
			System.err.println("error in sleep");
		}
	}

	private File mFile;
	private File mOutStream;
	private File mErrStream;
	private PrintStream mOutPrintWriter;
	private PrintStream mErrPrintWriter;
	private Process mProcess;
	private String mComand;
	private String mDeploy;
	private static String sTitle = "csf_embeddor";
	private static String sEmbeddor = "java com.hp.mwlabs.csf.embeddors.text.Server -deploymentURL";
	private static String sPassedResult = "ArjunaTest Passed";
	private static String sFailedResult = "ArjunaTest Failed";
}