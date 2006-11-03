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
package com.arjuna.common.util.propertyservice.propertycontainer;

import com.arjuna.common.util.propertyservice.PropertyManager;

import java.util.Properties;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropertyManagerPluginInterface.java 2342 2006-03-30 13:06:17Z  $
 */

public interface PropertyManagerPluginInterface extends PropertyManager
{
	public String getName();

    /**
     * Get the top-level property manager for this property manager
     * @return
     */
    public PropertyManagerPluginInterface getTopLevelPropertyManager();

	/**
	 * This method creates a property manager with the name <code>moduleName</code>.
	 * @param modulename
	 * @return
	 */
	public PropertyManagerPluginInterface createPropertyManager(String modulename);

	/**
	 * This method adds the property manager as a child.
	 */
	public void addChild(PropertyManager pm);

	/**
	 * This method adds a parent property manager.
 	 * @param pm The property manager to add as a parent.
	 */
	public void addParent(PropertyManager pm);

	/**
	 * This method returns an array of parents to the property manager.
	 * @return
	 */
	public PropertyManagerPluginInterface[] getParents();

	/**
	 * This method returns an array of the children of the current property manager.
	 * @return
	 */
	public PropertyManagerPluginInterface[] getChildren();

	/**
	 * Get the child module.
	 *
	 * @param moduleName
	 * @return
	 */
	public PropertyManagerPluginInterface getChild(String moduleName);

	/**
	 * Get the properties stored in this property manager only.
	 * @return
	 */
	public Properties getLocalProperties();

    /**
     * Retrieves the URI associated with this property manager
     * @return
     */
    public String getUri();

    /**
     * Set the URI associated with this property manager
     * @param uri
     */
    public void setUri(String uri);

    /**
	 * Get the IO plugin classname associated with this property manager.
	 * @return
	 */
	public String getIOPluginClassname();

	/**
	 * Set the IO plugin classname associated with this property manager.
	 * @param classname
	 */
	public void setIOPluginClassname(String classname);
}
