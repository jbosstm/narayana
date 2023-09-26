/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package org.jboss.jbossts.qa.CrashRecovery13Clients;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;

public class Test01
{
	public static void main(String[] args)
	{
		System.setProperty("com.arjuna.ats.jta.xaRecoveryNode", "1");
		System.setProperty("XAResourceRecovery1", "com.hp.mwtests.ts.jta.recovery.DummyXARecoveryResource");

		try
		{
			RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);

			manager.scan();
			manager.scan();

			System.out.println("Passed.");
		}
		catch (Exception ex)
		{
			System.out.println("Failed.");
		}
		System.clearProperty("com.arjuna.ats.jta.xaRecoveryNode");
		System.clearProperty("XAResourceRecovery1");
	}

}