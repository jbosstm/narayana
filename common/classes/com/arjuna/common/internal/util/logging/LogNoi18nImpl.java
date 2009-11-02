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
import com.arjuna.common.util.logging.DebugLevel;
import com.arjuna.common.util.logging.VisibilityLevel;
import com.arjuna.common.util.logging.FacilityCode;

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
    * Level for finer-debug logging.
    *
    * @see com.arjuna.common.util.logging.DebugLevel for possible values.
    */
   private final long m_debugLevel;

   /**
    * log level for visibility-based logging
    *
    * @see com.arjuna.common.util.logging.VisibilityLevel for possible values.
    */
   private final long m_visLevel;

   /**
    * log level for facility code
    *
    * @see com.arjuna.common.util.logging.FacilityCode for possible values.
    */
   private final long m_facLevel;


   /**
    * constructor
    *
    * @param logInterface the underlying logger to wrap
    * @param dl The finer debugging value.
    *           See {@link com.arjuna.common.util.logging.DebugLevel DebugLevel} for possible values.
    * @param vl The visibility level value.
    *           See {@link com.arjuna.common.util.logging.VisibilityLevel VisibilityLevel} for possible values.
    * @param fl The facility code level value.
    *           See {@link com.arjuna.common.util.logging.FacilityCode FacilityCode} for possible values.
    */
   public LogNoi18nImpl(LogInterface logInterface, long dl, long vl, long fl)
   {
      m_logInterface = logInterface;
      m_debugLevel = dl;
      m_visLevel = vl;
      m_facLevel = fl;
   }


   /**
    * Determine if this logger is enabled for DEBUG messages.
    *
    * This method returns true when the following is set:
    * <ul>
    * <li>finer debug level = <code>DebugLevel.FULL_DEBUGGING</code>.</li>
    * <li>visibility level = <code>VisibilityLevel.VIS_ALL</code>.</li>
    * <li>facility code = <code>FacilityCode.FAC_ALL</code>.</li>
    * </ul>
    * and the debug level is enabled in the underlying logger.
    *
    * @return  True if the logger is enabled for DEBUG, false otherwise
    */
   public boolean isDebugEnabled()
   {
       return debugAllowed(DebugLevel.FULL_DEBUGGING, VisibilityLevel.VIS_ALL, FacilityCode.FAC_ALL);
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


   /**************************** Debug Granularity Extension ***************************/

   /**
    * Is it allowed to print finer debugging statements with a given debug level,
    * visibility level and facility code level?
    *
    * @return true if the Logger allows logging for the finer debugging value <code>dLevel</code>,
    *    visibility level <code>vLevel</code> and facility code level <code>fLevel</code>.
    *    i.e., dLevel is equal or greater than the finer debug level assigned to the Logger
    *    and vLevel is equal or greater than the visiblity level
    *    and fLevel is equal or greater then the facility code level.
    * @param dLevel The debug finer level to check for.
    * @param vLevel The debug visibilty level to check for.
    * @param fLevel The facility code level to check for.
    */
   private boolean debugAllowed(long dLevel, long vLevel, long fLevel)
   {
       // check the underlying logger threshold as well as (but after, due to cost) our own filters
       return (((dLevel & m_debugLevel) != 0) && ((vLevel & m_visLevel) != 0) &&
              ((fLevel & m_facLevel) != 0) && m_logInterface.isDebugEnabled());
   }

   /**********************************************************************************************************
    * Finer-Granularity Debug Methods.
    **********************************************************************************************************/



   /**
    * Log a message with the DEBUG Level and with finer granularity. The debug message
    * is sent to the output only if the specified debug level, visibility level, and facility code
    * match those allowed by the logger.
    * <p>
    * <b>Note:</b> this method does not use i18n. ie, message is directly used for log output.
    *
    * @param dl The debug finer level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and dl is either equals or greater the debug level assigned to
    * the logger Object
    * @param vl The visibility level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and vl is either equals or greater the visibility level assigned to
    * the logger Object
    * @param fl The facility code level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and fl is either equals or greater the facility code level assigned to
    * the logger Object
    * @param message The message to log.
    */
   public void debug(long dl, long vl, long fl, String message)
   {
      if (debugAllowed(dl, vl, fl))
      {
         debug(message);
      }
   }

   /**
    * Log a message with DEBUG Level
    *
    * @param message the message to log
    */
   private void debug(String message)
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
      m_logInterface.info(message);
   }

   /**
    * Log a message with WARN Level
    *
    * @param message the message to log
    */
   public void warn(Throwable message)
   {
      m_logInterface.warn(message);
   }

   /**
    * Log a message with ERROR Level
    *
    * @param message the message to log
    */
   public void error(Throwable message)
   {
      m_logInterface.error(message);
   }

   /**
    * Log a message with FATAL Level
    *
    * @param message the message to log
    */
   public void fatal(Throwable message)
   {
      m_logInterface.fatal(message);
   }
}
