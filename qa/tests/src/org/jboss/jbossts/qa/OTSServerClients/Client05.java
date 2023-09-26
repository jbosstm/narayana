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
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;

public class Client05
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


			boolean correct;
			Control control = transactionFactory.create(4);

			Thread.sleep(8000);

			try
			{
				control.get_terminator().commit(false);
				correct = false;
			}
			catch (INVALID_TRANSACTION invalidTransaction)
			{
				correct = true;
			}
			catch (BAD_OPERATION badOperation)
			{
				correct = true;
			}
			catch (org.omg.CORBA.OBJECT_NOT_EXIST object_not_exist_exception)
			{
				// This test creates a transaction with timeout period of 4 seconds then
				// sleeps for 8 seconds.
				// When the timeout goes off at the transaction service, the transaction is
				// rolled back and destroyed.
				// The subsequent call to commit on the transaction results in an
				// org.omg.CORBA.OBJECT_NOT_EXIST exception being thrown.
				// The JTS specification appears to be quite vague in this area, however our
				// implementation is compliant with this vagueness.
				// Hence, For the purposes of this test, org.omg.CORBA.OBJECT_NOT_EXIST being thrown
				// does not indicate a failure - BD 20/06/01

				correct = true;
			}
			catch (Exception exception)
			{
				System.err.println("Client05.main: commit exception = " + exception);
				correct = false;
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
			System.err.println("Client05.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client05.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}