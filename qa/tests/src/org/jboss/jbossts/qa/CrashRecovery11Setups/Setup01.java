/*
 * SPDX short identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery11Setups;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery11.*;
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
        boolean passed = true;

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

            System.out.println("databaseURL: "+databaseURL+" dynamicClass: "+databaseDynamicClass);

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

            String tableName = JDBCProfileStore.getTableName(databaseUser, "Service");

            try
            {
                System.err.println("DROP TABLE " + tableName);
                statement.executeUpdate("DROP TABLE " + tableName);
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
            System.err.println("CREATE TABLE " + tableName+" (Name VARCHAR(64), Value INTEGER)");
			statement.executeUpdate("CREATE TABLE " +tableName+" (Name VARCHAR(64), Value INTEGER)");

			statement.close();
			connection.close();
		}
		catch (Exception exception)
		{
            System.err.println("Setup01.main: " + exception);
            exception.printStackTrace(System.err);
            System.out.println("Failed");
            passed = false;
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
            System.err.println("Setup01.main: " + exception);
            System.out.println("Failed");
            exception.printStackTrace(System.err);
            passed = false;
		}

        if (passed)
		{
			System.out.println("Passed");
		}
		else
		{
			System.out.println("Failed");
		}
	}
}