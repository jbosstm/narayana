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
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Current;

public class Test20
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			boolean correct = true;

			int numberOfControls = 1000;

			Current current = OTS.get_current();

			String[] transactionNames = new String[numberOfControls];
			Control[] controls = new Control[numberOfControls];
			for (int index = 0; index < numberOfControls; index++)
			{
				current.begin();
				transactionNames[index] = current.get_transaction_name();
				controls[index] = current.suspend();
			}

			for (int index = 0; index < numberOfControls; index++)
			{
				current.resume(controls[index]);
				correct = correct && transactionNames[index].equals(current.get_transaction_name());
				current.commit(true);
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
			System.err.println("Test20.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Test20.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}