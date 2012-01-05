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
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//

package org.jboss.jbossts.qa.Hammer02Clients;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client04.java,v 1.2 2003/06/26 11:44:00 rbegg Exp $
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
 * $Id: Client04.java,v 1.2 2003/06/26 11:44:00 rbegg Exp $
 */


import com.arjuna.ats.jts.extensions.AtomicTransaction;
import org.jboss.jbossts.qa.Hammer02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.IntHolder;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Status;

import java.util.Random;

public class Client04
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String matrixIOR = ServerIORStore.loadIOR(args[args.length - 3]);

			_matrix = MatrixHelper.narrow(ORBInterface.orb().string_to_object(matrixIOR));
			_matrixWidth = _matrix.get_width();
			_matrixHeight = _matrix.get_height();

			int numberOfWorkers = Integer.parseInt(args[args.length - 2]);
			int numberOfOperations = Integer.parseInt(args[args.length - 1]);

			Worker[] workers = new Worker[numberOfWorkers];

			for (int index = 0; index < workers.length; index++)
			{
				workers[index] = new Worker(numberOfOperations);
			}

			for (int index = 0; index < workers.length; index++)
			{
				workers[index].start();
			}

			for (int index = 0; index < workers.length; index++)
			{
				workers[index].join();
			}

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

	private static class Worker extends Thread
	{
		public Worker(int numberOfOperations)
		{
			_numberOfOperations = numberOfOperations;
		}

		public void run()
		{
			try
			{
				int count = 0;
				for (int i = 0; i < _numberOfOperations; i++)
				{
					if (operation())
					{
						count++;
					}
				}

				System.err.println("Work: done " + count + " of " + _numberOfOperations);
			}
			catch (Exception exception)
			{
				System.err.println("Client04.Worker.run: " + exception);
				exception.printStackTrace(System.err);
			}
		}

		private static boolean operation()
				throws Exception
		{
			boolean successful = false;

			try
			{
				AtomicTransaction atomicTransaction = new AtomicTransaction();

				try
				{
					atomicTransaction.begin();

					try
					{
						int d = Math.abs(_random.nextInt() % 10) + 1;

						int x0 = Math.abs(_random.nextInt() % _matrixWidth);
						int y0 = Math.abs(_random.nextInt() % _matrixHeight);
						int x1 = Math.abs(_random.nextInt() % _matrixWidth);
						int y1 = Math.abs(_random.nextInt() % _matrixHeight);

						IntHolder location0Value = new IntHolder();
						IntHolder location1Value = new IntHolder();

						Control control = OTS.current().get_control();

						_matrix.get_value(x0, y0, location0Value, control);
						_matrix.get_value(x1, y1, location1Value, control);

						_matrix.set_value(x0, y0, location1Value.value + d, control);
						_matrix.set_value(x1, y1, location0Value.value - d, control);

						successful = (x0 != x1) || (y0 != y1);
					}
					catch (InvocationException invocationException)
					{
						if (invocationException.myreason != Reason.ReasonConcurrencyControl)
						{
							throw invocationException;
						}
					}

					if (successful)
					{
						atomicTransaction.commit(true);
					}
					else
					{
						atomicTransaction.rollback();
					}
				}
				catch (Exception exception)
				{
					if (atomicTransaction.get_status() == Status.StatusActive)
					{
						atomicTransaction.rollback();
					}

					throw exception;
				}
			}
			catch (Exception exception)
			{
				System.err.println("Client04.Worker.operation: " + exception);
				exception.printStackTrace(System.err);
				throw exception;
			}

			return successful;
		}

		private int _numberOfOperations;
	}

	private static Matrix _matrix = null;
	private static int _matrixWidth = 0;
	private static int _matrixHeight = 0;

	private static Random _random = new Random();
}
