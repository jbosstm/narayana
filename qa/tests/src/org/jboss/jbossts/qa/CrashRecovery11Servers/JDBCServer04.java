/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.CrashRecovery11Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery11.*;
import org.jboss.jbossts.qa.CrashRecovery11Impls.JDBCServiceImpl02;
import org.jboss.jbossts.qa.Utils.JDBCProfileStore;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class JDBCServer04
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String profileName = args[args.length - 5];

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

			JDBCServiceImpl02 jdbcServiceImpl1 = new JDBCServiceImpl02(args[args.length - 4], databaseURL, databaseUser, databasePassword, databaseDynamicClass);
			JDBCServiceImpl02 jdbcServiceImpl2 = new JDBCServiceImpl02(args[args.length - 3], databaseURL, databaseUser, databasePassword, databaseDynamicClass);

			AfterCrashServicePOATie servant1 = new AfterCrashServicePOATie(jdbcServiceImpl1);
			AfterCrashServicePOATie servant2 = new AfterCrashServicePOATie(jdbcServiceImpl2);

			OAInterface.objectIsReady(servant1);
			OAInterface.objectIsReady(servant2);
			AfterCrashService service1 = AfterCrashServiceHelper.narrow(OAInterface.corbaReference(servant1));
			AfterCrashService service2 = AfterCrashServiceHelper.narrow(OAInterface.corbaReference(servant2));

			ServerIORStore.storeIOR(args[args.length - 2], ORBInterface.orb().object_to_string(service1));
			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(service2));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("JDBCServer04.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}