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
	# Copyright (C) 2001,
	#
	# Hewlett-Packard Company,
	# Newcastle upon Tyne,
	# Tyne and Wear,
	# UK.
*/

package org.jboss.jbossts.qa.Utils;

import com.arjuna.ats.arjuna.common.Configuration;
import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.arjuna.recovery.RecoveryConfiguration;

import java.io.*;

public class CSFToolTest
{
	public static void main(String[] args)
	{
		new CSFToolTest(args);
	}

	/**
	 * We may run into problems with the recovery manager file because we have not started the
	 * process and so not loaded in the recovery properties.
	 */
	public CSFToolTest(String[] args)
	{
		//first init orb to load properties
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();
		}
		catch (Exception e)
		{
			mCorrect = false;
			System.err.println("Exception in init: " + e);
			e.printStackTrace(System.err);
		}

		// now get values from static variables

		String mTSPropFile = System.getProperty(Environment.PROPERTIES_FILE);
		if (mTSPropFile == null)
		{
			mTSPropFile = Configuration.propertiesDir() + File.separator + Configuration.propertiesFile();
		}

		//we may have problems here
		String mTSRecPropFile = RecoveryConfiguration.recoveryManagerPropertiesFile();

		System.err.println("prop1 = " + mTSPropFile);
		System.err.println("prop2 = " + mTSRecPropFile);

		//create first config file
		mComand = "java PropertyFile2XML -file " + mTSPropFile + " -xml " + sPropFileName;
		System.err.println("running 1");
		System.err.println(mComand);
		runComand();

		//create second config file
		mComand = "java PropertyFile2XML -file " + mTSRecPropFile + " -xml " + sRecPropFileName;
		System.err.println("running 2");
		System.err.println(mComand);
		runComand();

		//now check file exist
		File f = new File(sPropFileName);
		if (!f.exists())
		{
			System.err.println(sPropFileName + "file does not exist");
			mCorrect = false;
		}

		f = new File(sRecPropFileName);
		if (!f.exists())
		{
			System.err.println(sRecPropFileName + "file does not exist");
			mCorrect = false;
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			mCorrect = false;
			System.err.println("Exception in shutdown: " + exception);
			exception.printStackTrace(System.err);
		}

		if (mCorrect)
		{
			System.out.println("Passed");
		}
		else
		{
			System.out.println("Failed");
		}

	}

	public void runComand()
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
			mCorrect = false;
			System.out.println("runtime exception " + io);
		}
		outputToDisplay();
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
			System.err.println("waiting for process to complete");
			mProcess.waitFor();
		}
		catch (Exception e)
		{
			mCorrect = false;
			System.out.println("process exception");
		}
	}

	public void writeToLog(String s, boolean b)
	{
		if (b)
		{
			System.out.println(s);
		}
		else
		{
			System.err.println(s);
		}
	}


	private Process mProcess;
	private String mComand;
	private boolean mCorrect = true;
	private static String sPropFileName = "arjuna.config";
	private static String sRecPropFileName = "arjuna_recovery.config";
}
