/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



//

package org.jboss.jbossts.qa.Utils;

import java.io.File;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

public class EmptyObjectStore
{
	public static void main(String[] args)
	{
		Setup orbClass = null;

		try
		{
			boolean needOrb = true;

			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-local"))
				{
					needOrb = false;
				}
			}

			if (needOrb)
			{
				Class c = Thread.currentThread().getContextClassLoader().loadClass("org.jboss.jbossts.qa.Utils.OrbSetup");

				orbClass = (Setup) c.getDeclaredConstructor().newInstance();

				orbClass.start(args);
			}

            ObjectStoreEnvironmentBean storeEnvBean = BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, null);

			if (storeEnvBean.getObjectStoreType() != null && storeEnvBean.getObjectStoreType().contains("JDBCStore"))
			{
                // ensure that all relevant JDBC store tables are cleared
                BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "stateStore").setDropTable(true);
                BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "stateStore").setCreateTable(true);
                BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setDropTable(true);
                BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setCreateTable(true);

                storeEnvBean.setDropTable(true);
                storeEnvBean.setCreateTable(true);

                // the first get on a store initializes it (which, for a JDBC store, includes table reinitialization)
                StoreManager.getParticipantStore();
                StoreManager.getRecoveryStore();
                StoreManager.getCommunicationStore();
                StoreManager.getTxOJStore();
			}
			else
			{
				String objectStoreDirName = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();

				System.out.println("Emptying " + objectStoreDirName);

				if (objectStoreDirName != null)
				{
					File objectStoreDir = new File(objectStoreDirName);

					removeContents(objectStoreDir);
				}
				else
				{
					System.err.println("Unable to find the ObjectStore root.");
					System.out.println("Failed");
				}
			}
			emptyPIDStore();
		}
		catch (Exception exception)
		{
			System.err.println("EmptyObjectStore.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			if (orbClass != null)
			{
				orbClass.stop();
			}
		}
		catch (Exception exception)
		{
			System.err.println("EmptyObjectStore.main: " + exception);
			exception.printStackTrace(System.err);
		}

		System.out.println("Passed");
	}

	public static void removeContents(File directory)
	{
		if ((directory != null) &&
				directory.isDirectory() &&
				(!directory.getName().equals("")) &&
				(!directory.getName().equals("/")) &&
				(!directory.getName().equals("\\")) &&
				(!directory.getName().equals(".")) &&
				(!directory.getName().equals("..")))
		{
			File[] contents = directory.listFiles();

			for (int index = 0; index < contents.length; index++)
			{
				if (contents[index].isDirectory())
				{
					removeContents(contents[index]);

					//System.err.println("Deleted: " + contents[index]);
					contents[index].delete();
				}
				else
				{
					System.err.println("Deleted: " + contents[index]);
					contents[index].delete();
				}
			}
		}
	}

	public static void emptyPIDStore()
	{
		// Do nothing
	}
}