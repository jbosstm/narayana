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

package org.jboss.jbossts.qa.Utils;

import com.arjuna.ats.arjuna.common.Uid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ObjectUidStore
{
	public static void storeUid(String objectName, Uid objectUid)
			throws Exception
	{
		Properties objectUids = new Properties();

		try
		{
			FileInputStream objectUidsFileInputStream = new FileInputStream("ObjectUids");
			objectUids.load(objectUidsFileInputStream);
			objectUidsFileInputStream.close();
		}
		catch (Exception exception)
		{
		}

		objectUids.put(objectName, objectUid.toString());

		FileOutputStream objectUidsFileOutputStream = new FileOutputStream("ObjectUids");
		objectUids.store(objectUidsFileOutputStream, "Object Uids");
		objectUidsFileOutputStream.close();
	}

	public static void removeUid(String objectName)
			throws Exception
	{
		Properties objectUids = new Properties();

		FileInputStream objectUidsFileInputStream = new FileInputStream("ObjectUids");
		objectUids.load(objectUidsFileInputStream);
		objectUidsFileInputStream.close();

		objectUids.remove(objectName);

		FileOutputStream objectUidsFileOutputStream = new FileOutputStream("ObjectUids");
		objectUids.store(objectUidsFileOutputStream, "Object Uids");
		objectUidsFileOutputStream.close();
	}

	public static Uid loadUid(String objectName)
			throws Exception
	{
		Uid objectUid = null;

		Properties objectUids = new Properties();

		FileInputStream objectUidsFileInputStream = new FileInputStream("ObjectUids");
		objectUids.load(objectUidsFileInputStream);
		objectUidsFileInputStream.close();

		objectUid = new Uid((String) objectUids.get(objectName));

		return objectUid;
	}

	public static void remove()
	{
		try
		{
			File file = new File("ObjectUids");

			file.delete();
		}
		catch (Exception exception)
		{
			System.err.println("Failed to remove \"ObjectUids\": " + exception);
		}
	}
}
