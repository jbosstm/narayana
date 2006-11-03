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
package com.arjuna.common.util.propertyservice;

import com.arjuna.common.util.exceptions.LoadPropertiesException;
import com.arjuna.common.util.exceptions.ManagementPluginException;
import com.arjuna.common.util.propertyservice.plugins.PropertyManagementPlugin;
import com.arjuna.common.util.exceptions.SavePropertiesException;

import java.util.Properties;
import java.util.Enumeration;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropertyManager.java 2342 2006-03-30 13:06:17Z  $
 */

public interface PropertyManager
{
	/**
	 * Get the value of the property with the name <code>name</code>
	 *
	 * @param name The name of the property to retrieve the value of.
	 * @return The value of the property.
	 */
    public String getProperty(String name);

	/**
	 * Get the value of the property with the name <code>name</code>.
	 * If the property cannot be found return the value <code>defaultValue</code>.
	 *
	 * @param name The name of the property to retrieve the value of.
	 * @param defaultValue The default value to return if the property cannot be found.
	 * @return The value of the property (or defaultValue).
	 */
	public String getProperty(String name, String defaultValue);

	/**
	 * Set the value of the property <code>name</code> to <code>value</code>.
	 *
	 * @param name The name of the property to set.
	 * @param value The value of the property being set.
	 * @param setSystemProperty True - set the system property if it has a value
	 * @return
	 */
	public String setProperty(String name, String value, boolean setSystemProperty);

	/**
	 * Set the value of the property <code>name</code> to <code>value</code>
	 * @param name The name of the property to set.
	 * @param value The value of the property to set.
	 * @return
	 */
	public String setProperty(String name, String value);

	/**
	 * Removes the property from the property manager.
	 * @param name The name of the property to remove.
	 * @return
	 */
	public String removeProperty(String name);

	/**
	 * Get all the properties stored in this property manager
	 *
	 * @return
	 */
	public Properties getProperties();

	/**
	 * Returns an enumeration of the property names
	 * @return
	 */
    public Enumeration propertyNames();

	/**
	 * Loads properties from a given URI using the given property manager plugin.
	 * This plugin can be overridden by setting the system property 'propertyservice.plugin'.
	 *
	 * @param pluginClassname The classname of the plugin to use.  This plugin is loaded
	 * using the current thread context classloader.
	 * @param uri The URI to load.
	 */
	public void load(String pluginClassname, String uri) throws java.io.IOException, ClassNotFoundException, LoadPropertiesException;

    /**
	 * Saves the properties stored in this property manager using the given
	 * property manager plugin.  This plugin can be overridden by setting the
	 * system property 'propertyservice.plugin'
	 *
	 * @param pluginClassname The classname of the plugin to use.  This plugin is loaded
	 * using the current thread context classloader.  If null is provided the plugin used
	 * to load the properties is also used to save them.
	 * @param uri The URI to save to.  If null is provided the uri used to load the properties
	 * is also used to save them.
	 * @throws java.io.IOException
	 * @throws ClassNotFoundException
	 * @throws SavePropertiesException
	 */
	public void save(String pluginClassname, String uri) throws java.io.IOException, ClassNotFoundException, SavePropertiesException;

	/**
	 * This adds a management plugin to this property manager.
	 *
	 * @param plugin The PropertyManagementPlugin to plug-in.
	 * @throws java.io.IOException
	 * @throws ManagementPluginException
	 */
	public void addManagementPlugin(PropertyManagementPlugin plugin) throws java.io.IOException, ManagementPluginException;

	/**
	 * Returns true if the property manager is in verbose mode
	 * @return
	 */
	public boolean verbose();
}
