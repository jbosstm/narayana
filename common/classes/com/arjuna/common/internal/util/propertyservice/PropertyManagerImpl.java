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
package com.arjuna.common.internal.util.propertyservice;

import com.arjuna.common.util.propertyservice.propertycontainer.PropertyManagerPluginInterface;
import com.arjuna.common.util.propertyservice.Environment;
import com.arjuna.common.util.propertyservice.plugins.PropertyManagerIOPlugin;
import com.arjuna.common.util.propertyservice.plugins.PropertyManagementPlugin;
import com.arjuna.common.util.exceptions.LoadPropertiesException;
import com.arjuna.common.util.exceptions.SavePropertiesException;
import com.arjuna.common.util.exceptions.ManagementPluginException;

import java.util.Properties;
import java.util.Enumeration;
import java.io.IOException;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropertyManagerImpl.java 2342 2006-03-30 13:06:17Z  $
 */

/**
 * Implementation of the Property Manager.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 */
public class PropertyManagerImpl implements PropertyManagerPluginInterface
{
    /** Specifies whether the property manager is in verbose mode **/
	protected static boolean _verbose = false;
    /** Properties stored within this property manager **/
    protected Properties _properties;
    /** The name of this property manager **/
    protected String _name;
    /** The uri associated with this property manager **/
    protected String _associatedUri;
    private String _associatedPluginClassname;

    public PropertyManagerImpl(String name) {
        _associatedUri = null;
        _name = name;
        _properties = new Properties();
    }

    /**
     * Retrieve the name of this property manager
     * @return the property manager name
     */
	public String getName()
	{
		return _name;
	}

    /**
     * Retrieves the URI associated with this property manager
     * @return the URI
     */
    public String getUri()
    {
        return _associatedUri;
    }

    /**
     * Set the URI associated with this property manager
     * @param uri the URI
     */
    public void setUri(String uri)
    {
        _associatedUri = uri;
    }

    public String getIOPluginClassname()
	{
		return _associatedPluginClassname;
	}

    public void setIOPluginClassname(String classname)
	{
		_associatedPluginClassname = classname;
	}

    /**
	 * Get the value of the property with the name <code>name</code>
	 *
	 * @param name The name of the property to retrieve the value of.
	 * @return The value of the property.
	 */
	public String getProperty(String name)
	{
		/** Get the system property **/
		String value = System.getProperty(name);

		/** If the system property is null look in the local properties **/
		value = value != null ? value : _properties.getProperty(name);

		return value;
	}

    /**
	 * Get the value of the property with the name <code>name</code>.
	 * If the property cannot be found return the value <code>defaultValue</code>.
	 *
	 * @param name The name of the property to retrieve the value of.
	 * @param defaultValue The default value to return if the property cannot be found.
	 * @return The value of the property (or defaultValue).
	 */
	public String getProperty(String name, String defaultValue)
	{
		String value = getProperty(name);

		return value == null ? defaultValue : value;
	}

    /**
	 * Set the value of the property <code>name</code> to <code>value</code>.
	 *
	 * @param name The name of the property to set.
	 * @param value The value of the property being set.
	 * @return The previous value of the property.
	 */
	public String setProperty(String name, String value)
	{
		return setProperty(name, value, true);
	}

    /**
	 * Set the value of the property <code>name</code> to <code>value</code>.
	 *
	 * @param name The name of the property to set.
	 * @param value The value of the property being set.
	 * @param setSystemProperty True - set the system property if it has a value
	 * @return the previous value of the property
	 */
	public String setProperty(String name, String value, boolean setSystemProperty)
	{
		/** If the property exists in the system properties ensure that is set also **/
		if ( setSystemProperty && System.getProperty(name) != null )
		{
			System.setProperty(name, value);
		}

		String oldValue = _properties.getProperty(name);

		_properties.setProperty(name, value);

		return oldValue;
	}

    /**
	 * Removes the property from the property manager.
	 * @param name The name of the property to remove.
	 * @return previous value of the property
	 */
	public String removeProperty(String name)
	{
		/** If the property exists in the system properties ensure that is removed also **/
		if ( System.getProperty(name) != null )
		{
			System.getProperties().remove(name);
		}

        String oldValue = _properties.getProperty(name);

        if ( oldValue != null )
        {
            _properties.remove(name);
        }

		return oldValue;
	}

    /**
	 * Get the properties stored in this property manager only.
	 * @return the Properties
	 */
	public Properties getLocalProperties()
	{
		return _properties;
	}

    /**
	 * Get all the properties stored in this property manager and it's parents.
     * It also includes the system properties.
	 *
	 * @return the Properties
	 */
	public Properties getProperties()
	{
		Properties returnProps = (Properties)_properties.clone();

        returnProps.putAll(System.getProperties());

        return returnProps;
    }

    /**
	 * Returns an enumeration of the property names
	 * @return the enumeration
	 */
	public Enumeration propertyNames()
	{
		return getProperties().keys();
	}

    /**
	 * Loads properties from a given URI using the given property manager plugin.
	 * This plugin can be overridden by setting the system property 'propertyservice.plugin'.
	 *
	 * @param pluginClassname The classname of the plugin in to use.  This plugin is loaded
	 * using the current thread context classloader.
	 * @param uri The URI to load.
	 */
	public synchronized void load(String pluginClassname, String uri) throws IOException, ClassNotFoundException, LoadPropertiesException
	{
		try
		{
            String existingUri = getUri();
            if(existingUri != null && !existingUri.equals(uri)) {
                throw new LoadPropertiesException("Not allowed to reload from a different uri! [existing: "+existingUri+", requested: "+uri+"]");
            }

			/** Check to see if the system property has been set **/
			String classname = System.getProperty(Environment.OVERRIDING_PLUGIN_CLASSNAME);
			classname = classname == null ? pluginClassname : classname;

			PropertyManagerIOPlugin plugin = (PropertyManagerIOPlugin)Thread.currentThread().getContextClassLoader().loadClass(classname).newInstance();

			plugin.load(uri, this, _verbose);

            this.setUri(uri);
		}
		catch (IOException e)
		{
			throw e;
		}
        catch (Exception e)
		{
			throw new LoadPropertiesException("Failed to instantiate plugin: "+e, e);
		}
	}

    /**
	 * Saves the properties stored in this property manager using the given
	 * property manager plugin.  This plugin can be overridden by setting the
	 * system property 'propertyservice.plugin'
	 *
	 * @param pluginClassname The classname of the plugin to use.  This plugin is loaded
	 * using the current thread context classloader.  If null is provided the plugin used to
	 * load the properties is also used to save them.
	 * @param uri The URI to save to.  If null is provided the uri used to load the properties
	 * is also used to save them.
	 * @throws java.io.IOException
	 * @throws ClassNotFoundException
	 * @throws com.arjuna.common.util.exceptions.SavePropertiesException
	 */
	public synchronized void save(String pluginClassname, String uri) throws IOException, ClassNotFoundException, SavePropertiesException
	{
		try
		{
			if ( pluginClassname == null )
			{
				pluginClassname = _associatedPluginClassname;
			}

			/** Check to see if the system property has been set **/
			String classname = System.getProperty(Environment.OVERRIDING_PLUGIN_CLASSNAME);
			classname = classname == null ? pluginClassname : classname;

			PropertyManagerIOPlugin plugin = (PropertyManagerIOPlugin)Thread.currentThread().getContextClassLoader().loadClass(pluginClassname).newInstance();

			plugin.save(uri, this);
		}
		catch (IOException e)
		{
			throw e;
		}
        catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new SavePropertiesException("Failed to instantiate plugin: "+e, e);
		}
	}

    public boolean verbose()
	{
		return _verbose;
	}

    /**
	 * This adds a management plugin to this property manager.
     *
     * @param plugin The proeprty management plugin to plugin.
     * @throws java.io.IOException
     * @throws com.arjuna.common.util.exceptions.ManagementPluginException
     */
    public void addManagementPlugin(PropertyManagementPlugin plugin) throws IOException, ManagementPluginException
    {
        try
        {
            plugin.initialise(this);
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ManagementPluginException(e.toString(), e);
        }
    }
}
