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
package com.arjuna.ats.internal.tsmx.mbeans;

import com.arjuna.common.util.propertyservice.plugins.PropertyManagementPlugin;
import com.arjuna.common.util.propertyservice.propertycontainer.PropertyManagerPluginInterface;
import com.arjuna.common.util.exceptions.ManagementPluginException;

import com.arjuna.ats.tsmx.TransactionServiceMX;
import com.arjuna.ats.tsmx.logging.tsmxLogger;

import java.io.IOException;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropertyServiceJMXPlugin.java 2342 2006-03-30 13:06:17Z  $
 */

/**
 * This class is a Management plugin for the property manager which registers each and
 * every property manager in a property manager tree as mbeans.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: PropertyServiceJMXPlugin.java 2342 2006-03-30 13:06:17Z  $
 */
public class PropertyServiceJMXPlugin implements PropertyManagementPlugin
{
	public void initialise(PropertyManagerPluginInterface pm) throws ManagementPluginException, IOException
	{
		try
		{
			registerPropertyManagers(pm);
		}
		catch (Exception e)
		{
			throw new ManagementPluginException("Failed to register mbeans: "+e, e);
		}
	}

	/**
	 * Register the given property manager dynamic mbean wrapper.
	 * @param pm
	 * @throws Exception
	 */
	private void registerPropertyManagers(PropertyManagerPluginInterface pm) throws Exception
	{
		/** If this property manager has properties then register it as a JMX Bean **/
		if ( !pm.getLocalProperties().isEmpty() )
		{
            PropertyServiceMBeanWrapper mbean = null;

            try
            {
                /** Create the dynamic mbean for this property manager **/
                mbean = new PropertyServiceMBeanWrapper(pm);

                /** Register it with the current TSMX agent **/
                TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().registerMBean(mbean, mbean.getObjectName());
            }
            catch (MappingsNotFoundException e)
            {
                if ( pm.verbose() )
                {
                    System.err.println("Mappings not found: "+e.toString());
                }
            }
		}
	}
}
