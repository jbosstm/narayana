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

import com.arjuna.common.util.propertyservice.PropertyManager;
import org.jboss.jbossts.qa.ArjunaCore.Utils.ChangeClasspath;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

/**
 * Simple test to see if Properties are being loaded correctly
 */
public class PropertyManagerTest
{
	/**
	 * Global varable for test result
	 */
	private static boolean mCorrect = true;
	private static int mNumberOfFiles = 0;
	private static String mOriginalarg = "";

	/**
	 * Simple test we only need to use the main method.
	 */
	public static void main(String[] args)
	{
		//since property manager does not check absolute path we need to add
		//location of propertyfiles to the classpath.
		ChangeClasspath.addToEnd(PropertyFileLoader.getFileLocation()); //we had to use '/etc' at the end of the path

		try
		{
			mOriginalarg = (String) args[0];
			mNumberOfFiles = Integer.parseInt(mOriginalarg);
		}
		catch (NumberFormatException nfe)
		{
			qautil.debug("Error in argument 1: ", nfe);
			mCorrect = false;
		}
		catch (Exception e)
		{
			qautil.debug("No args provided: ", e);
			mCorrect = false;
		}

		if (args.length < 2 || args[1].equals("force"))
		{
			forceReload();
		}
		else if (args[1].equals("reload"))
		{
			reload();
		}
		else
		{
			removeProperty();
		}
	}

	public static void forceReload()
	{
		qautil.qadebug("Running force reload test");
		loadProperty(true);

		//get the property from both methods.
		String systemProperty = System.getProperty("TESTPROPERTY");
		String propertyManagerProperty = pm.getProperty("TESTPROPERTY");

		//check final values
		if (!mOriginalarg.equals(systemProperty))
		{
			qautil.debug("Error checking system property: " + systemProperty);
			mCorrect = false;
		}

		if (!mOriginalarg.equals(propertyManagerProperty))
		{
			qautil.debug("Error checking property manager property: " + propertyManagerProperty);
			mCorrect = false;
		}
		finishTest(mCorrect);
	}

	public static void reload()
	{
		qautil.qadebug("Running reload test");
		loadProperty(false);

		//get the property from both methods.
		String systemProperty = System.getProperty("TESTPROPERTY");
		String propertyManagerProperty = pm.getProperty("TESTPROPERTY");

		//check final values
		if (!Integer.toString(1).equals(systemProperty))
		{
			qautil.debug("Error checking system property: " + systemProperty);
			qautil.debug("this is a known miss interpretation of the docs that has already been raised: see issue 565");
			mCorrect = false;
		}

		if (!Integer.toString(1).equals(propertyManagerProperty))
		{
			qautil.debug("Error checking property manager property: " + propertyManagerProperty);
			mCorrect = false;
		}
		finishTest(mCorrect);
	}

	/**
	 * Test if remove property removes all instances of property.
	 */
	public static void removeProperty()
	{
		qautil.qadebug("Running removeproperty test with flag set to true");
		loadProperty(true);

		//check we have the value or not
		String systemProperty = System.getProperty("TESTPROPERTY");
		qautil.qadebug("first check = " + systemProperty);
		String propertyManagerProperty = pm.getProperty("TESTPROPERTY");
		qautil.qadebug("first check = " + propertyManagerProperty);
		//if we have loaded more than 1 property file will this remove them all ?

		//remove property (for some reason this returns a string)
		String test = pm.removeProperty("TESTPROPERTY");
		qautil.qadebug("removeProperty method returned: " + test);

		//get the property from both methods.
		systemProperty = System.getProperty("TESTPROPERTY");
		propertyManagerProperty = pm.getProperty("TESTPROPERTY");

		//check final values
		if (systemProperty != null)
		{
			qautil.debug("Error checking system property: " + systemProperty);
			qautil.debug("see jitterbug issue 566");
			mCorrect = false;
		}

		if (propertyManagerProperty != null)
		{
			qautil.debug("Error checking property manager property: " + propertyManagerProperty);
			qautil.debug("see jitterbug issue 566");
			mCorrect = false;
		}
		finishTest(mCorrect);
	}

	public static void loadProperty(boolean flag)
	{
		/**
		 * Load property files with true flag to force reload.
		 */
		try
		{
			for (int i = 1; i < mNumberOfFiles + 1; i++)
			{
				String filename = "t" + i + ".props";
				qautil.qadebug("adding: " + filename);
				pm.load(com.arjuna.common.internal.util.propertyservice.plugins.io.XMLFilePlugin.class.getName(), filename);
			}
		}
		catch (Exception e)
		{
			qautil.debug("exception in test: ", e);
			mCorrect = false;
		}
	}

	/**
	 * Simple method for printing result.
	 */
	public static void finishTest(boolean result)
	{
		if (result)
		{
			System.out.println("Passed");
		}
		else
		{
			System.out.println("Failed");
		}
	}

	private static PropertyManager pm = com.arjuna.common.util.propertyservice.PropertyManagerFactory.getPropertyManager("qa");
}
