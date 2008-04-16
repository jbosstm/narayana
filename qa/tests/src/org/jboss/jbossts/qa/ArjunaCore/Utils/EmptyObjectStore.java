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
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//

package org.jboss.jbossts.qa.ArjunaCore.Utils;

import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

public class EmptyObjectStore
{
	public static void main(String[] args)
	{
		try
		{
			if (arjPropertyManager.propertyManager.getProperty(Environment.OBJECTSTORE_TYPE) != null &&
					arjPropertyManager.propertyManager.getProperty(Environment.OBJECTSTORE_TYPE).startsWith("JDBCStore"))
			{
				JDBCAccess mJDBC = (JDBCAccess) Class.forName(System.getProperty("JDBC2_USER_DB_ACCESS")).newInstance();
				Connection mConnection = mJDBC.getConnection();
				Statement s = mConnection.createStatement();
				s.executeUpdate("DROP TABLE " + mJDBC.tableName());
			}
			else
			{
				String objectStoreDirName = arjPropertyManager.propertyManager.getProperty(Environment.OBJECTSTORE_DIR, com.arjuna.ats.arjuna.common.Configuration.objectStoreRoot());

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
