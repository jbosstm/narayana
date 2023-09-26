/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.JDBCLocals01;

import org.jboss.jbossts.qa.JDBCLocals01Impls.InfoTable;
import org.jboss.jbossts.qa.JDBCLocals01Impls.JDBCInfoTableImpl01;
import org.jboss.jbossts.qa.JDBCLocals01Impls.JDBCInfoTableImpl02;
import org.jboss.jbossts.qa.Utils.JDBCProfileStore;

public class Client01
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
			int databaseTimeout = JDBCProfileStore.timeout(profileName);

			InfoTable infoTable = null;
			boolean tableTwo = false;

			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-table2"))
				{
					tableTwo = true;
				}
			}

			if (!tableTwo)
			{
				infoTable = new JDBCInfoTableImpl01(databaseURL, databaseUser, databasePassword, databaseDynamicClass, databaseTimeout);
			}
			else
			{
				infoTable = new JDBCInfoTableImpl02(databaseURL, databaseUser, databasePassword, databaseDynamicClass, databaseTimeout);
			}

			boolean correct = true;

			for (int index = 0; index < 10; index++)
			{
				String name = "Name_" + index;
				String value = "Value_" + index;

				infoTable.insert(name, value);
			}

			for (int index = 0; correct && (index < 10); index++)
			{
				String name = "Name_" + index;
				String value = "Value_" + index;
				String newValue = infoTable.select(name);

				correct = correct && value.equals(newValue);
			}

			if (correct)
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
			System.err.println("Client01.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}