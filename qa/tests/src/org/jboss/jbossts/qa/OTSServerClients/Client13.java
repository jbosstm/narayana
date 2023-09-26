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

public class Client13
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


			int numberOfWorkers = Integer.parseInt(args[args.length - 2]);
			int numberOfControls = Integer.parseInt(args[args.length - 1]);

			boolean correct = true;

			Worker[] workers = new Worker[numberOfWorkers];

			for (int index = 0; index < workers.length; index++)
			{
				workers[index] = new Worker(numberOfControls, transactionFactory);
			}

			for (int index = 0; index < workers.length; index++)
			{
				workers[index].start();
			}

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
			System.err.println("Client13.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client13.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}

	private static class Worker extends Thread
	{
		public Worker(int numberOfControls, TransactionFactory transactionFactory)
		{
			_numberOfControls = numberOfControls;
			_transactionFactory = transactionFactory;
		}

		public void run()
		{
			try
			{
				for (int index = 0; _correct && (index < _numberOfControls); index++)
				{
					Control control = _transactionFactory.create(0);

					_correct = _correct && (control.get_coordinator().get_status() == Status.StatusActive);

					int option = index % 3;

					if (option == 0)
					{
						control.get_terminator().commit(true);
					}
					else if (option == 1)
					{
						control.get_terminator().commit(false);
					}
					else
					{
						control.get_terminator().rollback();
					}
				}
			}
			catch (Exception exception)
			{
				System.err.println("Client13.Worker.run: " + exception);
				exception.printStackTrace(System.err);
				_correct = false;
			}
		}

		public boolean isCorrect()
		{
			return _correct;
		}

		private boolean _correct = true;
		private int _numberOfControls;
		private TransactionFactory _transactionFactory = null;
	}
}