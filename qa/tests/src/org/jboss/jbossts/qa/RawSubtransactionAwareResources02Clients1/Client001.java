/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.RawSubtransactionAwareResources02Clients1;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.RawSubtransactionAwareResources02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

public class Client001
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String serviceIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			Service service = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR));

			boolean correct = true;

			OTS.current().begin();

			OTS.current().begin();

			OTS.current().rollback_only();

			try
			{
				service.oper(1, OTS.current().get_control());
			}
			catch (TRANSACTION_ROLLEDBACK transactionRolledback)
			{
			}

			try
			{
				OTS.current().commit(true);
				correct = false;
			}
			catch (TRANSACTION_ROLLEDBACK transactionRolledback)
			{
			}

			OTS.current().commit(true);

			correct = correct && service.is_correct();

			if (correct)
			{
				System.out.println("Passed");
			}
			else
			{
				System.out.println("Failed");
			}
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Client001.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client001.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}