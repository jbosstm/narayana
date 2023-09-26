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

public class Client05
{
	public static void main(String[] args)
	{
		jakarta.transaction.TransactionManager transactionManager = null;

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
			String databaseUser =
					JDBCProfileStore.databaseUser(profileName);
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

			transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

			transactionManager.begin();

			for (int index = 0; index < 10; index++)
			{
				String name = "Name_" + index;
				String value = "Value_" + index;

				try
				{
					infoTable.insert(name, value);
				}
				catch (Exception e)
				{
					correct = false;
					System.err.println("Error in insert : " + e);
					e.printStackTrace(System.err);
				}
			}

			transactionManager.commit();

			transactionManager.begin();

			try
			{
				infoTable.update("Name_4", "Value_6");
			}
			catch (Exception e)
			{
				correct = false;
				System.err.println("Error in update : " + e);
				e.printStackTrace(System.err);
			}

			transactionManager.commit();

			try
			{
				infoTable.update("Name_4", "Value_4");
			}
			catch (Exception e)
			{
				correct = false;
				System.err.println("Error in update : " + e);
				e.printStackTrace(System.err);
			}

			transactionManager.begin();

			for (int index = 0; correct && (index < 10); index++)
			{
				String name = "Name_" + index;
				String value = "Value_" + index;
				String newValue = "";

				try
				{
					newValue = infoTable.select(name);
				}
				catch (Exception e)
				{
				}

				correct = correct && value.equals(newValue);
			}

			transactionManager.commit();

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
			System.err.println("Client05.main: " + exception);
			exception.printStackTrace(System.err);
		}
		finally
		{
// code change to stop database locking
			try
			{
				if (transactionManager.getTransaction() != null)
				{
					transactionManager.rollback();
				}
			}
			catch (Exception e)
			{
				System.err.println("Finally has caught exception");
			}
		}
	}
}