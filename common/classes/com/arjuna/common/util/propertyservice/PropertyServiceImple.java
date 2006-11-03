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
* Copyright (C) 2001,
*
* Arjuna Solutions Limited,
* Newcastle upon Tyne,
* Tyne and Wear,
* UK.
*
* $Id: PropertyServiceImple.java 2342 2006-03-30 13:06:17Z  $
*/

package com.arjuna.common.util.propertyservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.SecurityException;

/**
 * The PropertyServiceImple class provides a uniform interface to
 * accessing and manipulating Java properties.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: PropertyServiceImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.2.
 */

public interface PropertyServiceImple
{

   /**
    * Add the specified property file to the list of registered
    * property files.
    */

   public void addPropertiesFile (String fileName);

   /**
    * Remove the specified property file from the list of registered
    * property files. If the file was not added, then the
    * FileNotFoundException will be thrown.
    */

   public void removePropertiesFile (String fileName) throws FileNotFoundException;

   /**
    * Read in the properties file, if present.
    *
    * @param force if this is <code>true</code> the property files will
    * be resourced, i.e., properties will be re-loaded.
    */

   public void loadProperties () throws SecurityException, IOException;

   /**
    * Saves the altered properties back to the properties files
    * from which they came.
    *
    * @throws SecurityException
    * @throws IOException
    */
   public void saveProperties() throws SecurityException, IOException;

   /**
    * Set the property name to have the specified value.
    * If flush is true then we put the property value into the System
    * so that a call to System.getProperty will succeed. Otherwise we
    * cache it as a "volatile" value.
    * If <code>warn</code> is <code>true</code> then issue a warning
    * message if an existing property value is changed.
    */

   public void setProperty (String name, String property, boolean flush, boolean warn);

   /**
    * Return the value associated with the property name. If the name
    * does not exist then the specified default value will be returned.
    */

   public String getProperty (String name, String defaultValue);

   /**
    * Remove the property.
    */

   public String removeProperty (String name, boolean flush);

   /**
    * Locate property file
    */
   public String locateFile (String propertyName) throws FileNotFoundException;

}
