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
package com.hp.mwtests.commonlogging.i18n;

import com.arjuna.common.util.logging.Logi18n;
import com.arjuna.common.util.logging.LogFactory;
//import com.arjuna.common.util.logging.CommonLevel;

import java.util.*;

public class logi18n
{

   //static LoggerFactory log_fac = LogManager.getLogFactory();
   static String language = System.getProperty("language", "en");
   static String country = System.getProperty("country", "US");

   private static Locale currentLocale = new Locale(language, country);
   //private static ResourceBundle log_mesg = ResourceBundle.getBundle("logging_msg", currentLocale);

   //static commonLogger mylog = (commonLogger)log_fac.getLogger(logi18n.class.getName(),"logging_msg_"+language+"_"+country);
   //static Logi18n log = LogFactory.getLogi18n("logi18n", "com.hp.mwtests.commonlogging.i18n.logging_msg_fr_FR");
   static Logi18n log = LogFactory.getLogi18n("logi18n", "com.hp.mwtests.commonlogging.i18n.logging_msg");

   public static void main(String[] args)
   {

      Object[] myParams = new Object[2];

      String lastname = "Foo";
      String firstname = "Bar";
      myParams[0] = firstname;
      myParams[1] = lastname;

      log.info("IDENTIFICATION", myParams);
      //log.log(CommonLevel.FATAL, "FATAL_Message");
      log.fatal("FATAL_Message");

      //log.log(CommonLevel.INFO, "INFO_Message");
      log.info("INFO_Message");
   }
}
    
    
    
