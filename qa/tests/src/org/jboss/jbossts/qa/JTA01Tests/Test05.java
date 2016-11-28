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
// $Id: Test05.java,v 1.3 2004/03/19 14:34:36 nmcl Exp $
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
 * $Id: Test05.java,v 1.3 2004/03/19 14:34:36 nmcl Exp $
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
 * $Id: Test05.java,v 1.3 2004/03/19 14:34:36 nmcl Exp $
 */


import org.jboss.jbossts.qa.Utils.Setup;

import javax.transaction.Status;
import javax.transaction.Transaction;

public class Test05
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

			int numberOfTransactions = Integer.parseInt(args[args.length - 1]);

			boolean correct = true;

			javax.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

			Transaction[] transactions = new Transaction[numberOfTransactions];

			for (int index = 0; index < transactions.length; index++)
			{
				correct = correct && (transactionManager.getTransaction() == null);
				correct = correct && (transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION);

				transactionManager.begin();
				transactions[index] = transactionManager.suspend();
			}

			correct = correct && (transactionManager.getTransaction() == null);
			correct = correct && (transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION);

			for (int index = 0; index < transactions.length; index++)
			{
				transactionManager.resume(transactions[index]);

				correct = correct && (transactionManager.getTransaction() != null);
				correct = correct && (transactionManager.getStatus() == Status.STATUS_ACTIVE);

				if ((index % 2) == 0)
				{
					transactionManager.commit();
				}
				else
				{
					transactionManager.rollback();
				}
			}

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
			System.err.print("Test05.main: ");
			exception.printStackTrace(System.err);
		}
		catch (Error error)
		{
			System.out.println("Failed");
			System.err.print("Test05.main: ");
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
			System.err.print("Test05.main: ");
			exception.printStackTrace(System.err);
		}
		catch (Error error)
		{
			System.err.print("Test05.main: ");
			error.printStackTrace(System.err);
		}
	}
}
