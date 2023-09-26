/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.OTSServerClients;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ORBServices;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;

public class Client12
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			TransactionFactory transactionFactory = null;


			String[] transactionFactoryParams = new String[1];
			transactionFactoryParams[0] = ORBServices.otsKind;

			transactionFactory = TransactionFactoryHelper.narrow(ORBServices.getService(ORBServices.transactionService, transactionFactoryParams));


			int numberOfControls = Integer.parseInt(args[args.length - 1]);

			boolean correct = true;
			Control[] controls = new Control[numberOfControls];

			for (int index = 0; correct && (index < controls.length); index++)
			{
				controls[index] = transactionFactory.create(0);

				correct = correct && (controls[index].get_coordinator().get_status() == Status.StatusActive);
			}

			for (int index = 0; correct && (index < controls.length); index++)
			{
				controls[index].get_terminator().rollback();
			}

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
			System.err.println("Client12.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client12.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}