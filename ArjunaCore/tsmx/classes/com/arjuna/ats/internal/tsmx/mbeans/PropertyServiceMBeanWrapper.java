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
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropertyServiceMBeanWrapper.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.tsmx.mbeans;

import com.arjuna.common.util.propertyservice.propertycontainer.PropertyManagerPluginInterface;
import com.arjuna.common.util.exceptions.SavePropertiesException;

import javax.management.*;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import java.util.Properties;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Hashtable;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This is a dynamic mbean class which exposes the properties stored in
 * a property manager as attributes.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: PropertyServiceMBeanWrapper.java 2342 2006-03-30 13:06:17Z  $
 */
public class PropertyServiceMBeanWrapper implements DynamicMBean
{
	private final static String PROPERTY_FILE_MBEAN_OBJECT_NAME_PREFIX = "com.arjuna.ats.properties:module=";
	private final static String MAPPINGS_FILENAME_SUFFIX = "-properties.mappings";
	private final static String COMMENT_PREFIX = "#";

	private PropertyManagerPluginInterface _pm = null;
	private ArrayList _attributeMapping = new ArrayList();
	private MBeanInfo _info = null;
	private Hashtable _comment = new Hashtable();
	private ArrayList _propertyMapping = new ArrayList();

	PropertyServiceMBeanWrapper(PropertyManagerPluginInterface pm) throws MappingsNotFoundException
	{
		_pm = pm;

		retrievePropertyToAttributeMappings(pm.getName());
	}

	/**
	 * Save the properties stored in the property manager.
	 * @throws java.io.IOException
	 * @throws SavePropertiesException
	 * @throws ClassNotFoundException
	 */
	public void save() throws java.io.IOException, SavePropertiesException, ClassNotFoundException
	{
		/**
		 * Save using the plugin that loaded the properties
		 * to the URI the properties were loaded from
		 */
		_pm.save(null, null);
	}


	private void retrievePropertyToAttributeMappings(String name) throws MappingsNotFoundException
	{
		InputStream mappingsIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(name + MAPPINGS_FILENAME_SUFFIX);

		if (mappingsIn != null)
		{
			try
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(mappingsIn));
				String mapping;
				String comment = null;

				/**
				 * Read in each one-to-one mapping:
				 *
				 *     property=attribute
				 *
				 * Store them in separate arrays
				 */
				while ((mapping = in.readLine()) != null)
				{
					if (mapping.startsWith(COMMENT_PREFIX))
					{
						if (mapping.length() > 1)
						{
							comment = mapping.substring(1);
						}
					}
					else
					{
						if (mapping.indexOf('=') == -1)
						{
							throw new MappingsNotFoundException("A valid property-to-attribute was not found (" + name + MAPPINGS_FILENAME_SUFFIX + ")");
						}

						_propertyMapping.add(mapping.substring(0, mapping.indexOf('=')));
						_attributeMapping.add(mapping.substring(mapping.indexOf('=') + 1));


                        if (comment != null)
                        {
                            _comment.put(mapping.substring(0, mapping.indexOf('=')), comment);
                        }

						comment = null;
					}
				}

				in.close();
			}
			catch (java.io.IOException e)
			{
				throw new MappingsNotFoundException("Failed to load property-to-attribute mappings (" + name + MAPPINGS_FILENAME_SUFFIX + ")");
			}
		}
		else
		{
			throw new MappingsNotFoundException("Failed to find property-to-attribute mappings (" + name + MAPPINGS_FILENAME_SUFFIX + ")");
		}
	}

	private String getPropertyForAttribute(String attributeName)
	{
		String property = null;
		int index = _attributeMapping.indexOf(attributeName);

		if (index != -1)
		{
			property = (String) _propertyMapping.get(index);
		}

		return property;
	}


	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
	{
		/** If the attribute name is a valid property **/
		if (_attributeMapping.contains(attribute.getName()))
		{
			String property = getPropertyForAttribute(attribute.getName());

			if (property == null)
			{
				throw new AttributeNotFoundException("Attribute-to-property mapping not found for: " + attribute.getName());
			}

			/** Set the property **/
			_pm.setProperty(property, (String) attribute.getValue());
		}
		else
			throw new AttributeNotFoundException("Attribute '" + attribute.getName() + "' does not exist within this property manager");
	}

	public AttributeList setAttributes(AttributeList attributeList)
	{
		AttributeList returnList = new AttributeList();

		for (int count = 0; count < attributeList.size(); count++)
		{
			Attribute attribute = (Attribute) attributeList.get(count);

			/** If the attribute name is a valid property **/
			if (_attributeMapping.contains(attribute.getName()))
			{
				try
				{
					/** Try to set the attribute **/
					setAttribute(attribute);

					/** Add this attribute to the returned list of set attributes **/
					returnList.add(attribute);
				}
				catch (Exception e)
				{
					// Ignore
				}
			}
		}

		return returnList;
	}

	public Object getAttribute(String s) throws AttributeNotFoundException, MBeanException, ReflectionException
	{
		String returnValue = null;

		/** Ensure the attribute maps onto an existing property **/
		if (_attributeMapping.contains(s))
		{
			returnValue = _pm.getProperty(getPropertyForAttribute(s), null);
		}

		if (returnValue == null)
		{
			throw new AttributeNotFoundException("The attribute '" + s + "' is not defined");
		}

		return returnValue;
	}

	public AttributeList getAttributes(String[] strings)
	{
		AttributeList returnList = new AttributeList(strings.length);

		for (int count = 0; count < strings.length; count++)
		{
			try
			{
				returnList.add(new Attribute(strings[count], getAttribute(strings[count])));
			}
			catch (Exception e)
			{
				// Ignore exceptions
			}
		}

		return returnList;
	}

	public Object invoke(String methodName, Object[] params, String[] signature) throws MBeanException, ReflectionException
	{
		if ((methodName.equals("save")) && (params.length == 0))
		{
			try
			{
				save();
			}
			catch (Exception e)
			{
				throw new MBeanException(e);
			}
		}
		return null;
	}

	public MBeanInfo getMBeanInfo()
	{
		if (_info == null)
		{
			_info = new MBeanInfo(this.getClass().getName(),
					"This is a mbean representing the properties stored in '" + _pm.getName() + "'",
					getAttributeInfo(),
					null,
					getOperationInfo(),
					null);
		}

		return _info;
	}

	private MBeanOperationInfo[] getOperationInfo()
	{
		MBeanOperationInfo saveOperation = null;

		try
		{
			saveOperation = new MBeanOperationInfo("Save the properties",
					this.getClass().getMethod("save", null));
		}
		catch (NoSuchMethodException e)
		{
			// This shouldn't happen
		}

		return new MBeanOperationInfo[]{saveOperation};
	}

	private MBeanAttributeInfo[] getAttributeInfo()
	{
		ArrayList attributes = new ArrayList();
		Properties p = new Properties();
		p.putAll(_pm.getLocalProperties());

		for (Enumeration e = p.keys(); e.hasMoreElements();)
		{
			String propertyName = (String) e.nextElement();
			// Only add the property if a mapping exists for it
			if (_propertyMapping.contains(propertyName))
			{
				int index = _propertyMapping.indexOf(propertyName);

				if (index != -1)
				{
					/** If a comment exists for this attribute then retrieve it **/
                    String comment = (String) _comment.get(propertyName);

					attributes.add(new MBeanAttributeInfo((String) _attributeMapping.get(index), // Attribute Name
							java.lang.String.class.getName(), // Attribute Type
							comment, // Attribute Description
							true, // Attribute is readable
							true, // Attribute is writable
							false)); // Attribute is not is
				}
			}
		}

		MBeanAttributeInfo[] attrInfo = new MBeanAttributeInfo[attributes.size()];
		attributes.toArray(attrInfo);

		return attrInfo;
	}

	/**
	 * Get the object name for this MBean
	 * @return
	 * @throws MalformedObjectNameException
	 */
	public ObjectName getObjectName() throws MalformedObjectNameException
	{
		return new ObjectName(PROPERTY_FILE_MBEAN_OBJECT_NAME_PREFIX + _pm.getName());
	}
}
