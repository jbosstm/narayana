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
