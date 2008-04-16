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

package org.jboss.jbossts.qa.JDBCResources04Clients;

import org.jboss.jbossts.qa.JDBCResources04.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.IntHolder;

public class Client01
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String numberTableIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			NumberTable numberTable = NumberTableHelper.narrow(ORBInterface.orb().string_to_object(numberTableIOR));

			boolean correct = true;

			OTS.current().begin();

			IntHolder valueHolder1 = new IntHolder();
			IntHolder valueHolder2 = new IntHolder();
			IntHolder valueHolder3 = new IntHolder();
			IntHolder valueHolder4 = new IntHolder();

			numberTable.get("Name_0", valueHolder1, OTS.current().get_control());
			numberTable.get("Name_1", valueHolder2, OTS.current().get_control());

			System.err.println("Phase 0, Name_0: " + valueHolder1.value);
			System.err.println("Phase 0, Name_1: " + valueHolder2.value);

			numberTable.increase("Name_0", OTS.current().get_control());

			Thread.sleep(15000);

			numberTable.increase("Name_1", OTS.current().get_control());

			numberTable.get("Name_0", valueHolder3, OTS.current().get_control());
			numberTable.get("Name_1", valueHolder4, OTS.current().get_control());

			System.err.println("Phase 1, Name_0: " + valueHolder3.value);
			System.err.println("Phase 1, Name_1: " + valueHolder4.value);

			OTS.current().commit(true);

			correct = (valueHolder1.value == valueHolder2.value) && (valueHolder3.value == valueHolder4.value) &&
					(valueHolder1.value == (valueHolder3.value - 1)) && (valueHolder2.value == (valueHolder4.value - 1));

			if (correct)
			{
				System.out.println("Passed");
			}
			else
			{
				System.out.println("Failed");
			}
		}
		catch (InvocationException exception)
		{
			// If the reason the exception was thrown was due to a 'can't serialize access'
			// exception then we have passed otherwise we have failed

			if (exception.myreason == Reason.ReasonCantSerializeAccess)
			{
				System.out.println("Passed");
			}
			else
			{
				System.out.println("Failed");
			}

			System.err.println("Client01.main: " + exception);
			exception.printStackTrace(System.err);
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Client01.main: " + exception);
			exception.printStackTrace(System.err);
		}
		finally
		{
// code change to stop database locking
			try
			{
				if (OTS.current().get_control() != null)
				{
					OTS.current().rollback();
				}
			}
			catch (Exception e)
			{
				System.err.println("Finally has caught exception");
				e.printStackTrace(System.err);
			}
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
}
