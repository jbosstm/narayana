/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.EnlistDelistEnlist01Servers;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.EnlistDelistEnlist01.*;
import org.jboss.jbossts.qa.EnlistDelistEnlist01Impls.EnlistDelistEnlistImpl01;
import org.jboss.jbossts.qa.Utils.JDBCProfileStore;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;


public class Server01
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String profileName = args[args.length - 2];

			//
			// from the JNDI profile...
			//
			String binding = JDBCProfileStore.binding(profileName);

			//
			// ..and from the JDBC equivalent
			//
			String databaseUser = JDBCProfileStore.databaseUser(profileName);
			String databasePassword = JDBCProfileStore.databasePassword(profileName);

			EnlistDelistEnlistImpl01 enlistDelistEnlistImpl = new EnlistDelistEnlistImpl01(binding, databaseUser, databasePassword);
			ServicePOATie servant = new ServicePOATie(enlistDelistEnlistImpl);

			OAInterface.objectIsReady(servant);
			Service enlistDelistEnlist = ServiceHelper.narrow(OAInterface.corbaReference(servant));

			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(enlistDelistEnlist));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server01.main: " + exception);
		}
	}
}