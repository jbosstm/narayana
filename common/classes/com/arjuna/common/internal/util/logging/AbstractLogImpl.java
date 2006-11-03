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
* LogImpl.java
*
* Copyright (c) 2003 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* Created on Jun 30, 2003, 12:48:49 PM by Thomas Rischbeck
*/
package com.arjuna.common.internal.util.logging;

import com.arjuna.common.util.logging.*;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.util.Locale;
import java.text.MessageFormat;

/**
 * Abstract implementation of the Log interface.
 *
 * Abstract superclass for {@link LogImpl LogImpl (without i18n support)}
 * and the {@link Logi18nImpl Logi18nImpl (built-in i18n support if the logger
 * does not provide this}. This contains the functionality common to both,
 * essentially support for finer-grained logging and guards (e.g.,
 * isWarnEnabled(), etc.).
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * @since clf-2.0
 */
public abstract class AbstractLogImpl implements Logi18n
{

   /**
    * Abstract Log interface (extended by i18n enabled or non-i18n log interfaces).
    */
   private AbstractLogInterface m_logInterface = null;

   /**
    * default resource bundle for this logger.
    *
    * Note that it is also possible to use a resource bundle name as argument in log statements if so required.
    * (this is only used by the AMS client library and we might remove this feature because of performance)
    *
    * all extra resource bundles are kept in {@link #m_extraResourceBundles m_extraResourceBundles}.
    *
    * @deprecated only used temporariliy for support of CSF
    */
   protected String m_resourceBundleName = "no resource bundle set";

   private ResourceBundle m_defaultResourceBundle = null;

   /**
    * extra resource bundles (if more than one is in use)
    *
    * Note that there is a performance issue when more than one resource bundles is in use.
    *
    * @see #m_defaultResourceBundle.
    */
   //protected String[] m_extraResBundles = null;
   private ResourceBundle[] m_extraResourceBundles = null;

   /**
    * Level for finer-debug logging.
    *
    * @see DebugLevel for possible values.
    */
   private long m_debugLevel = DebugLevel.NO_DEBUGGING;

   /**
    * log level for visibility-based logging
    *
    * @see VisibilityLevel for possible values.
    */
   private long m_visLevel = VisibilityLevel.VIS_ALL;

   /**
    * log level for facility code
    *
    * @see FacilityCode for possible values.
    */
   private long m_facLevel = FacilityCode.FAC_ALL;


   /**
    * constructor
    *
    * @param logInterface
    */
   public AbstractLogImpl(AbstractLogInterface logInterface)
   {
      m_logInterface = logInterface;
   }

   /**
    * constructor
    *
    * @param logInterface
    * @param resBundle bundle used for this logger (if a resource bundle is used per logger)
    */
   public AbstractLogImpl(AbstractLogInterface logInterface, String resBundle)
   {
      m_logInterface = logInterface;
      //m_resourceBundleName = resBundle;
      //m_defaultResourceBundle = PropertyResourceBundle.getBundle(resBundle);
      addResourceBundle(resBundle);
   }

   /**
    * constructor
    *
    * @param logInterface
    * @param resBundles a set of resource bundles (if a resource bundle is used per logger)
    * @deprecated Note: This implementation is optimised for using a single per-module resource bundle or direct
    *   resource use of multiple resource bundles reduces performance -- use this only if really necessary.
    */
   public AbstractLogImpl(AbstractLogInterface logInterface, String[] resBundles)
   {
      m_logInterface = logInterface;
      for (int i = 0; i < resBundles.length; i++)
      {
         addResourceBundle(resBundles[i]);
      }
   }


   /**
    * Set the name of the resource bundle name that the logger will use
    * to retreive national text
    *
    * @param baseName The default resource bundle name the logger uses to retreive messages
    */
   public synchronized void setResourceBundleName(String baseName)
   {
      try
      {
         m_resourceBundleName = baseName;
         m_defaultResourceBundle = PropertyResourceBundle.getBundle(baseName, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
      }
      catch (MissingResourceException mre)
      {
         System.err.println("resource bundle " + mre.getClassName() + " not found!");
      }
   }

   /**
    * Add the given resource bundle to this logger.
    *
    * @param bundleName The name of the resource bundle.
    */
   public synchronized void addResourceBundle(String bundleName)
   {
      try
      {
         // if no default resource bundle has been set, use this one as the default one
         if (m_defaultResourceBundle == null)
         {
            m_defaultResourceBundle = PropertyResourceBundle.getBundle(bundleName, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
            m_resourceBundleName = bundleName;
         }

         // otherwise, add it to the extra resource bundles supported by this logger
         else if (m_extraResourceBundles == null)
         {
            m_extraResourceBundles = new ResourceBundle[]{PropertyResourceBundle.getBundle(bundleName, Locale.getDefault(), Thread.currentThread().getContextClassLoader())};
         }
         else
         {
            ResourceBundle[] oldExtraResBundles = m_extraResourceBundles;
            m_extraResourceBundles = new ResourceBundle[oldExtraResBundles.length + 1];
            System.arraycopy(oldExtraResBundles, 0, m_extraResourceBundles, 0, oldExtraResBundles.length);
            m_extraResourceBundles[oldExtraResBundles.length] = PropertyResourceBundle.getBundle(bundleName, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
         }
      }
      catch (MissingResourceException mre)
      {
         System.err.println("resource bundle " + mre.getClassName() + " not found!");
      }
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
    *           See {@link DebugLevel DebugLevel} for possible values.
    * @param vl The visibility level value.
    *           See {@link VisibilityLevel VisibilityLevel} for possible values.
    * @param fl The facility code level value.
    *           See {@link FacilityCode FacilityCode} for possible values.
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
    * @see DebugLevel for possible return values.
    */
   public long getDebugLevel()
   {
      return m_debugLevel;
   }

   /**
    * Set the debug level as available in the {@link DebugLevel DebugLevel}.
    *
    * @param level The finer debugging value
    * @see DebugLevel for possible values of <code>level</code>.
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
    * @see DebugLevel for possible values of <code>level</code>.
    */
   public void mergeDebugLevel(long level)
   {
      m_debugLevel |= level;
   }

   /**
    * Return the visibility level.
    *
    * @return The visibility level value associated with the Logger
    * @see VisibilityLevel for possible return values.
    */
   public long getVisibilityLevel()
   {
      return m_visLevel;
   }

   /**
    * Set the visibility level.
    *
    * @param level The visibility level value
    * @see VisibilityLevel for possible values of <code>level</code>.
    */
   public void setVisibilityLevel(long level)
   {
      m_visLevel = level;
   }

   /**
    * Merge the visibility level provided with that currently used by the Logger.
    *
    * @param level The visibility level value
    * @see VisibilityLevel for possible values of <code>level</code>.
    */
   public void mergeVisibilityLevel(long level)
   {
      m_visLevel |= level;
   }

   /**
    * Return the facility code.
    *
    * @return The facility code value associated with the Logger.
    * @see FacilityCode for possible return values.
    */
   public long getFacilityCode()
   {
      return m_facLevel;
   }

   /**
    * Set the facility code.
    *
    * @param level The facility code value
    * @see FacilityCode for possible values of <code>level</code>.
    */
   public void setFacilityCode(long level)
   {
      m_facLevel = level;
   }

   /**
    * Merge the debug level provided with that currently used by the Logger.
    *
    * @param level The visibility level value
    * @see FacilityCode for possible values of <code>level</code>.
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
    * Log a message with the DEBUG Level and with finer granularity. The throwable message
    * is sent to the output only if the specified debug level, visibility level, and facility code
    * match those allowed by the logger.
    * <p>
    * <b>note</b>: this method does not use i18n.
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
    * @param throwable Throwable associated with the log message
    */
   public void debug(long dl, long vl, long fl, Throwable throwable)
   {
      if (debugAllowed(dl, vl, fl))
      {
         debug(null, null, throwable);
      }
   }

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
   public void debug(long dl, long vl, long fl, String key, Object[] params)
   {
      if (debugAllowed(dl, vl, fl))
      {
         debug(key, params);
      }
   }

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
   public void debug(long dl, long vl, long fl, String key)
   {
      if (debugAllowed(dl, vl, fl))
      {
         debug(key);
      }

   }

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
   public void debug(long dl, long vl, long fl, String key, Throwable throwable)
   {
      if (debugAllowed(dl, vl, fl))
      {
         debug(key, throwable);
      }
   }

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
   public void debug(long dl, long vl, long fl, String key, Object[] params, Throwable throwable)
   {
      if (debugAllowed(dl, vl, fl))
      {
         debug(key, params, throwable);
      }
   }


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
   public String getString(String key) throws MissingResourceException
   {
      String msg = null;
      try
      {
         msg = getResourceBundleString(key);
      }
      catch (MissingResourceException mre)
      {
         return mre.getLocalizedMessage() + ": [key='" + key + "']";
      }
      return msg;
   }

   /**
    * Obtain a localized and parameterized message from one of the resource
    * bundles associated with this logger.
    *
    * First, the user supplied <code>key</code> is searched in the resource
    * bundle. Next, the resulting pattern is formatted using
    * {@link MessageFormat#format(String,Object[])} method with the
    * user supplied object array <code>params</code>.
    *
    * @param key unique key to identify an entry in the resource bundle.
    * @param params parameters to fill placeholders (e.g., {0}, {1}) in the resource bundle string.
    * @return The localised string according to user's locale and available resource bundles. placeholder message
    *    if the resource bundle or key cannot be found.
    * //@throws MissingResourceException if the key cannot be found in any of the associated resource bundles.
    */
   public String getString(String key, Object[] params) throws MissingResourceException
   {
      String pattern = null;
      try
      {
         pattern = getResourceBundleString(key);
      }
      catch (MissingResourceException mre)
      {
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < params.length; i++)
         {
            sb.append(params[i] + ", ");
         }
         return mre.getLocalizedMessage() + ": [key='" + key + "']" + sb.toString();
      }
      String msg;
      msg = java.text.MessageFormat.format(pattern, params);
      return msg;
   }

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
   public String getString(String base, String key) throws MissingResourceException
   {
      try
      {
         ResourceBundle rb = PropertyResourceBundle.getBundle(base, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
         String msg = rb.getString(key);
         return msg;
      }
      catch (MissingResourceException mre)
      {
         return mre.getLocalizedMessage() + ": [key='" + key + "']";
      }
   }

   /**
    * Obtain a localized and parameterized message from the given resource bundle.
    *
    * First, the user supplied <code>key</code> is searched in the resource
    * bundle. Next, the resulting pattern is formatted using
    * {@link MessageFormat#format(String,Object[])} method with the
    * user supplied object array <code>params</code>.
    *
    * @param base resource bundle name
    * @param key unique key to identify an entry in the resource bundle.
    * @param params parameters to fill placeholders (e.g., {0}, {1}) in the resource bundle string.
    * @return The localised string according to user's locale and available resource bundles. placeholder message
    *    if the resource bundle or key cannot be found.
    * //@throws MissingResourceException if the key cannot be found in any of the associated resource bundles.
    */
   public String getString(String base, String key, Object[] params) throws MissingResourceException
   {
      try
      {
         ResourceBundle rb = PropertyResourceBundle.getBundle(base, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
         String pattern = rb.getString(key);
         String msg = java.text.MessageFormat.format(pattern, params);
         return msg;
      }
      catch (MissingResourceException mre)
      {
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < params.length; i++)
         {
            sb.append(params[i] + ", ");
         }
         return mre.getLocalizedMessage() + ": [key='" + key + "']" + sb.toString();
      }
   }

   /**
    *
    * @param key
    * @return
    * @throws MissingResourceException
    */
   protected synchronized String getResourceBundleString(String key) throws MissingResourceException
   {
      String resource = null;
      if (m_defaultResourceBundle == null)
      {
         throw new MissingResourceException("no default resource bundle set for this logger", null, null);
      }
      else
      {
         try
         {
            resource = m_defaultResourceBundle.getString(key);
         }
               // this exception will occur if no resource for the given key can be found.
         catch (MissingResourceException mre)
         {
            // if no extra res bundles are available, that's it. bail out!
            if (m_extraResourceBundles == null)
            {
               throw mre;
            }
            else
            {
               for (int i = 0; i < m_extraResourceBundles.length; i++)
               {
                  try
                  {
                     resource = m_extraResourceBundles[i].getString(key);
                  }
                  catch (MissingResourceException mre2)
                  {
                     mre = mre2;
                  }
               }
               // if no resource for the given key could be found in any of the
               // resource bundles, throw an exception.
               if (resource == null)
               {
                  throw mre;
               }
            }
         }
      }
      return resource;
   }
}
