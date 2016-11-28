/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
//
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004,
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: EmptyObjectStore.java,v 1.5 2004/10/26 11:13:18 jcoleman Exp $
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
                BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setDropTable(true);

                storeEnvBean.setDropTable(true);

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
