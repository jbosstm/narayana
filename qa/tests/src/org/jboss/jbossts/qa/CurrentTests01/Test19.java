/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CurrentTests01;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.omg.CosTransactions.Current;

public class Test19
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			boolean correct = true;

			int numberOfTransactionNames = 1000;

			Current current = OTS.get_current();

			String[] transactionNames = new String[numberOfTransactionNames];
			for (int index = 0; index < numberOfTransactionNames; index++)
			{
				current.begin();
				transactionNames[index] = current.get_transaction_name();
				current.rollback();
			}

			for (int index1 = 0; index1 < numberOfTransactionNames - 1; index1++)
			{
				for (int index2 = index1 + 1; index2 < numberOfTransactionNames; index2++)
				{
					correct = correct && (!transactionNames[index1].equals(transactionNames[index2]));
				}
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
			System.err.println("Test19.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Test19.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}