/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
package com.arjuna.common.tests.simple;

import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.common.util.propertyservice.PropertyManagerFactory;
import com.arjuna.common.internal.util.propertyservice.plugins.io.XMLFilePlugin;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SetPropertyTest.java 2342 2006-03-30 13:06:17Z  $
 */

public class SetPropertyTest
{
	public static void main(String[] args)
	{
		PropertyManager arjunaPM = PropertyManagerFactory.getPropertyManager( "test-property-manager", "Arjuna" );
		PropertyManager txojPM = PropertyManagerFactory.getPropertyManager( "test-property-manager", "TXOJ" );

		try
		{
			arjunaPM.load(XMLFilePlugin.class.getName(), "test-product.xml");
		}
		catch (Exception e)
		{
			e.printStackTrace();  //To change body of catch statement use Options | File Templates.
		}

		boolean passed = true;
		int count = 0;

		String value = arjunaPM.getProperty("com.arjuna.ats.arjuna.Test");
		System.out.println("Value["+(++count)+"] :"+value);
		passed &= value.equals("Test");

		arjunaPM.setProperty("com.arjuna.ats.arjuna.Test", "OverriddenInCode");

		value = txojPM.getProperty("com.arjuna.ats.arjuna.Test");
		System.out.println("Value["+(++count)+"] :"+value);
		passed &= value.equals("Overridden");

		value = arjunaPM.getProperty("com.arjuna.ats.arjuna.Test");
		System.out.println("Value["+(++count)+"] :"+value);
		passed &= value.equals("OverriddenInCode");

		System.out.println( passed ? "Passed" : "Failed" );
	}
}
