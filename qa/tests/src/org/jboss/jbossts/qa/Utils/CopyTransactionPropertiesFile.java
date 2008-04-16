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
// Copyright (C) 2001,
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//

package org.jboss.jbossts.qa.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class CopyTransactionPropertiesFile
{
	public final static String BACKUP = "backup",
			RESTORE = "restore",
			BACKUP_SUFFIX = "_qabackup";

	/**
	 * Copy a file from source to destination
	 * returns true if the copy was successfull
	 * returns false if the copy failed
	 */
	public static boolean copyFile(String source, String destination)
	{
		System.err.println("Copying file " + source + " to " + destination);

		boolean result = false;

		try
		{
			File src = new File(source),
					dest = new File(destination);

			if (src.exists())
			{
				FileInputStream in = new FileInputStream(src);
				FileOutputStream out = new FileOutputStream(dest);
				int bytesRead = 0;
				byte[] buffer = new byte[65535];

				while ((bytesRead = in.read(buffer)) != -1)
				{
					out.write(buffer, 0, bytesRead);
				}
				in.close();
				out.close();
			}
		}
		catch (java.io.IOException e)
		{
			System.err.println("ERROR - " + e.toString());
		}

		return (result);
	}

	public static void main(String args[])
	{
		if (args.length != 2)
		{
			System.out.println("USAGE: CopyTransactionPropertiesFile [PROPERTY_FILENAME] BACKUP/RESTORE");
		}
		else
		{
			/*
			 * Retrieve parameters from the command line
			 */
			String propertyFilename = args[0];
			String action = args[1];

			if (action.equalsIgnoreCase(BACKUP))
			{
				copyFile(propertyFilename, propertyFilename + BACKUP_SUFFIX);
			}
			else
			{
				if (action.equalsIgnoreCase(RESTORE))
				{
					copyFile(propertyFilename + BACKUP_SUFFIX, propertyFilename);
				}
			}
		}
	}
}

