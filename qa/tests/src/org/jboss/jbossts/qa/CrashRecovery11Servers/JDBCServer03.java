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
import org.jboss.jbossts.qa.CrashRecovery11Impls.JDBCServiceImpl01;
import org.jboss.jbossts.qa.Utils.JDBCProfileStore;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class JDBCServer03
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

			JDBCServiceImpl01 jdbcServiceImpl1 = new JDBCServiceImpl01(args[args.length - 4], databaseURL, databaseUser, databasePassword, databaseDynamicClass);
			JDBCServiceImpl01 jdbcServiceImpl2 = new JDBCServiceImpl01(args[args.length - 3], databaseURL, databaseUser, databasePassword, databaseDynamicClass);

			BeforeCrashServicePOATie servant1 = new BeforeCrashServicePOATie(jdbcServiceImpl1);
			BeforeCrashServicePOATie servant2 = new BeforeCrashServicePOATie(jdbcServiceImpl2);

			OAInterface.objectIsReady(servant1);
			OAInterface.objectIsReady(servant2);
			BeforeCrashService service1 = BeforeCrashServiceHelper.narrow(OAInterface.corbaReference(servant1));
			BeforeCrashService service2 = BeforeCrashServiceHelper.narrow(OAInterface.corbaReference(servant2));


			ServerIORStore.storeIOR(args[args.length - 2], ORBInterface.orb().object_to_string(service1));
			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(service2));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("JDBCServer03.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}