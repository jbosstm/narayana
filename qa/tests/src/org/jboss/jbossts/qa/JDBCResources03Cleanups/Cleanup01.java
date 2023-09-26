/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.JDBCResources03Cleanups;

import org.jboss.jbossts.qa.JDBCResources03.*;
import org.jboss.jbossts.qa.Utils.JDBCProfileStore;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class Cleanup01
{
	public static void main(String[] args)
	{
		boolean success = false;
		boolean trying = true;
		int tries = 0;

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

			while (trying)
			{
				try
				{
					Statement statement = connection.createStatement();

					System.err.println("DROP TABLE " + databaseUser + "_NumberTable");
					statement.executeUpdate("DROP TABLE " + databaseUser + "_NumberTable");

					statement.close();
					connection.close();

					trying = false;
					success = true;
					/* Server might have crashed and table might still be busy. */
				}
				catch (java.sql.SQLException s)
				{
					System.err.println("Cleanup01.main: " + s);
					System.err.println("SQL state is: " + s.getSQLState());
					if (s.getSQLState() == "42000" ||	/* no table to drop */
							s.getSQLState() == "42S02" ||	/* table not found */
							s.getSQLState() == null)		/* connection failed */
					{
						trying = false;
					}
					else
					{
						tries++;
						if (tries >= 6)
						{
							trying = false;
							System.err.println("Giving up.");
						}
						else
						{
							try
							{
								System.err.println("Sleeping " + (tries * 10) + " seconds and re-trying ...");
								Thread.sleep(tries * 10000);
							}
							catch (Exception e)
							{
								System.err.println("Cleanup01.main: " + e);
								trying = false;
							}
						}
					}
				}
				catch (Exception e)
				{
					System.err.println("Cleanup01.main: " + e);
					trying = false;
				}
			}
		}
		catch (Exception exception)
		{
			System.err.println("Cleanup01.main: " + exception);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Cleanup01.main: " + exception);
			exception.printStackTrace(System.err);

			success = false;
		}

		System.out.println(success ? "Passed" : "Failed");
	}
}