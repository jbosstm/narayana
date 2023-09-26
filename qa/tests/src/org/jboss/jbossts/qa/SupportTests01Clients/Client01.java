/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.SupportTests01Clients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.AITResources01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client01
{
	public static String status_text_from_int(int status)
	{
		switch (status)
		{
			case jakarta.transaction.Status.STATUS_ACTIVE:
				return ("STATUS_ACTIVE");
			case jakarta.transaction.Status.STATUS_COMMITTED:
				return ("STATUS_COMMITTED");
			case jakarta.transaction.Status.STATUS_COMMITTING:
				return ("STATUS_COMMITTING");
			case jakarta.transaction.Status.STATUS_MARKED_ROLLBACK:
				return ("STATUS_MARKED_ROLLBACK");
			case jakarta.transaction.Status.STATUS_NO_TRANSACTION:
				return ("STATUS_NO_TRANSACTION");
			case jakarta.transaction.Status.STATUS_PREPARED:
				return ("STATUS_PREPARED");
			case jakarta.transaction.Status.STATUS_PREPARING:
				return ("STATUS_PREPARING");
			case jakarta.transaction.Status.STATUS_ROLLEDBACK:
				return ("STATUS_ROLLEDBACK");
			case jakarta.transaction.Status.STATUS_ROLLING_BACK:
				return ("STATUS_ROLLING_BACK");
			case jakarta.transaction.Status.STATUS_UNKNOWN:
				return ("STATUS_UNKNOWN");
		}
		return ("!!ERROR!!");
	}

	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

			String counterIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			Counter counter = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR));

			tm.begin();

			jakarta.transaction.Transaction transaction = tm.getTransaction();

			counter.increase();

			tm.commit();

			System.err.println("Transaction Status (reported by actual transaction): " + status_text_from_int(transaction.getStatus()));
			System.err.println("Transaction Status (reported by transaction manager): " + status_text_from_int(tm.getStatus()));

			System.out.println("Passed");
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Client04.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client04.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}