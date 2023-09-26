/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



//

package org.jboss.jbossts.qa.JDBCLocals01Setups;

import org.jboss.jbossts.qa.Utils.JDBCProfileStore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Properties;

public class Setup01
{
	public static void main(String[] args)
	{
		try
		{
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

			try
			{
				statement.executeUpdate("DROP TABLE " + databaseUser + "_InfoTable");
			}
			catch (java.sql.SQLException s)
			{
				if(!(s.getSQLState().startsWith("42") // old ms sql 2000 drivers
						|| s.getSQLState().equals("S0005") // ms sql 2005 drivers
						|| s.getSQLState().equals("ZZZZZ"))) // sybase jConnect drivers
				{
					System.err.println("Setup01.main: " + s);
					System.err.println("SQL state is: <" + s.getSQLState() + ">");
				}
			}

			statement.executeUpdate("CREATE TABLE " + databaseUser + "_InfoTable (Name VARCHAR(64), Value VARCHAR(64))");

			// Create an Index for the table just created. Microsoft SQL requires an index for Row Locking.
			statement.executeUpdate("CREATE UNIQUE INDEX " + databaseUser + "_IT_Ind " +
					"ON " + databaseUser + "_InfoTable (Name) ");


			statement.close();
			connection.close();

			System.out.println("Passed");
		}
		catch (Exception exception)
		{
			System.err.println("Setup01.main: " + exception);
			exception.printStackTrace(System.err);
			System.out.println("Failed");
		}
	}
}