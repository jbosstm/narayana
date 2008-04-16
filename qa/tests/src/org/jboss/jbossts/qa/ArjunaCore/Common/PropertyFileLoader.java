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
