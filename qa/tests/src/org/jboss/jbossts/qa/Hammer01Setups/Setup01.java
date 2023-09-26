/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.Hammer01Setups;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.Hammer01.*;
import org.jboss.jbossts.qa.Utils.JDBCProfileStore;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class Setup01
{
	public static void main(String[] args)
	{
		boolean success = false;

		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String profileName = args[args.length - 1];

			int numberOfDrivers = JDBCProfileStore.numberOfDrivers(profileName);
			for (int index = 0; index < numberOfDrivers; index++)
			{
				String driver = JDBCProfileStore.driver(profileName, index);

				Class.forName(driver);
			}

			String databaseURL = JDBCProfileStore.databaseURL(profileName);
			String databaseUser = JDBCProfileStore.databaseUser(profileName);
			String databasePassword = JDBCProfileStore.databasePassword(profileName);
			String databaseDynamicClass = JDBCProfileStore.databaseDynamicClass(profileName);

			Connection connection;
			if (databaseDynamicClass != null)
			{
				Properties databaseProperties = new Properties();

				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.userName, databaseUser);
				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.password, databasePassword);
				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.dynamicClass, databaseDynamicClass);

				connection = DriverManager.getConnection(databaseURL, databaseProperties);
			}
			else
			{
				connection = DriverManager.getConnection(databaseURL, databaseUser, databasePassword);
			}

			Statement statement = connection.createStatement();

			statement.executeUpdate("CREATE TABLE " + databaseUser + "_Matrix (X INT, Y INT, Value INT)");

			int width = 16;
			int height = 16;

			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					if (y < (height / 2))
					{
						statement.executeUpdate("INSERT INTO " + databaseUser + "_Matrix VALUES(\'" + x + "\', \'" + y + "\', \'0\')");
					}
					else
					{
						statement.executeUpdate("INSERT INTO " + databaseUser + "_Matrix VALUES(\'" + x + "\', \'" + y + "\', \'1\')");
					}
				}
			}

			statement.close();
			connection.close();

			success = true;
		}
		catch (Exception exception)
		{
			System.err.println("Setup01.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Setup01.main: " + exception);
			exception.printStackTrace(System.err);

			success = false;
		}

		System.out.println(success ? "Passed" : "Failed");
	}
}