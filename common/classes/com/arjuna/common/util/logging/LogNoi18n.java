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
* Log.java
*
* Copyright (c) 2003 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* Created on Jun 30, 2003, 10:04:22 AM by Thomas Rischbeck
*/
package com.arjuna.common.util.logging;

/**
 * Non-internationalised logging interface abstracting the various logging APIs
 * supported by Arjuna CLF.
 *
 * Non-i18n messages should generally be debug (trace) level or raw exceptions. All
 * textual messages at higher levels should go through the i18n logger instead.
 *
 * See {@link Logi18n Logi18n} for an internationalised version and for more
 * information.
 *
 * <p> The five logging levels used by <code>Log</code> are (in order):
 * <ol>
 * <li>debug (the least serious</li>
 * <li>info</li>
 * <li>warn</li>
 * <li>error</li>
 * <li>fatal (the most serious)</li>
 * </ol>
 *
 * The mapping of these log levels to the concepts used by the underlying
 * logging system is implementation dependent. The implemention should ensure,
 * though, that this ordering behaves as expected.</p>
 *
 * <p>Performance is often a logging concern. By examining the appropriate property,
 * a component can avoid expensive operations (producing information
 * to be logged).</p>
 *
 * <p> For example,
 * <code><pre>
 *    if (log.isDebugEnabled()) {
 *        ... do something expensive ...
 *        log.debug(theResult);
 *    }
 * </pre></code>
 * </p>
 *
 * <p>Configuration of the underlying logging system will generally be done
 * external to the Logging APIs, through whatever mechanism is supported by
 * that system.</p>
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 */
public interface LogNoi18n
{

   /**
    * Determine if this logger is enabled for DEBUG messages.
    *
    * This method returns true when the following is set:
    * <ul>
    * <li>finer debug level = <code>DebugLevel.FULL_DEBUGGING</code>.</li>
    * <li>visibility level = <code>VisibilityLevel.VIS_ALL</code>.</li>
    * <li>facility code = <code>FacilityCode.FAC_ALL</code>.</li>
    *
    * @return  True if the logger is enabled for DEBUG, false otherwise
    */
   boolean isDebugEnabled();

   /**
    * Determine if this logger is enabled for INFO messages.
    * @return  True if the logger is enabled for INFO, false otherwise
    */
   boolean isInfoEnabled();

   /**
    * Determine if this logger is enabled for WARN messages.
    * @return  True if the logger is enabled for WARN, false otherwise
    */
   boolean isWarnEnabled();

   /**
    * Determine if this logger is enabled for ERROR messages.
    * @return  True if the logger is enabled for ERROR, false otherwise
    */
   boolean isErrorEnabled();

   /**
    * Determine if this logger is enabled for FATAL messages.
    * @return  True if the logger is enabled for FATAL, false otherwise
    */
   boolean isFatalEnabled();


   /************************   Log Debug Messages   ****************************/

   /**
    * Log a message with INFO Level
    *
    * @param message the message to log
    * @deprecated exceptions at info level don't make a lot sense.
    */
   void info(Throwable message);

   /**
    * Log a message with WARN Level
    *
    * @param message the message to log
    */
   void warn(Throwable message);

   /**
    * Log a message with ERROR Level
    *
    * @param message the message to log
    */
   void error(Throwable message);

   /**
    * Log a message with FATAL Level
    *
    * @param message the message to log
    */
   void fatal(Throwable message);

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
   void debug(long dl, long vl, long fl, String message);
}
