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
package com.hp.mwtests.commonlogging.testlevels;

import com.arjuna.common.util.logging.Logi18n;
import com.arjuna.common.util.logging.LogFactory;


public class TestLevels
{
   /**
    * for logging purposes.
    */
   public static final String CLASS = TestLevels.class.getName();

   /**
    * CLF logger for this class
    *
    * the resource bundle to use is the class name if no additional parameters are given.
    */
   private static Logi18n log = LogFactory.getLogi18n(CLASS);

   /**
    *
    * @message testMessage This is the {0} message, logged at level {0}.
    *
    * @param args
    */
   public static void main(String[] args)
   {
      log.debug("testMessage", new Object[] {"1st", "debug"});
      log.info("testMessage", new Object[] {"1st", "info"});
      log.warn("testMessage", new Object[] {"1st", "warn"});
      log.error("testMessage", new Object[] {"1st", "error"});
      log.fatal("testMessage", new Object[] {"1st", "fatal"});
   }
}

