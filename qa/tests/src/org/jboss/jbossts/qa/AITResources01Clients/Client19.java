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

package org.jboss.jbossts.qa.AITResources01Clients;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client19.java,v 1.2 2003/06/26 11:43:07 rbegg Exp $
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
 * $Id: Client19.java,v 1.2 2003/06/26 11:43:07 rbegg Exp $
 */


import com.arjuna.ats.jts.extensions.AtomicTransaction;
import org.jboss.jbossts.qa.AITResources01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.IntHolder;

public class Client19
{
	public static void main(String[] args)
	{

		boolean correct = true;
		int numberOfCalls = 10;
		Counter counter = null;

		System.err.println("Starting first init");

		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in first start: " + exception);
			exception.printStackTrace(System.err);
		}


		System.err.println("Starting first block");

		try
		{
			String counterIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			counter = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR));

			for (int index = 0; index < numberOfCalls; index++)
			{
				AtomicTransaction atomicTransaction = new AtomicTransaction();

				atomicTransaction.begin();

				counter.increase();

				if ((index % 2) == 0)
				{
					atomicTransaction.commit(true);
				}
				else
				{
					atomicTransaction.rollback();
				}
			}

			AtomicTransaction atomicTransaction = new AtomicTransaction();

			atomicTransaction.begin();

			IntHolder value = new IntHolder();
			counter.get(value);

			atomicTransaction.commit(true);

			if (value.value == (numberOfCalls / 2) && correct)
			{
				correct = true;
			}
			else
			{
				correct = false;
			}
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in first block" + exception);
			exception.printStackTrace(System.err);
		}

		System.err.println("Starting first shutdown");

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in first shutdown" + exception);
			exception.printStackTrace(System.err);
		}

		System.err.println("----Starting second block -------");

		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in second start " + exception);
			exception.printStackTrace(System.err);
		}

		System.err.println("init done starting second block");

		try
		{
			String counterIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			counter = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR));

			AtomicTransaction atomicTransaction = new AtomicTransaction();
			atomicTransaction.begin();
			counter.set(0);
			atomicTransaction.commit(true);
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in set operation " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			for (int index = 0; index < numberOfCalls; index++)
			{
				AtomicTransaction atomicTransaction = new AtomicTransaction();

				atomicTransaction.begin();

				counter.increase();

				if ((index % 2) == 0)
				{
					atomicTransaction.commit(true);
				}
				else
				{
					atomicTransaction.rollback();
				}
			}

		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in second loop block " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			AtomicTransaction atomicTransaction = new AtomicTransaction();

			atomicTransaction.begin();

			IntHolder value = new IntHolder();
			counter.get(value);

			atomicTransaction.commit(true);

			if (value.value == (numberOfCalls / 2) && correct)
			{
				correct = true;
			}
			else
			{
				correct = false;
			}
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("exception in second test " + exception);
			exception.printStackTrace(System.err);
		}

		System.err.println("Starting second shutdown");

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			correct = false;
			System.err.println("error in second shutdown" + exception);
			exception.printStackTrace(System.err);
		}

		System.err.println("testing result");
		if (correct)
		{
			System.out.println("Passed");
		}
		else
		{
			System.out.println("Failed");
		}
	}
}
