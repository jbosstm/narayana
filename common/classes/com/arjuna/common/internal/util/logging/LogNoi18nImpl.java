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
* LogNoi18nImpl.java
*
* Copyright (c) 2003 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* Created on Aug 8, 2003, 12:55:00 PM by Thomas Rischbeck
*/
package com.arjuna.common.internal.util.logging;

import com.arjuna.common.util.logging.LogNoi18n;

/**
 * Logger for non-i18n messages. These should generally be debug (trace) level or raw exceptions. All
 * textual messages at higher levels should go through the i18n logger instead.
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * @since   AMS 3.0
 */
public class LogNoi18nImpl implements LogNoi18n
{
   /**
    * Log interface (no i18n provided).
    */
   private final LogInterface m_logInterface;

   /**
    * constructor
    *
    * @param logInterface the underlying logger to wrap
    */
   public LogNoi18nImpl(LogInterface logInterface)
   {
      m_logInterface = logInterface;
   }


   /**
    * Determine if this logger is enabled for DEBUG messages.
    *
    * This method returns true when the underlying logger is configured with DEBUG level on.
    *
    * @return  True if the logger is enabled for DEBUG, false otherwise
    */
   public boolean isDebugEnabled()
   {
       return m_logInterface.isDebugEnabled(); // TODO: reintro an ON|OFF flag for perf ortimization?
   }

   /**
    * Determine if this logger is enabled for INFO messages.
    * @return  True if the logger is enabled for INFO, false otherwise
    */
   public boolean isInfoEnabled()
   {
      return m_logInterface.isInfoEnabled();
   }

   /**
    * Determine if this logger is enabled for WARN messages.
    * @return  True if the logger is enabled for WARN, false otherwise
    */
   public boolean isWarnEnabled()
   {
      return m_logInterface.isWarnEnabled();
   }

   /**
    * Determine if this logger is enabled for ERROR messages.
    * @return  True if the logger is enabled for ERROR, false otherwise
    */
   public boolean isErrorEnabled()
   {
      return m_logInterface.isErrorEnabled();
   }

   /**
    * Determine if this logger is enabled for FATAL messages.
    * @return  True if the logger is enabled for FATAL, false otherwise
    */
   public boolean isFatalEnabled()
   {
      return m_logInterface.isFatalEnabled();
   }


   /**
    * Log a message with DEBUG Level
    *
    * <b>Note:</b> this method does not use i18n. ie, message is directly used for log output.
    *
    * @param message the message to log
    */
   public void debug(String message)
   {
      m_logInterface.debug(message);
   }

   /**
    * Log a message with INFO Level
    *
    * @param message the message to log
    */
   public void info(Throwable message)
   {
      m_logInterface.info(message.toString(), message);
   }

   /**
    * Log a message with WARN Level
    *
    * @param message the message to log
    */
   public void warn(Throwable message)
   {
      m_logInterface.warn(message.toString(), message);
   }

   /**
    * Log a message with ERROR Level
    *
    * @param message the message to log
    */
   public void error(Throwable message)
   {
      m_logInterface.error(message.toString(), message);
   }

   /**
    * Log a message with FATAL Level
    *
    * @param message the message to log
    */
   public void fatal(Throwable message)
   {
      m_logInterface.fatal(message.toString(), message);
   }
}
