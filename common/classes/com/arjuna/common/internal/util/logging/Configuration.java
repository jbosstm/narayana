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
package com.arjuna.common.internal.util.logging;


/**
 * This class contains various run-time configuration options. Default
 * values are provided at compile-time, and may be operating system
 * specific.
 *
 * @author Malik SAHEB - malik.saheb@arjuna.com
 */

public class Configuration
{

   /**
    * @return the name of the module properties file to use.
    */

   public static synchronized final String propertiesFile()
   {
      return _propFile;
   }

   /**
    * Set the name of the properties file.
    */

   public static synchronized final void setPropertiesFile(String file)
   {
      _propFile = file;
   }

   /**
    * @return the location of the module properties file to use.
    */

   public static synchronized final String propertiesDir()
   {
      return _propDir;
   }

   /**
    * Set the location of the properties file.
    */

   public static synchronized final void setPropertiesDir(String file)
   {
      _propDir = file;
   }

   /**
    * @return the version of arjuna.
    */

   public static final String version()
   {
      return "1.0.0";
   }

   private static String _propFile = "CommonLogging-properties.xml";
   private static String _propDir = ".";
}

