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
package com.arjuna.common.util.propertyservice.plugins;

import com.arjuna.common.util.propertyservice.propertycontainer.PropertyManagerPluginInterface;
import com.arjuna.common.util.exceptions.LoadPropertiesException;
import com.arjuna.common.util.exceptions.SavePropertiesException;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropertyManagerIOPlugin.java 2342 2006-03-30 13:06:17Z  $
 */

public interface PropertyManagerIOPlugin
{
	/**
	 * This method loads the properties stored at the given <code>uri</code>.  The
	 * plugin uses the <code>PropertyManagerPluginInterface</code> to put the properties
	 * into the property manager.
	 *
	 * @param uri
	 * @param pcm
	 * @param verbose
	 * @throws java.io.IOException
	 */
	public void load(String uri, PropertyManagerPluginInterface pcm, boolean verbose) throws LoadPropertiesException, java.io.IOException;

	/**
	 * This method saves the properties to the given <code>uri</code>.  The plugin
	 * uses the <code>PropertyManagerPluginInterface</code> to read the properties
	 * from the property manager.
	 *
	 * @param uri
	 * @param pcm
	 * @throws SavePropertiesException
	 * @throws java.io.IOException
	 */
	public void save(String uri, PropertyManagerPluginInterface pcm) throws SavePropertiesException, java.io.IOException;
}
