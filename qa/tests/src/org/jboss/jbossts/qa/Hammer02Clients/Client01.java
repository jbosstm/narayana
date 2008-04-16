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
 * $Id: Client01.java,v 1.2 2003/06/26 11:44:00 rbegg Exp $
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
 * $Id: Client01.java,v 1.2 2003/06/26 11:44:00 rbegg Exp $
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

public class Client01
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

			int numberOfOperations = Integer.parseInt(args[args.length - 2]);

// Modified 17/01/01 K Jones:  Third argument added to allow delay between operations.

			int delayMillis = Integer.parseInt(args[args.length - 1]);

			work(numberOfOperations, delayMillis);

			System.out.println("Passed");
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Client01.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client01.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}

	private static void work(int numberOfOperations, int delayMillis)
			throws Exception
	{
		int count = 0;
		for (int i = 0; i < numberOfOperations; i++)
		{
			if (operation())
			{
				count++;
			}
			Thread.sleep(delayMillis);
		}

		System.err.println("Work: done " + count + " of " + numberOfOperations);
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
					int x0 = Math.abs(_random.nextInt() % _matrixWidth);
					int y0 = Math.abs(_random.nextInt() % _matrixHeight);
					int x1 = Math.abs(_random.nextInt() % _matrixWidth);
					int y1 = Math.abs(_random.nextInt() % _matrixHeight);

					IntHolder srcValue = new IntHolder();
					IntHolder dstValue = new IntHolder();

					Control control = OTS.current().get_control();

					_matrix.get_value(x0, y0, srcValue, control);

					if (srcValue.value == 1)
					{
						_matrix.get_value(x1, y1, dstValue, control);

						if (dstValue.value == 0)
						{
							_matrix.set_value(x0, y0, 0, control);
							_matrix.set_value(x1, y1, 1, control);

							successful = true;
						}
					}
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
			System.err.println("Client01.operation: " + exception);
			exception.printStackTrace(System.err);
			throw exception;
		}

		return successful;
	}

	private static Matrix _matrix = null;
	private static int _matrixWidth = 0;
	private static int _matrixHeight = 0;

	private static Random _random = new Random();
}
