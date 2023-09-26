/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package org.jboss.jbossts.qa.CrashRecovery13Clients;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.jboss.jbossts.qa.CrashRecovery13Impls.ExampleXAResource;

public class Test03
{
	public static void main(String[] args) throws Exception
	{
		System.setProperty("XAConnectionRecovery1", "ExampleXAConnectionRecovery");

		ORB myORB = null;
		RootOA myOA = null;

		try
		{
			myORB = ORB.getInstance("test");
			myOA = OA.getRootOA(myORB);

			myORB.initORB(args, null);
			myOA.initOA();

			ORBManager.setORB(myORB);
			ORBManager.setPOA(myOA);
		}
		catch (Exception e)
		{
			System.err.println("Initialisation failed: " + e);

			System.exit(0);
		}

		com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple rm = new com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple(true);

		try
		{
			Thread.sleep(140000);
		}
		catch (Exception ex)
		{
		}

		if (ExampleXAResource.passed)
		{
			System.out.println("Passed.");
		}
		else
		{
			System.out.println("Failed.");
		}
		System.clearProperty("XAConnectionRecovery1");
	}
}