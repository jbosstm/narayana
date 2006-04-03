/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
/*
 * Copyright (C) 2004,
 * 
 * Arjuna Technologies Ltd, Newcastle upon Tyne, Tyne and Wear, UK.
 * 
 * $Id: XAResourceErrorHandler.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.resources;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;

import com.arjuna.ats.jta.resources.XAResourceMap;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.common.Environment;

import java.util.HashMap;
import java.util.Enumeration;
import java.util.Properties;

public class XAResourceErrorHandler
{

	public static boolean notAProblem (XAResource res, XAException ex, boolean commit)
	{
		boolean isNotAProblem = false;
		XAResourceMap theMap = (XAResourceMap) _maps.get(res.getClass().getName());

		if (theMap != null)
			isNotAProblem = theMap.notAProblem(ex, commit);

		return isNotAProblem;
	}

	public static void addXAResourceMap (String type, XAResourceMap map)
	{
		_maps.put(type, map);
	}

	private static HashMap _maps = new HashMap();

	static
	{
		// explicitly add tibco

		Properties props = jtaPropertyManager.propertyManager.getProperties();

		if (props != null)
		{
			Enumeration names = props.propertyNames();

			while (names.hasMoreElements())
			{
				String propName = (String) names.nextElement();

				if (propName.startsWith(Environment.XA_ERROR_HANDLER))
				{
					/*
					 * Given the recovery string, create the class it refers to
					 * and store it.
					 */

					String theClass = jtaPropertyManager.propertyManager.getProperty(propName);

					try
					{
						Class c = Thread.currentThread().getContextClassLoader().loadClass(theClass);

						XAResourceMap map = (XAResourceMap) c.newInstance();

						XAResourceErrorHandler.addXAResourceMap(map.getXAResourceName(), map);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
	}
}
