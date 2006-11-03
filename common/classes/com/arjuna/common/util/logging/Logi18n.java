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
 * Internationalised logging interface abstracting the various logging APIs
 * supported by Arjuna CLF.
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
 * @since clf-2.0
 */
public interface Logi18n
{

   /**
    * Add the given resource bundle to this logger.
    *
    * @param bundleName The name of the resource bundle.
    */
   public void addResourceBundle(String bundleName);

   /**
    * Determine if this logger is enabled for DEBUG messages.
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
    * Log a message with DEBUG Level
    *
    * @param key resource bundle key for the message to log
    */
   void debug(String key);

   /**
    * Log a throwable message with DEBUG Level
    *
    * @param throwable The Throwable to log
    * @deprecated Use debug(String key, Throwable throwable) instead
    */
   void debug(Throwable throwable);

   /**
    * Log a message with the DEBUG Level and with a throwable arguments
    * @param key resource bundle key for the message to log
    * @param throwable The Throwable to log
    */
   void debug(String key, Throwable throwable);

   /**
    * Log a message with DEBUG Level and with arguments
    *
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    */
   void debug(String key, Object[] params);

   /**
    * Log a message with DEBUG Level, with arguments and with a throwable arguments
    *
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    * @param throwable The Throwable to log
    */
   void debug(String key, Object[] params, Throwable throwable);

   /************************   Log Info Messages   ****************************/

   /**
    * Log a message with INFO Level
    *
    * @param key resource bundle key for the message to log
    */
   void info(String key);

   /**
    * Log a throwable message with te INFO Level
    * @param throwable The Throwable to log
    * @deprecated Use info(String key, Throwable throwable) instead
    */
   void info(Throwable throwable);

   /**
    * Log a message with the INFO Level and with a throwable arguments
    * @param key resource bundle key for the message to log
    * @param throwable Throwable associated to the logging message
    */
   void info(String key, Throwable throwable);

   /**
    * Log a message with the INFO Level and  with arguments
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    */
   void info(String key, Object[] params);

   /**
    * Log a message with the INFO Level, with arguments and with a throwable arguments
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    * @param throwable Throwable associated with the logging request
    */
   void info(String key, Object[] params, Throwable throwable);


   /************************   Log Warn Messages   ****************************/

   /**
    * Log a message with the WARN Level
    * @param key resource bundle key for the message to log
    */
   void warn(String key);

   /**
    * Log a throwable message with te WARN Level
    * @param throwable Throwable associated with the logging request
    * @deprecated Use warn(String key, Throwable throwable) instead
    */
   void warn(Throwable throwable);

   /**
    * Log a message with the WARN Level and with a throwable arguments
    * @param key resource bundle key for the message to log
    * @param throwable Throwable associated with the logging request
    */
   void warn(String key, Throwable throwable);

   /**
    * Log a message with the WARN Level and  with arguments
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    */
   void warn(String key, Object[] params);

   /**
    * Log a message with the WARN Level, with arguments and with a throwable arguments
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    * @param throwable Throwable associated with the logging request
    */
   void warn(String key, Object[] params, Throwable throwable);


   /************************   Log Error Messages   ****************************/

   /**
    * Log a message with the ERROR Level
    * @param key resource bundle key for the message to log
    */
   void error(String key);

   /**
    * Log a throwable message with te ERROR Level
    * @param throwable Throwable associated with the logging request
    * @deprecated Use error(String key, Throwable throwable) instead
    */
   void error(Throwable throwable);

   /**
    * Log a message with the ERROR Level and with a throwable arguments
    * @param key resource bundle key for the message to log
    * @param throwable Throwable associated with the logging request
    */
   void error(String key, Throwable throwable);

   /**
    * Log a message with the ERROR Level and  with arguments
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    */
   void error(String key, Object[] params);

   /**
    * Log a message with the ERROR Level, with arguments and with a throwable arguments
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    * @param throwable Throwable associated with the logging request
    */
   void error(String key, Object[] params, Throwable throwable);

   /************************   Log Fatal Messages   ****************************/

   /**
    * Log a message with the FATAL Level
    * @param key resource bundle key for the message to log
    */
   void fatal(String key);

   /**
    * Log a throwable message with te FATAL Level
    * @param throwable Throwable associated with the logging request
    * @deprecated Use fatal(String key, Throwable throwable) instead
    */
   void fatal(Throwable throwable);

   /**
    * Log a message with the FATAL Level and with a throwable arguments
    * @param key resource bundle key for the message to log
    * @param throwable Throwable associated with the logging request
    */
   void fatal(String key, Throwable throwable);

   /**
    * Log a message with the FATAL Level and  with arguments
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    */
   void fatal(String key, Object[] params);

   /**
    * Log a message with the FATAL Level, with arguments and with a throwable arguments
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    * @param throwable Throwable associated with the logging request
    */
   void fatal(String key, Object[] params, Throwable throwable);



   /**************************** Debug Granularity Extension ***************************/

   /**
    * Set the debug level, the visibility level, and the facility code.
    *
    * @param dl The finer debugging value.
    *           See {@link DebugLevel DebugLevel} for possible values.
    * @param vl The visibility level value.
    *           See {@link VisibilityLevel VisibilityLevel} for possible values.
    * @param fl The facility code level value.
    *           See {@link FacilityCode FacilityCode} for possible values.
    */
   void setLevels (long dl, long vl, long fl);

   /**
    * Return the finer debug level.
    *
    * @return The finer debugging level value associated with the logger
    * @see DebugLevel for possible return values.
    */
   long getDebugLevel ();

   /**
    * Set the debug level as available in the {@link DebugLevel DebugLevel}.
    *
    * @param level The finer debugging value
    * @see DebugLevel for possible values of <code>level</code>.
    */
   void setDebugLevel (long level);

   /**
    * Merge the debug level provided with that currently used by
    * the controller.
    *
    * @param level The finer debugging value
    * @see DebugLevel for possible values of <code>level</code>.
    */
   void mergeDebugLevel (long level);

   /**
    * Return the visibility level.
    *
    * @return The visibility level value associated with the Logger
    * @see VisibilityLevel for possible return values.
    */
   long getVisibilityLevel ();

   /**
    * Set the visibility level.
    *
    * @param level The visibility level value
    * @see VisibilityLevel for possible values of <code>level</code>.
    */
   void setVisibilityLevel (long level);

   /**
    * Merge the visibility level provided with that currently used by the Logger.
    *
    * @param level The visibility level value
    * @see VisibilityLevel for possible values of <code>level</code>.
    */
   void mergeVisibilityLevel (long level);

   /**
    * Return the facility code.
    *
    * @return The facility code value associated with the Logger.
    * @see FacilityCode for possible return values.
    */
   long getFacilityCode ();

   /**
    * Set the facility code.
    *
    * @param level The facility code value
    * @see FacilityCode for possible values of <code>level</code>.
    */
   void setFacilityCode (long level);


   /**
    * Merge the debug level provided with that currently used by the Logger.
    *
    * @param level The visibility level value
    * @see FacilityCode for possible values of <code>level</code>.
    */
   void mergeFacilityCode (long level);


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
   boolean debugAllowed ();


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
   boolean debugAllowed (long dLevel);


   /**
    * Is it allowed to print debugging statements?
    *
    * This method assumes <pre> FacilityCode.FAC_ALL)</pre>
    * @return true if the logger allows logging for the finer debugging values - dLevel and vLevel
    * @param dLevel The debug finer level. Used to ask if the logger object allows logging for this value.
    * The answer is yes if dLevel is either equals or greater the debug level assigned to the logger
    * @param vLevel The visibility level. Used to ask if the logger object allows logging for this value.
    * The answer is yes if vLevel is either equals or greater the visibility level assigned to the logger
    */
   boolean debugAllowed (long dLevel, long vLevel);


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
   boolean debugAllowed (long dLevel, long vLevel, long fLevel);



   /*************** logging and ResourceBundle Name *******************/

   /**
    * Set the name of the resource bundle name that the logger will use
    * to retreive national text
    * @param BaseName The default resource bundle name the logger uses to retreive messages
    */
   void setResourceBundleName(String BaseName);


   /************************   Log Debug Messages   ****************************/

   /**
    * Log a message with the DEBUG Level
    * @param baseName The name of resource bundle to localize message
    * @param key The resource bundle key to retrieve a localised string
    */
   void debugb(String baseName, String key);


   /**
    * Log a message with the DEBUG Level and with a throwable arguments
    * @param baseName The name of resource bundle to localize message
    * @param key The resource bundle key to retrieve a localised string
    * @param throwable Throwable associated with the log message
    */
   void debugb(String baseName, String key, Throwable throwable);

   /**
    * Log a message with the DEBUG Level and  with arguments
    * @param baseName The name of resource bundle to localize message
    * @param key The resource bundle key to retrieve a localised string
    * @param params parameters passed to the message
    */
   void debugb(String baseName, String key, Object[] params);

   /**
    * Log a message with the DEBUG Level, with arguments and with a throwable arguments
    * @param baseName The name of resource bundle to localize message
    * @param key The resource bundle key to retrieve a localised string
    * @param params parameters passed to the message
    * @param throwable Throwable associated with the log message
    */
   void debugb(String baseName, String key, Object[] params, Throwable throwable);




   /************************   Log Info Messages  with bundle Name ****************************/

   /**
    * Log a message with the INFO Level
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    */
   void infob(String baseName, String key);

   /**
    * Log a message with the INFO Level and with a throwable arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param throwable Throwable associated with the log message
    */
   void infob(String baseName, String key, Throwable throwable);

   /**
    * Log a message with the INFO Level and  with arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    */
   void infob(String baseName, String key, Object[] params);

   /**
    * Log a message with the INFO Level, with arguments and with a throwable arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    * @param throwable Throwable associated with the log message
    */
   void infob(String baseName, String key, Object[] params, Throwable throwable);


   /************************   Log Warn Messages with Bundle Name ****************************/

   /**
    * Log a message with the WARN Level
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    */
   void warnb(String baseName, String key);

   /**
    * Log a message with the WARN Level and with a throwable arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param throwable Throwable associated with the log message
    */
   void warnb(String baseName, String key, Throwable throwable);

   /**
    * Log a message with the WARN Level and  with arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    */
   void warnb(String baseName, String key, Object[] params);

   /**
    * Log a message with the WARN Level, with arguments and with a throwable arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    * @param throwable Throwable associated with the log message
    */
   void warnb(String baseName, String key, Object[] params, Throwable throwable);


   /************************   Log Error Messages with Bundle Name ****************************/

   /**
    * Log a message with the ERROR Level
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    */
   void errorb(String baseName, String key);

   /**
    * Log a message with the ERROR Level and with a throwable arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param throwable Throwable associated with the log message
    */
   void errorb(String baseName, String key, Throwable throwable);

   /**
    * Log a message with the ERROR Level and  with arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    */
   void errorb(String baseName, String key, Object[] params);


   /**
    * Log a message with the ERROR Level, with arguments and with a throwable arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    * @param throwable Throwable associated with the log message
    */
   void errorb(String baseName, String key, Object[] params, Throwable throwable);



   /************************   Log Fatal Messages with Bundle Name **************************/

   /**
    * Log a message with the FATAL Level
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    */
   void fatalb(String baseName, String key);


   /**
    * Log a message with the FATAL Level and with a throwable arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param throwable Throwable associated with the log message
    */
   void fatalb(String baseName, String key, Throwable throwable);


   /**
    * Log a message with the FATAL Level and  with arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    */
   void fatalb(String baseName, String key, Object[] params);


   /**
    * Log a message with the FATAL Level, with arguments and with a throwable arguments
    * @param baseName The name of resource bundle to localize message
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    * @param throwable Throwable associated with the log message
    */
   void fatalb(String baseName, String key, Object[] params, Throwable throwable);


   /**********************************************************************************************************
    * Finer-Granularity Debug Methods.
    **********************************************************************************************************/


   /**
    * Log a message with the DEBUG Level and with finer granularity.
    * The debug message is sent to the output only if the specified debug level, visibility level,
    * and facility code match those allowed by the logger.
    * @param dl The debug finer level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and dl is either equals or greater the debug level assigned to
    * the logger Object
    * @param vl The visibility level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and vl is either equals or greater the visibility level assigned to
    * the logger Object
    * @param fl The facility code level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and fl is either equals or greater the facility code level assigned to
    * the logger Object
    * @param key resource bundle key for the message to log
    */
   void debug(long dl, long vl, long fl, String key);

      /**
    * Log a message with the DEBUG Level and with finer granularity and throwable message.
    * The debug message is sent to the output only if the specified debug level, visibility level,
    * and facility code match those allowed by the logger.
    * @param dl The debug finer level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and dl is either equals or greater the debug level assigned to
    * the logger Object
    * @param vl The visibility level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and vl is either equals or greater the visibility level assigned to
    * the logger Object
    * @param fl The facility code level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and fl is either equals or greater the facility code level assigned to
    * the logger Object
    * @param key resource bundle key for the message to log
    * @param throwable Throwable associated with the log message
    */
   void debug(long dl, long vl, long fl, String key, Throwable throwable);

   /**
    * Log a message with the DEBUG Level and with finer granularity and with arguments. The debug message
    * is sent to the output only if the specified debug level, visibility level, and facility code
    * match those allowed by the logger.
    * @param dl The debug finer level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and dl is either equals or greater the debug level assigned to
    * the logger Object
    * @param vl The visibility level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and vl is either equals or greater the visibility level assigned to
    * the logger Object
    * @param fl The facility code level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and fl is either equals or greater the facility code level assigned to
    * the logger Object
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    */
   void debug(long dl, long vl, long fl, String key, Object[] params);


   /**
    * Log a message with the DEBUG Level and with finer granularity, arguments and throwable message.
    * The debug message is sent to the output only if the specified debug level, visibility level,
    * and facility code match those allowed by the logger.
    * @param dl The debug finer level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and dl is either equals or greater the debug level assigned to
    * the logger Object
    * @param vl The visibility level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and vl is either equals or greater the visibility level assigned to
    * the logger Object
    * @param fl The facility code level associated with the log message. That is, the logger object allows
    * to log only if the DEBUG level is allowed and fl is either equals or greater the facility code level assigned to
    * the logger Object
    * @param key resource bundle key for the message to log
    * @param params parameters passed to the message
    * @param throwable Throwable associated with the log message
    */
   void debug(long dl, long vl, long fl, String key, Object[] params, Throwable throwable);

   /**********************************************************************************************************/

   /**********************************************************************************************************/
   /* RESOURCE BUNDLE EVALUATION */
   /**********************************************************************************************************/

   /**
    * Obtain a localized message from one of the resource bundles associated
    * with this logger.
    *
    * The user supplied parameter <code>key</code> is replaced by its localized
    * version from the resource bundle.
    *
    * @see #setResourceBundleName
    *
    * @param key unique key to identify an entry in the resource bundle.
    * @return The localised string according to user's locale and available resource bundles. placeholder message
    *    if the resource bundle or key cannot be found.
    * //@throws MissingResourceException if the key cannot be found in any of the associated resource bundles.
    */
   public String getString(String key); //throws MissingResourceException;


   /**
    * Obtain a localized and parameterized message from one of the resource
    * bundles associated with this logger.
    *
    * First, the user supplied <code>key</code> is searched in the resource
    * bundle. Next, the resulting pattern is formatted using
    * {@link java.text.MessageFormat#format(String,Object[])} method with the
    * user supplied object array <code>params</code>.
    *
    * @param key unique key to identify an entry in the resource bundle.
    * @param params parameters to fill placeholders (e.g., {0}, {1}) in the resource bundle string.
    * @return The localised string according to user's locale and available resource bundles. placeholder message
    *    if the resource bundle or key cannot be found.
    * //@throws MissingResourceException if the key cannot be found in any of the associated resource bundles.
    */
   public String getString(String key, Object[] params); // throws MissingResourceException;

   /**
    * Obtain a localized message from one of the resource bundles associated
    * with this logger.
    *
    * The user supplied parameter <code>key</code> is replaced by its localized
    * version from the resource bundle <code>base</code>.
    *
    * @see #setResourceBundleName
    *
    * @param base resource bundle name
    * @param key unique key to identify an entry in the resource bundle.
    * @return The localised string according to user's locale and available resource bundles. placeholder message
    *    if the resource bundle or key cannot be found.
    * //@throws MissingResourceException if the key cannot be found in any of the associated resource bundles.
    */
   public String getString(String base, String key); // throws MissingResourceException;

   /**
    * Obtain a localized and parameterized message from the given resource bundle.
    *
    * First, the user supplied <code>key</code> is searched in the resource
    * bundle. Next, the resulting pattern is formatted using
    * {@link java.text.MessageFormat#format(String,Object[])} method with the
    * user supplied object array <code>params</code>.
    *
    * @param base resource bundle name
    * @param key unique key to identify an entry in the resource bundle.
    * @param params parameters to fill placeholders (e.g., {0}, {1}) in the resource bundle string.
    * @return The localised string according to user's locale and available resource bundles. placeholder message
    *    if the resource bundle or key cannot be found.
    * //@throws MissingResourceException if the key cannot be found in any of the associated resource bundles.
    */
   public String getString(String base, String key, Object[] params); // throws MissingResourceException;

}
