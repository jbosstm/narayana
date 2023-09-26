/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.Utils;

import java.util.Properties;

public class ChangeClasspath
{
	public static void addToFront(String path)
	{
		String orig = System.getProperty("java.class.path");
		String seperator = System.getProperty("path.separator");
		String newclasspath = path + seperator + orig;

		//this will make the system properties the default propertie for
		//this new property object
		Properties props = new Properties(System.getProperties());
		props.put("java.class.path", newclasspath);
		System.setProperties(props);
	}

	public static void addToEnd(String path)
	{
		String orig = System.getProperty("java.class.path");
		String seperator = System.getProperty("path.separator");
		String newclasspath = orig + seperator + path;

		//this will make the system properties the default propertie for
		//this new property object
		Properties props = new Properties(System.getProperties());
		props.put("java.class.path", newclasspath);
		System.setProperties(props);
	}
}