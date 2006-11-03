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
 * Copyright (C) 2001
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryConfiguration.javatmpl 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.recovery;

import com.arjuna.ats.arjuna.common.Configuration;

/**
 * This class contains various run-time configuration options. Default
 * values are provided at compile-time, and may be operating system
 * specific.
 *
 */
public class RecoveryConfiguration
{

   /**
    * @return the name of the RecoveryManager properties file to use.
    * @since JTS 2.1.
    */
   public static synchronized final String recoveryManagerPropertiesFile()
   {
      return _rmPropertyFile;
   }

   /**
    * Set the name of the RecoveryManager properties file.
    * @since JTS 2.1.
    */

   public static synchronized final void setRecoveryManagerPropertiesFile (String file)
   {
      _rmPropertyFile = file;
   }

   private static String _rmPropertyFile = Configuration.getBuildTimeProperty("RECOVERY_PROPERTIES_FILE")  ;

   private RecoveryConfiguration()
   {
      // zero-ton class
   }
}



