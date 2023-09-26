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

import jakarta.transaction.Status;
import jakarta.transaction.TransactionManager;

public class Test06
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

			int numberOfWorkers = Integer.parseInt(args[args.length - 2]);
			int numberOfTransactions = Integer.parseInt(args[args.length - 1]);

			jakarta.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

			Worker[] workers = new Worker[numberOfWorkers];

			for (int index = 0; index < workers.length; index++)
			{
				workers[index] = new Worker(numberOfTransactions, transactionManager);
			}

			for (int index = 0; index < workers.length; index++)
			{
				workers[index].start();
			}

			boolean correct = true;

			for (int index = 0; index < workers.length; index++)
			{
				workers[index].join();
				correct = correct && workers[index].isCorrect();
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
			System.err.print("Test06.main: ");
			exception.printStackTrace(System.err);
		}
		catch (Error error)
		{
			System.out.println("Failed");
			System.err.print("Test06.main: ");
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
			System.err.print("Test06.main: ");
			exception.printStackTrace(System.err);
		}
		catch (Error error)
		{
			System.err.print("Test06.main: ");
			error.printStackTrace(System.err);
		}
	}


	private static class Worker extends Thread
	{
		public Worker(int numberOfTransactions, TransactionManager transactionManager)
		{
			_numberOfTransactions = numberOfTransactions;
			_transactionManager = transactionManager;
		}

		public void run()
		{
			try
			{
				for (int index = 0; index < _numberOfTransactions; index++)
				{
					_correct = _correct && (_transactionManager.getTransaction() == null);
					_correct = _correct && (_transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION);

					_transactionManager.begin();

					_correct = _correct && (_transactionManager.getTransaction() != null);
					_correct = _correct && (_transactionManager.getStatus() == Status.STATUS_ACTIVE);

					if ((index % 2) == 0)
					{
						_transactionManager.commit();
					}
					else
					{
						_transactionManager.rollback();
					}
				}

				_correct = _correct && (_transactionManager.getTransaction() == null);
				_correct = _correct && (_transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION);
			}
			catch (Exception exception)
			{
				System.err.print("Test06.Worker.run: ");
				exception.printStackTrace(System.err);
				_correct = false;
			}
			catch (Error error)
			{
				System.err.print("Test06.Worker.run: ");
				error.printStackTrace(System.err);
				_correct = false;
			}
		}

		public boolean isCorrect()
		{
			return _correct;
		}

		private boolean _correct = true;
		private int _numberOfTransactions;
		private TransactionManager _transactionManager;
	}
}