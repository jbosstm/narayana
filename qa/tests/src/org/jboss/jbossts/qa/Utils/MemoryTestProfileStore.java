/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

///////////////////////////////////////////////////////////////////
//

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