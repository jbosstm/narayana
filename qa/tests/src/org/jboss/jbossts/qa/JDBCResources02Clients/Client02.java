/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.JDBCResources02Clients;

import org.jboss.jbossts.qa.JDBCResources02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.StringHolder;

public class Client02
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
			System.err.println("Client02.main: " + exception);
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
			System.err.println("Client02.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}