/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



//

package org.jboss.jbossts.qa.JTA01Tests;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.Utils.Setup;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.Status;

public class Test02
{
	public static void main(String[] args)
	{
		Setup orbClass = null;

		try
		{
			boolean needOrb = true;

			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-local"))
				{
					needOrb = false;
				}
			}

			if (needOrb)
			{
				Class c = Thread.currentThread().getContextClassLoader().loadClass("org.jboss.jbossts.qa.Utils.OrbSetup");

				orbClass = (Setup) c.getDeclaredConstructor().newInstance();

				orbClass.start(args);
			}

			boolean correct = true;

			jakarta.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

			correct = correct && (transactionManager.getTransaction() == null);
			correct = correct && (transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION);

			transactionManager.begin();

			correct = correct && (transactionManager.getTransaction() != null);
			correct = correct && (transactionManager.getStatus() == Status.STATUS_ACTIVE);

			try
			{
				transactionManager.begin();
				correct = false;
			}
			catch (NotSupportedException notSupportedException)
			{
			}

			correct = correct && (transactionManager.getTransaction() != null);
			correct = correct && (transactionManager.getStatus() == Status.STATUS_ACTIVE);

			transactionManager.commit();

			correct = correct && (transactionManager.getTransaction() == null);
			correct = correct && (transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION);

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
			System.err.print("Test02.main: ");
			exception.printStackTrace(System.err);
		}
		catch (Error error)
		{
			System.out.println("Failed");
			System.err.print("Test02.main: ");
			error.printStackTrace(System.err);
		}

		try
		{
			if (orbClass != null)
			{
				orbClass.stop();
			}
		}
		catch (Exception exception)
		{
			System.err.print("Test02.main: ");
			exception.printStackTrace(System.err);
		}
		catch (Error error)
		{
			System.err.print("Test02.main: ");
			error.printStackTrace(System.err);
		}
	}
}