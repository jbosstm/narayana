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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Outcome01.java,v 1.3 2004/11/02 10:11:06 kconner Exp $
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
