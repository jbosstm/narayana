/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
//
// Copyright (C) 2001,
//
// Hewlett-Packard Company,
// HP Arjuna Labs.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: Test06.java,v 1.3 2004/03/19 14:34:36 nmcl Exp $
//

package org.jboss.jbossts.qa.JTA01Tests;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Test06.java,v 1.3 2004/03/19 14:34:36 nmcl Exp $
 */

/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Test06.java,v 1.3 2004/03/19 14:34:36 nmcl Exp $
 */


import org.jboss.jbossts.qa.Utils.Setup;

import javax.transaction.Status;
import javax.transaction.TransactionManager;

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

			javax.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

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
