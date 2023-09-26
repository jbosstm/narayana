/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.JDBCResources02Outcomes;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.JDBCResources02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.StringHolder;

public class Outcome03
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String infoTableIOR1 = ServerIORStore.loadIOR(args[args.length - 2]);
			InfoTable infoTable1 = InfoTableHelper.narrow(ORBInterface.orb().string_to_object(infoTableIOR1));

			String infoTableIOR2 = ServerIORStore.loadIOR(args[args.length - 1]);
			InfoTable infoTable2 = InfoTableHelper.narrow(ORBInterface.orb().string_to_object(infoTableIOR2));

			boolean correct = true;

			for (int index = 0; correct && (index < 10); index++)
			{
				String name = "Name_" + index;
				String value = "Value_" + index;
				StringHolder valueHolder1 = new StringHolder();
				StringHolder valueHolder2 = new StringHolder();

				infoTable1.select(name, valueHolder1, OTS.current().get_control());
				infoTable2.select(name, valueHolder2, OTS.current().get_control());

				correct = correct && value.equals(valueHolder1.value) && value.equals(valueHolder2.value);
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
			System.err.println("Outcome03.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Outcome03.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}