/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package org.jboss.jbossts.qa.CrashRecovery12Outcomes;

import org.jboss.jbossts.qa.CrashRecovery12Clients.Client01;
import org.jboss.jbossts.qa.Utils.CrashRecoveryDelays;

import java.io.BufferedReader;
import java.io.FileReader;

public class Outcome01
{
	public static void main(String[] args)
	{
		String resultsFile = args[0];
        boolean recoveryPassedExpected = "yes".equalsIgnoreCase(args[1]);

		boolean passed = false;
        boolean foundRecoveryPassed = false;
        boolean foundPassed = false;

		try
		{
            CrashRecoveryDelays.awaitRecoveryCR12();


			FileReader fr = new FileReader(resultsFile);
			BufferedReader br = new BufferedReader(fr);
			String line;

			while ((line = br.readLine()) != null)
			{
				System.err.println("Read: " + line);
				if ("Passed".equals(line))
				{
					foundPassed = true;
				}
				if ("Recovery Passed".equals(line))
				{
					foundRecoveryPassed = true;
				}
			}
			br.close();
		}
		catch (Exception ex)
		{
		}

		passed = recoveryPassedExpected ? foundRecoveryPassed : foundPassed;
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