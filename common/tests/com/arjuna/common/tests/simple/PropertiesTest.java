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

import java.util.Properties;
import java.util.Enumeration;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropertiesTest.java 2342 2006-03-30 13:06:17Z  $
 */

public class PropertiesTest
{
    @Test
	public void propertiesTest()
	{
        System.setProperty("property.file.name", "test-product.xml");
        PropertyManager txojPropertyManager = PropertyManagerFactory.getPropertyManagerForModule("txoj", "property.file.name");

        int count = 0;
		Properties p = txojPropertyManager.getProperties();

		System.out.println("TXOJ Properties size = "+p.size());

		for (Enumeration e = p.keys();e.hasMoreElements();)
		{
			String propertyName = (String)e.nextElement();
			String propertyValue = p.getProperty(propertyName);

            if(!propertyName.startsWith("com.arjuna.")) {
                continue;
            }

			if ( ( propertyName.equals("com.arjuna.ats.arjuna.Test") ) ||
			     ( propertyName.equals("com.arjuna.ats.txoj.Test") ) )
			{
				System.out.println("Found property '"+propertyName+"', value: "+propertyValue);
				count++;
			}
			else
			{
                fail("Found unexpected property '"+propertyName+"' failed");
			}
		}

        assertEquals(2, count);
	}
}
