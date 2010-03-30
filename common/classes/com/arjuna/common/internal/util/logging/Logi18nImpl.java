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
* Logi18nImpl.java
*
* Copyright (c) 2003 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* Created on Jun 30, 2003, 12:48:49 PM by Thomas Rischbeck
*/
package com.arjuna.common.internal.util.logging;

import com.arjuna.common.util.logging.*;

import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

/**
 * Implementation of the Log interface without i18n support.
 *
 * Most log subsystems do not provide i18n, therefore we do resource
 * bundle evaluation in this class.
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * @since clf-2.0
 */
public class Logi18nImpl implements Logi18n
{
    private ResourceBundle m_resourceBundle = null;

    /**
     * Interface to the log subsystem to use
     */
    private final LogInterface m_logInterface;

    /**
     * constructor
     *
     * @param logInterface the underlying logger to wrap
     * @param resBundle bundle used for this logger (if a resource bundle is used per logger)
     */
    public Logi18nImpl(LogInterface logInterface, String resBundle)
    {
       m_logInterface = logInterface;
       addResourceBundle(resBundle);
    }

    /**
    * Add the given resource bundle to this logger.
    *
    * @param bundleName The name of the resource bundle.
    */
   private synchronized void addResourceBundle(String bundleName)
   {
      try
      {
         m_resourceBundle = PropertyResourceBundle.getBundle(bundleName, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
      }
      catch (MissingResourceException mre)
      {
         System.err.println("resource bundle " + mre.getClassName() + " not found!");
      }
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
    * @param key unique key to identify an entry in the resource bundle.
    * @return The localised string according to user's locale and available resource bundles. placeholder message
    *    if the resource bundle or key cannot be found.
    */
   public String getString(String key)
   {
      try
      {
         return getResourceBundleString(key);
      }
      catch (MissingResourceException mre)
      {
         return mre.getLocalizedMessage() + ": [key='" + key + "']";
      }
   }

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
    */
   public String getString(String key, Object[] params)
   {
      try
      {
          String pattern = getResourceBundleString(key);
          return java.text.MessageFormat.format(pattern, params);
      }
      catch (MissingResourceException mre)
      {
         StringBuffer sb = new StringBuffer();
          for(Object param : params) {
              sb.append(param);
              sb.append( ", ");
          }
         return mre.getLocalizedMessage() + ": [key='" + key + "']" + sb.toString();
      }
   }

   /**
    *
    * @param key the message key
    * @return the resource bundle String
    * @throws MissingResourceException if the default bundle is not set
    */
   synchronized String getResourceBundleString(String key) throws MissingResourceException
   {
      String resource = null;
      if (m_resourceBundle == null)
      {
         throw new MissingResourceException("no resource bundle set for this logger", null, null);
      }
      else
      {
          resource = m_resourceBundle.getString(key);
      }
      return resource;
   }

    /**
     * Log a message with DEBUG Level
     *
     * @param key resource bundle key for the message to log
     *
     * @deprecated use debug (String key, null) instead
     */
    public void debug(String key)
    {
        if (!isDebugEnabled()) return;
        String message = evalResourceBundle(key);
        m_logInterface.debug(message);
    }

    /**
     * Log a message with the DEBUG Level and with a throwable arguments
     * @param key resource bundle key for the message to log
     * @param throwable The Throwable to log
     */
    public void debug(String key, Throwable throwable)
    {
        if (!isDebugEnabled()) return;
        String message = evalResourceBundle(key);
        m_logInterface.debug(message, throwable);
    }

    /**
     * Log a message with DEBUG Level and with arguments
     *
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    public void debug(String key, Object[] params)
    {
        if (!isDebugEnabled()) return;
        String message = evalResourceBundle(key, params);
        m_logInterface.debug(message);
    }

    /**
     * Log a message with DEBUG Level, with arguments and with a throwable arguments
     *
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     * @param throwable The Throwable to log
     */
    public void debug(String key, Object[] params, Throwable throwable)
    {
        if (!isDebugEnabled()) return;
        String message = evalResourceBundle(key, params);
        m_logInterface.debug(message, throwable);
    }

    /**
     * Log a message with INFO Level
     *
     * @param key resource bundle key for the message to log
     *
     * @deprecated use info (String key, null) instead
     */
    public void info(String key)
    {
        if (!isInfoEnabled()) return;
        String message = evalResourceBundle(key);
        m_logInterface.info(message);
    }

    /**
     * Log a message with the INFO Level and with a throwable arguments
     * @param key resource bundle key for the message to log
     * @param throwable Throwable associated to the logging message
     */
    public void info(String key, Throwable throwable)
    {
        if (!isInfoEnabled()) return;
        String message = evalResourceBundle(key);
        m_logInterface.info(message, throwable);
    }

    /**
     * Log a message with the INFO Level and  with arguments
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    public void info(String key, Object[] params)
    {
        if (!isInfoEnabled()) return;
        String message = evalResourceBundle(key, params);
        m_logInterface.info(message);
    }

    /**
     * Log a message with the INFO Level, with arguments and with a throwable arguments
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     * @param throwable Throwable associated with the logging request
     */
    public void info(String key, Object[] params, Throwable throwable)
    {
        if (!isInfoEnabled()) return;
        String message = evalResourceBundle(key, params);
        m_logInterface.info(message, throwable);
    }

    /**
     * Log a message with the WARN Level
     * @param key resource bundle key for the message to log
     *
     * @deprecated use warn (String key, null) instead
     */
    public void warn(String key)
    {
        if (!isWarnEnabled()) return;
        String message = evalResourceBundle(key);
        m_logInterface.warn(message);
    }

    /**
     * Log a message with the WARN Level and with a throwable arguments
     * @param key resource bundle key for the message to log
     * @param throwable Throwable associated with the logging request
     */
    public void warn(String key, Throwable throwable)
    {
        if (!isWarnEnabled()) return;
        String message = evalResourceBundle(key);
        m_logInterface.warn(message, throwable);
    }

    /**
     * Log a message with the WARN Level and  with arguments
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    public void warn(String key, Object[] params)
    {
        if (!isWarnEnabled()) return;
        String message = evalResourceBundle(key, params);
        m_logInterface.warn(message);
    }

    /**
     * Log a message with the WARN Level, with arguments and with a throwable arguments
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     * @param throwable Throwable associated with the logging request
     */
    public void warn(String key, Object[] params, Throwable throwable)
    {
        if (!isWarnEnabled()) return;
        String message = evalResourceBundle(key, params);
        m_logInterface.warn(message, throwable);
    }

    /**
     * Log a message with the ERROR Level
     * @param key resource bundle key for the message to log
     *
     * @deprecated use error (String key, null) instead
     */
    public void error(String key)
    {
        if (!isErrorEnabled()) return;
        String message = evalResourceBundle(key);
        m_logInterface.error(message);
    }

    /**
     * Log a message with the ERROR Level and with a throwable arguments
     * @param key resource bundle key for the message to log
     * @param throwable Throwable associated with the logging request
     */
    public void error(String key, Throwable throwable)
    {
        if (!isErrorEnabled()) return;
        String message = evalResourceBundle(key);
        m_logInterface.error(message, throwable);
    }

    /**
     * Log a message with the ERROR Level and  with arguments
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    public void error(String key, Object[] params)
    {
        if (!isErrorEnabled()) return;
        String message = evalResourceBundle(key, params);
        m_logInterface.error(message);
    }

    /**
     * Log a message with the ERROR Level, with arguments and with a throwable arguments
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     * @param throwable Throwable associated with the logging request
     */
    public void error(String key, Object[] params, Throwable throwable)
    {
        if (!isErrorEnabled()) return;
        String message = evalResourceBundle(key, params);
        m_logInterface.error(message, throwable);
    }

    /**
     * Log a message with the FATAL Level
     * @param key resource bundle key for the message to log
     *
     * @deprecated use fatal (String key, null) instead
     */
    public void fatal(String key)
    {
        if (!isFatalEnabled()) return;
        String message = evalResourceBundle(key);
        m_logInterface.fatal(message);
    }

    /**
     * Log a message with the FATAL Level and with a throwable arguments
     * @param key resource bundle key for the message to log
     * @param throwable Throwable associated with the logging request
     */
    public void fatal(String key, Throwable throwable)
    {
        if (!isFatalEnabled()) return;
        String message = evalResourceBundle(key);
        m_logInterface.fatal(message, throwable);
    }

    /**
     * Log a message with the FATAL Level and  with arguments
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    public void fatal(String key, Object[] params)
    {
        if (!isFatalEnabled()) return;
        String message = evalResourceBundle(key, params);
        m_logInterface.fatal(message);
    }

    /**
     * Log a message with the FATAL Level, with arguments and with a throwable arguments
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     * @param throwable Throwable associated with the logging request
     */
    public void fatal(String key, Object[] params, Throwable throwable)
    {
        if (!isFatalEnabled()) return;
        String message = evalResourceBundle(key, params);
        m_logInterface.fatal(message, throwable);
    }

    // -------------------------------------------------------------------------------------
    // internal methods
    // -------------------------------------------------------------------------------------

    protected String evalResourceBundle(String key)
    {
        return getString(key);
    }

    protected String evalResourceBundle(String key, Object[] params)
    {
        return getString(key, params);
    }
}
