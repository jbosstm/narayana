/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.JDBCResources03Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.JDBCResources03.*;
import org.jboss.jbossts.qa.JDBCResources03Impls.JDBCNumberTableImpl03;
import org.jboss.jbossts.qa.Utils.JDBCProfileStore;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server03
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String profileName = args[args.length - 2];

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
			int databaseTimeout = JDBCProfileStore.timeout(profileName);

			JDBCNumberTableImpl03 jdbcNumberTableImpl = new JDBCNumberTableImpl03(databaseURL, databaseUser, databasePassword, databaseDynamicClass, databaseTimeout);
			NumberTablePOATie servant = new NumberTablePOATie(jdbcNumberTableImpl);

			OAInterface.objectIsReady(servant);
			NumberTable jdbcNumberTable = NumberTableHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(jdbcNumberTable));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server02.main: " + exception);
		}
	}
}