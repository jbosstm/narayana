/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


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