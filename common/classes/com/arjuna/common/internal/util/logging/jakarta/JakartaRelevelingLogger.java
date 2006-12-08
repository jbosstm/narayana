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
 * (C) 2006,
 * @author JBoss Inc.
 */

package com.arjuna.common.internal.util.logging.jakarta;

import com.arjuna.common.internal.util.logging.LogInterface;

/**
 * The semantics of logging levels used in the JBossTS source code
 * differs from those used in JBossAS. In order to preserve existing
 * backwards compatible semantics for standalone use, we won't change
 * the log levels used in the source. Instead, we provide this plugin
 * that dynamically changes the specified log level to match the
 * JBoss semantics.
 *
 * The following rules apply:
 * - INFO level messages are converted into DEBUG level messages.
 *   (and isInfoEnabled is a synonym for isDebugEnabled)
 * - All other messages are left at their original level.
 *
 * see also:
 *
 * http://jira.jboss.com/jira/browse/JBTM-20
 * http://www.jboss.com/index.html?module=bb&op=viewtopic&t=93949
 * http://wiki.jboss.org/wiki/Wiki.jsp?page=UsageOfLoggingLevels
 * http://docs.jboss.org/process-guide/en/html/internationalization.html
 *
 * @author Jonathan Halliday <jonathan.halliday@redhat.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 */
public class JakartaRelevelingLogger implements LogInterface
{

   /**
    * the Jakarta-specific logger.
    */
   private org.apache.commons.logging.Log m_log = null;

   protected JakartaRelevelingLogger(org.apache.commons.logging.Log log)
   {
      m_log = log;
   }

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
      return m_log.isDebugEnabled();
   }

   /**
    * Is INFO (a.k.a. DEBUG) logging currently enabled?
    *
    * Call this method to prevent having to perform expensive operations
    * (for example, <code>String</code> concatination)
    * when the log level is more than INFO.
    *
    * @return  True if the logger is enabled for INFO, false otherwise
    */
   public boolean isInfoEnabled()
   {
      return m_log.isDebugEnabled(); // level changed
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
      return m_log.isWarnEnabled();
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
      return m_log.isErrorEnabled();
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
      return m_log.isFatalEnabled();
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
      return m_log.isTraceEnabled();
   }

   /**
    * <p> Log a message with trace log level. </p>
    *
    * @param message log this message
    */
   public void trace(Object message)
   {
      m_log.trace(message);
   }

   /**
    * <p> Log an error with trace log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void trace(Object message, Throwable t)
   {
      m_log.trace(message, t);
   }

   /**
    * <p> Log a message with debug log level. </p>
    *
    * @param message log this message
    */
   public void debug(Object message)
   {
      m_log.debug(message);
      //m_log.log(_fcqn, Priority.DEBUG, message, null);
   }

   /**
    * <p> Log an error with debug log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void debug(Object message, Throwable t)
   {
      m_log.debug(message, t);
   }

   /**
    * <p> Log a message with info (a.k.a. DEBUG) log level. </p>
    *
    * @param message log this message
    */
   public void info(Object message)
   {
      m_log.debug(message); // level changed
   }

   /**
    * <p> Log an error with info (a.k.a. DEBUG) log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void info(Object message, Throwable t)
   {
      m_log.debug(message, t); // level changed
   }

   /**
    * <p> Log a message with warn log level. </p>
    *
    * @param message log this message
    */
   public void warn(Object message)
   {
      m_log.warn(message);
   }

   /**
    * <p> Log an error with warn log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void warn(Object message, Throwable t)
   {
      m_log.warn(message, t);
   }

   /**
    * <p> Log a message with error log level. </p>
    *
    * @param message log this message
    */
   public void error(Object message)
   {
      m_log.error(message);
   }

   /**
    * <p> Log an error with error log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void error(Object message, Throwable t)
   {
      m_log.error(message, t);
   }

   /**
    * <p> Log a message with fatal log level. </p>
    *
    * @param message log this message
    */
   public void fatal(Object message)
   {
      m_log.fatal(message);
   }

   /**
    * <p> Log an error with fatal log level. </p>
    *
    * @param message log this message
    * @param t log this cause
    */
   public void fatal(Object message, Throwable t)
   {
      m_log.fatal(message, t);
   }
}
