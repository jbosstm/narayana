/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package org.jboss.jbossts.qa.CrashRecovery12Clients;

import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.jboss.jbossts.qa.CrashRecovery12Resources.Resource01;
import org.jboss.jbossts.qa.CrashRecovery12Resources.Resource02;

import jakarta.transaction.UserTransaction;

public class Client01
{
	public static String resultsFile = "Client01.log";

	public static void main(String[] args)
	{
		int crashIn = Resource01.NOCRASH;
		;

		if (args.length >= 1)
		{
			if (args[0].startsWith("p") || args[0].startsWith("P"))
			{
				crashIn = Resource01.PREPARE;
			}
			if (args[0].startsWith("c") || args[0].startsWith("C"))
			{
				crashIn = Resource01.COMMIT;
			}
			if (args[0].startsWith("r") || args[0].startsWith("R"))
			{
				crashIn = Resource01.ROLLBACK;
			}
		}
		if (args.length >= 2)
		{
			resultsFile = args[1];
		}

		try
		{
			ORB myORB = ORB.getInstance("Client01");
			RootOA myOA = OA.getRootOA(myORB);

			myORB.initORB(args, null);
			myOA.initOA();

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.out.println("Failed");
		}

		System.out.println("Ready");

		try
		{
			UserTransaction ut = com.arjuna.ats.jta.UserTransaction.userTransaction();

			ut.begin();

			TransactionImple txImple = (TransactionImple) TransactionManager.transactionManager().getTransaction();

			txImple.enlistResource(new Resource01(crashIn, resultsFile));
			txImple.enlistResource(new Resource02());

			ut.commit();
			System.out.println("Passed");
		}
		catch (jakarta.transaction.RollbackException rbx)
		{
			System.out.println("Passed");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.out.println("Failed");
		}

	}
}