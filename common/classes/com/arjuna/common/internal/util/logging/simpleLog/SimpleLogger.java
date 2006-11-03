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
 * SimpleLogger.java
 *
 * Copyright (c) 2003 Arjuna Technologies Ltd.
 * Arjuna Technologies Ltd. Confidential
 *
 * Created on Jun 30, 2003, 10:10:07 AM by Thomas Rischbeck
 */
package com.arjuna.common.internal.util.logging.simpleLog;

import com.arjuna.common.internal.util.logging.LogInterface;

/**
 * JavaDoc
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * @since   AMS 3.0
 */
public class SimpleLogger implements LogInterface
{
   /**
    * Is DEBUG logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than DEBUG.
    *
    * @return  True if the logger is enabled for DEBUG, false otherwise
    */
   public boolean isDebugEnabled()
   {
      return false;
   }

   /**
    * Is INFO logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than INFO.
    *
    * @return  True if the logger is enabled for INFO, false otherwise
    */
   public boolean isInfoEnabled()
   {
      return false;
   }

   /**
    * Is WARN logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than WARN.
    *
    * @return  True if the logger is enabled for WARN, false otherwise
    */
   public boolean isWarnEnabled()
   {
      return false;
   }

   /**
    * Is ERROR logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than ERROR.
    *
    * @return  True if the logger is enabled for ERROR, false otherwise
    */
   public boolean isErrorEnabled()
   {
      return false;
   }

   /**
    * Is FATAL logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than FATAL.
    *
    * @return  True if the logger is enabled for FATAL, false otherwise
    */
   public boolean isFatalEnabled()
   {
      return false;
   }

   /**
    * Is TRACE logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than TRACE.
    */
   public boolean isTraceEnabled()
   {
      return false;
   }

   /**
    * <p> Log a message with trace log level. </p>
    *
    * @param message log this message
    */
   public void trace(Object message)
   {
   }

   /**
    * <p> Log an error with trace log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void trace(Object message, Throwable t)
   {
   }

   /**
    * <p> Log a message with debug log level. </p>
    *
    * @param message log this message
    */
   public void debug(Object message)
   {
   }

   /**
    * <p> Log an error with debug log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void debug(Object message, Throwable t)
   {
   }

   /**
    * <p> Log a message with info log level. </p>
    *
    * @param message log this message
    */
   public void info(Object message)
   {
   }

   /**
    * <p> Log an error with info log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void info(Object message, Throwable t)
   {
   }

   /**
    * <p> Log a message with warn log level. </p>
    *
    * @param message log this message
    */
   public void warn(Object message)
   {
   }

   /**
    * <p> Log an error with warn log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void warn(Object message, Throwable t)
   {
   }

   /**
    * <p> Log a message with error log level. </p>
    *
    * @param message log this message
    */
   public void error(Object message)
   {
   }

   /**
    * <p> Log an error with error log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void error(Object message, Throwable t)
   {
   }

   /**
    * <p> Log a message with fatal log level. </p>
    *
    * @param message log this message
    */
   public void fatal(Object message)
   {
   }

   /**
    * <p> Log an error with fatal log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void fatal(Object message, Throwable t)
   {
   }
}
