/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.JDBCResources01Outcomes;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.JDBCResources01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.StringHolder;

public class Outcome02
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

			for (int index = 0; correct && (index < 10); index++)
			{
				String name = "Name_" + index;
				String value = "Value_" + (9 - index);
				StringHolder valueHolder = new StringHolder();

				infoTable.select(name, valueHolder);

				correct = correct && value.equals(valueHolder.value);
				System.err.println("Name_" + index + " has a value of " + valueHolder.value);
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
			System.err.println("Outcome02.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Outcome02.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}