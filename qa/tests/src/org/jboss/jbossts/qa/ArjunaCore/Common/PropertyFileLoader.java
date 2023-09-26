/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.Common;

import java.io.File;
import java.net.URL;

/**
 * Utility for loading testharnes property file.
 */
public class PropertyFileLoader
{
	public static String getFileLocation()
	{
		//we hard code this from the class location
		String key = "propertyfiles" + File.separator + "etc";
		String key1 = "propertyfiles/etc";
		URL test = sPropertyFileLoader.getClass().getResource(key);

		if (test == null)
		{
			test = sPropertyFileLoader.getClass().getResource(key1);
		}

		String filelocation = test.toExternalForm();
		// now remove the file: from the url
		filelocation = filelocation.substring(5, filelocation.length());
		return filelocation;
	}

	private static final PropertyFileLoader sPropertyFileLoader = new PropertyFileLoader();
}