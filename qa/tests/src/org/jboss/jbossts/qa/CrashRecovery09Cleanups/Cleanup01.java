/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery09Cleanups;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery09.*;
import org.jboss.jbossts.qa.Utils.JDBCProfileStore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Cleanup01
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

			Connection connection = DriverManager.getConnection(databaseURL, databaseUser, databasePassword);
			Statement statement = connection.createStatement();

			statement.executeUpdate("DROP TABLE Service");

			statement.close();
			connection.close();
		}
		catch (Exception exception)
		{
			System.err.println("Cleanup01.main: " + exception);
		}
	}
}