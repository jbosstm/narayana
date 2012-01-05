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

package org.jboss.jbossts.qa.JDBCResources02Clients;

import org.jboss.jbossts.qa.JDBCResources02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.StringHolder;

public class Client06
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String infoTableIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			InfoTable infoTable = InfoTableHelper.narrow(ORBInterface.orb().string_to_object(infoTableIOR));

			boolean correct = true;

			OTS.current().begin();

			for (int index = 0; index < 10; index++)
			{
				String name = "Name_" + index;
				String value = "Value_" + index;

				try
				{
					infoTable.insert(name, value, OTS.current().get_control());
				}
				catch (Exception e)
				{
					correct = false;
					System.err.println("Error in insert : " + e);
					e.printStackTrace(System.err);
				}
			}

			OTS.current().commit(true);

			try
			{
				infoTable.update("Name_4", "Value_6", OTS.current().get_control());
			}
			catch (Exception e)
			{
				System.err.println("Ignoring error in update : " + e);
				e.printStackTrace(System.err);
			}

			OTS.current().begin();

			try
			{
				infoTable.update("Name_4", "Value_4", OTS.current().get_control());
			}
			catch (Exception e)
			{
				System.err.println("Ignoring error in update : " + e);
				e.printStackTrace(System.err);
			}

			OTS.current().commit(true);

			OTS.current().begin();

			for (int index = 0; correct && (index < 10); index++)
			{
				String name = "Name_" + index;
				String value = "Value_" + index;
				StringHolder valueHolder = new StringHolder();

				try
				{
					infoTable.select(name, valueHolder, OTS.current().get_control());
				}
				catch (Exception e)
				{
					System.err.println("Error in select : " + e);
					e.printStackTrace(System.err);
				}
				correct = correct && value.equals(valueHolder.value);
			}

			OTS.current().commit(true);

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
			System.err.println("Client06.main: " + exception);
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
			System.err.println("Client06.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}
