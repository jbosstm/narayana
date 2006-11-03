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
 * JavaDoc
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
   private LogInterface m_logInterface = null;

   /**
    * default resource bundle for this logger.
    *
    * Note that it is also possible to use a resource bundle name as argument in log statements if so required.
    * (this is only used by the AMS client library and we might remove this feature because of performance)
    *
    * all extra resource bundles are kept in {@link #m_extraResBundles m_extraResBundles}.
    */
   protected String m_resourceBundle = "no resource bundle set";

   /**
    * extra resource bundles (if more than one is in use)
    *
    * Note that there is a performance issue when more than one resource bundles is in use.
    */
   protected String[] m_extraResBundles = null;

   /**
    * Level for finer-debug logging.
    *
    * @see com.arjuna.common.util.logging.DebugLevel for possible values.
    */
   private long m_debugLevel = DebugLevel.NO_DEBUGGING;

   /**
    * log level for visibility-based logging
    *
    * @see com.arjuna.common.util.logging.VisibilityLevel for possible values.
    */
   private long m_visLevel = VisibilityLevel.VIS_ALL;

   /**
    * log level for facility code
    *
    * @see com.arjuna.common.util.logging.FacilityCode for possible values.
    */
   private long m_facLevel = FacilityCode.FAC_ALL;


   /**
    * constructor
    *
    * @param logInterface
    */
   public LogNoi18nImpl(LogInterface logInterface)
   {
      m_logInterface = logInterface;
   }


   /**
    * Determine if this logger is enabled for DEBUG messages.
    * @return  True if the logger is enabled for DEBUG, false otherwise
    */
   public boolean isDebugEnabled()
   {
      return m_logInterface.isDebugEnabled();
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
    * Set the debug level, the visibility level, and the facility code.
    *
    * @param dl The finer debugging value.
    *           See {@link com.arjuna.common.util.logging.DebugLevel DebugLevel} for possible values.
    * @param vl The visibility level value.
    *           See {@link com.arjuna.common.util.logging.VisibilityLevel VisibilityLevel} for possible values.
    * @param fl The facility code level value.
    *           See {@link com.arjuna.common.util.logging.FacilityCode FacilityCode} for possible values.
    */
   public void setLevels(long dl, long vl, long fl)
   {
      m_debugLevel = dl;
      m_visLevel = vl;
      m_facLevel = fl;
   }

   /**
    * Return the finer debug level.
    *
    * @return The finer debugging level value associated with the logger
    * @see com.arjuna.common.util.logging.DebugLevel for possible return values.
    */
   public long getDebugLevel()
   {
      return m_debugLevel;
   }

   /**
    * Set the debug level as available in the {@link com.arjuna.common.util.logging.DebugLevel DebugLevel}.
    *
    * @param level The finer debugging value
    * @see com.arjuna.common.util.logging.DebugLevel for possible values of <code>level</code>.
    */
   public void setDebugLevel(long level)
   {
      m_debugLevel = level;
   }

   /**
    * Merge the debug level provided with that currently used by
    * the controller.
    *
    * @param level The finer debugging value
    * @see com.arjuna.common.util.logging.DebugLevel for possible values of <code>level</code>.
    */
   public void mergeDebugLevel(long level)
   {
      m_debugLevel |= level;
   }

   /**
    * Return the visibility level.
    *
    * @return The visibility level value associated with the Logger
    * @see com.arjuna.common.util.logging.VisibilityLevel for possible return values.
    */
   public long getVisibilityLevel()
   {
      return m_visLevel;
   }

   /**
    * Set the visibility level.
    *
    * @param level The visibility level value
    * @see com.arjuna.common.util.logging.VisibilityLevel for possible values of <code>level</code>.
    */
   public void setVisibilityLevel(long level)
   {
      m_visLevel = level;
   }

   /**
    * Merge the visibility level provided with that currently used by the Logger.
    *
    * @param level The visibility level value
    * @see com.arjuna.common.util.logging.VisibilityLevel for possible values of <code>level</code>.
    */
   public void mergeVisibilityLevel(long level)
   {
      m_visLevel |= level;
   }

   /**
    * Return the facility code.
    *
    * @return The facility code value associated with the Logger.
    * @see com.arjuna.common.util.logging.FacilityCode for possible return values.
    */
   public long getFacilityCode()
   {
      return m_facLevel;
   }

   /**
    * Set the facility code.
    *
    * @param level The facility code value
    * @see com.arjuna.common.util.logging.FacilityCode for possible values of <code>level</code>.
    */
   public void setFacilityCode(long level)
   {
      m_facLevel = level;
   }

   /**
    * Merge the debug level provided with that currently used by the Logger.
    *
    * @param level The visibility level value
    * @see com.arjuna.common.util.logging.FacilityCode for possible values of <code>level</code>.
    */
   public void mergeFacilityCode(long level)
   {
      m_facLevel |= level;
   }

   /**
    * Is it allowed to print finer debugging statements?
    *
    * This method returns true when the following is set:
    * <ul>
    * <li>finer debug level = <code>DebugLevel.FULL_DEBUGGING</code>.</li>
    * <li>visibility level = <code>VisibilityLevel.VIS_ALL</code>.</li>
    * <li>facility code = <code>FacilityCode.FAC_ALL</code>.</li>
    * </ul>
    *
    * @return true if the Logger allows full logging
    */
   public boolean debugAllowed()
   {
      return debugAllowed(DebugLevel.FULL_DEBUGGING, VisibilityLevel.VIS_ALL,
                          FacilityCode.FAC_ALL);
   }

   /**
    * Is it allowed to print finer debugging statements with a given debug level?
    *
    * This method assumes that:
    * <ul>
    * <li>visibility level = <code>VisibilityLevel.VIS_ALL</code>.</li>
    * <li>facility code = <code>FacilityCode.FAC_ALL</code>.</li>
    * </ul>
    *
    * @return true if the Logger allows logging for the finer debugging value <code>dLevel</code>.
    *    i.e., dLevel is either equals or greater than the finer debug level assigned to the Logger.
    * @param dLevel The debug finer level to check for.
    */
   public boolean debugAllowed(long dLevel)
   {
      return debugAllowed(dLevel, VisibilityLevel.VIS_ALL, FacilityCode.FAC_ALL);
   }

   /**
    * Is it allowed to print finer debugging statements with a given debug level
    * and visibility level?
    *
    * This method assumes that:
    * <ul>
    * <li>facility code = <code>FacilityCode.FAC_ALL</code>.</li>
    * </ul>
    *
    * @return true if the Logger allows logging for the finer debugging value <code>dLevel</code>
    *    and visibility level <code>vLevel</code>.
    *    i.e., dLevel is equal or greater than the finer debug level assigned to the Logger
    *    and vLevel is equal or greater than the visiblity level.
    * @param dLevel The debug finer level to check for.
    * @param vLevel The debug visibilty level to check for.
    */
   public boolean debugAllowed(long dLevel, long vLevel)
   {
      return debugAllowed(dLevel, vLevel, FacilityCode.FAC_ALL);
   }

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
   public boolean debugAllowed(long dLevel, long vLevel, long fLevel)
   {
      return (((dLevel & m_debugLevel) != 0) &&
            ((vLevel & m_visLevel) != 0) &&
            ((fLevel & m_facLevel) != 0));
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
   public void debug(long dl, long vl, long fl, Object message)
   {
      if (debugAllowed(dl, vl, fl))
      {
         debug(message);
      }
   }

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
   public void debug(Object message)
   {
      m_logInterface.debug(message);
   }

   /**
    * Log a message with DEBUG Level
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
   public void info(Object message)
   {
      m_logInterface.info(message);
   }

   /**
    * Log a message with INFO Level
    *
    * @param message the message to log
    */
   public void info(String message)
   {
      m_logInterface.info(message);
   }

   /**
    * Log a message with WARN Level
    *
    * @param message the message to log
    */
   public void warn(Object message)
   {
      m_logInterface.warn(message);
   }

   /**
    * Log a message with WARN Level
    *
    * @param message the message to log
    */
   public void warn(String message)
   {
      m_logInterface.warn(message);
   }

   /**
    * Log a message with ERROR Level
    *
    * @param message the message to log
    */
   public void error(Object message)
   {
      m_logInterface.error(message);
   }

   /**
    * Log a message with ERROR Level
    *
    * @param message the message to log
    */
   public void error(String message)
   {
      m_logInterface.error(message);
   }


   /**
    * Log a message with FATAL Level
    *
    * @param message the message to log
    */
   public void fatal(Object message)
   {
      m_logInterface.fatal(message);
   }

   /**
    * Log a message with FATAL Level
    *
    * @param message the message to log
    */
   public void fatal(String message)
   {
      m_logInterface.fatal(message);
   }

}
