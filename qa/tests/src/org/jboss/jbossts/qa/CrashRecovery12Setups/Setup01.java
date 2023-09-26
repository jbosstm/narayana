/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package org.jboss.jbossts.qa.CrashRecovery12Setups;

import org.jboss.jbossts.qa.CrashRecovery12Clients.Client01;

import java.io.File;

public class Setup01
{
	public static void main(String[] args)
	{
		String resultsFile = Client01.resultsFile;
		boolean passed = false;

		if (args.length >= 1)
		{
			resultsFile = args[0];
		}

		try
		{
			File f = new File(resultsFile);
			f.delete();
			if (!f.exists())
			{
				passed = true;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (passed)
		{
			System.out.println("Passed");
		}
		else
		{
			System.out.println("Failed");
		}

	}
}