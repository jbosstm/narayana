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
///////////////////////////////////////////////////////////////////
//
// Copyright (C) 2001, HP Bluestone Arjuna.
//
// File        : MemoryTestProfileStore.java
//
// Description : Class used to get default memory increase values
//               from the config file MemoryTestProfile.
//
// Author      : M Buckingham
//
// History     : 1.0  1st May 2001  M Buckingham  Creation.
//
///////////////////////////////////////////////////////////////////

package org.jboss.jbossts.qa.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class MemoryTestProfileStore
{
	private final static String BASE_DIRECTORY_PROPERTY = "memorytestprofilestore.dir";

	public static String getNoThresholdValue() throws Exception
	{
		loadProfile();
		return (String) _profile.get("NoThresholdValue");
	}

	public static String getDefaultClientIncreaseThreshold() throws Exception
	{
		loadProfile();
		return (String) _profile.get("DefaultClientIncreaseThreshold");
	}

	public static String getDefaultServerIncreaseThreshold() throws Exception
	{
		loadProfile();
		return (String) _profile.get("DefaultServerIncreaseThreshold");
	}

	// end of new methods

	private static void loadProfile()
			throws Exception
	{
		if (_profile == null)
		{
			String baseDir = System.getProperty(BASE_DIRECTORY_PROPERTY);

			if (baseDir == null)
			{
				throw new Exception(BASE_DIRECTORY_PROPERTY + " property not set - cannot find memory test profiles!");
			}

			_profile = new Properties();
			FileInputStream profileFileInputStream =
					new FileInputStream(baseDir + File.separator + "MemoryTestProfile");
			_profile.load(profileFileInputStream);
			profileFileInputStream.close();
		}
	}

	private static Properties _profile = null;
}
