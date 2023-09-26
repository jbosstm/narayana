/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.Hammer01Outcomes;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.Hammer01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.IntHolder;

public class Outcome02
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String matrixIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			Matrix matrix = MatrixHelper.narrow(ORBInterface.orb().string_to_object(matrixIOR));

			int matrixWidth = matrix.get_width();
			int matrixHeight = matrix.get_height();

			int total = 0;
			for (int x = 0; x < matrixWidth; x++)
			{
				for (int y = 0; y < matrixHeight; y++)
				{
					IntHolder value = new IntHolder();

					matrix.get_value(x, y, value);

					total += value.value;
				}
			}

			if (total == (matrixWidth * matrixHeight / 2))
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