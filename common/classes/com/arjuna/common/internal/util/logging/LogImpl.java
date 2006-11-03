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

/**
 * Implementation of the Log interface without i18n support.
 *
 * Most log subsystems do not provide i18n, therefore we do resource
 * bundle evaluation in this class. An exception is the CSF logging.
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * @since clf-2.0
 */
public class LogImpl extends AbstractLogImpl implements Logi18n
{

    /**
     * Interface to the log subsystem to use
     */
    private LogInterface m_logInterface = null;

    /**
     * a default file-based logger for a certain severity level.
     * this can be configured to always be on, independt of the underlying
     * log framwork configured. this is controlled through teh property
     * com.arjuna.common.logging.default=true/false (default)
     */
    private DefaultLog m_defaultLog = null;

    /**
     * indicates whehter the default log is set or not.
     */
    private boolean defaultLogSet = false;


    /**
     * constructor
     *
     * @param logInterface
     */
    public LogImpl(LogInterface logInterface)
    {
        super(logInterface);
        m_logInterface = logInterface;
        setupDefaultLog("default");
    }

    /**
     * constructor
     *
     * @param logInterface
     * @param resBundle bundle used for this logger (if a resource bundle is used per logger)
     */
    public LogImpl(LogInterface logInterface, String resBundle)
    {
        super(logInterface, resBundle);
        m_logInterface = logInterface;
        setupDefaultLog(resBundle);
    }

    /**
     * constructor
     *
     * @param logInterface
     * @param resBundles a set of resource bundles (if a resource bundle is used per logger)
     * @deprecated Note: This implementation is optimised for using a single per-module resource bundle or direct
     *   resource use of multiple resource bundles reduces performance -- use this only if really necessary.
     */
    public LogImpl(LogInterface logInterface, String[] resBundles)
    {
        super(logInterface, resBundles);
        m_logInterface = logInterface;
        setupDefaultLog(resBundles[0]);
    }

    private void setupDefaultLog(String name)
    {
        String defaultLog = "false";

        try {
            defaultLog = commonPropertyManager.propertyManager.getProperty(DefaultLog.LOG_ENABLED_PROPERTY, null);
            // if the property manager has no info set, use the system property
            // and if this isn't set either, default to false.
            if (defaultLog == null) {
                defaultLog = System.getProperty(DefaultLog.LOG_ENABLED_PROPERTY, "false");
            }
        }
        catch (Throwable t)
        {
            // an exception could occur when trying to read system properties when we run in an applet
            // sandbox. therefore, ignore the trowable and just keep with the default settings above.

        }

        if (defaultLog.equals("true"))
        {
            m_defaultLog = new DefaultLog(name);
            defaultLogSet = true;
        }
    }


    /**
     * Determine if this logger is enabled for DEBUG messages.
     * @return  True if the logger is enabled for DEBUG, false otherwise
     */
    public boolean isDebugEnabled()
    {
        if (defaultLogSet)
        {
            return m_logInterface.isDebugEnabled() || m_defaultLog.isDebugEnabled();
        }
        return m_logInterface.isDebugEnabled();
    }

    /**
     * Determine if this logger is enabled for INFO messages.
     * @return  True if the logger is enabled for INFO, false otherwise
     */
    public boolean isInfoEnabled()
    {
        if (defaultLogSet)
        {
            return m_logInterface.isInfoEnabled() || m_defaultLog.isInfoEnabled();
        }
        return m_logInterface.isInfoEnabled();
    }

    /**
     * Determine if this logger is enabled for WARN messages.
     * @return  True if the logger is enabled for WARN, false otherwise
     */
    public boolean isWarnEnabled()
    {
        if (defaultLogSet)
        {
            return m_logInterface.isWarnEnabled() || m_defaultLog.isWarnEnabled();
        }
        return m_logInterface.isWarnEnabled();
    }

    /**
     * Determine if this logger is enabled for ERROR messages.
     * @return  True if the logger is enabled for ERROR, false otherwise
     */
    public boolean isErrorEnabled()
    {
        if (defaultLogSet)
        {
            return m_logInterface.isErrorEnabled() || m_defaultLog.isErrorEnabled();
        }
        return m_logInterface.isErrorEnabled();
    }

    /**
     * Determine if this logger is enabled for FATAL messages.
     * @return  True if the logger is enabled for FATAL, false otherwise
     */
    public boolean isFatalEnabled()
    {
        if (defaultLogSet)
        {
            return m_logInterface.isFatalEnabled() || m_defaultLog.isFatalEnabled();
        }
        return m_logInterface.isFatalEnabled();
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
        if (defaultLogSet)
            m_defaultLog.debug(message);
    }

    /**
     * Log a throwable message with DEBUG Level
     *
     * @param throwable The Throwable to log
     */
    public void debug(Throwable throwable)
    {
        if (!isDebugEnabled()) return;
        String message = throwable.getLocalizedMessage();
        m_logInterface.debug(message, throwable);
        if (defaultLogSet)
            m_defaultLog.debug(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.debug(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.debug(message);
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
        if (defaultLogSet)
            m_defaultLog.debug(message, throwable);
    }

    /**
     * Log a message with DEBUG Level, with arguments and with a throwable arguments
     *
     * @param key resource bundle key for the message to log
     * @param param0 parameters passed to the message
     *
     * @deprecated only provided temporarily to ease transition of the AMS server to clf-2.0. this invocation should
     *    be changed to <code>debug(key, new Object[] {param0, param1})</code>.
     */
    //public void debug(String key, Object param0)
    //{
    //   if (!isDebugEnabled()) return;
    //   debug(key, new Object[]{param0});
    //}

    /**
     * Log a message with DEBUG Level, with arguments and with a throwable arguments
     *
     * @param key resource bundle key for the message to log
     * @param param0 parameters passed to the message
     * @param param1 parameters passed to the message
     *
     * @deprecated only provided temporarily to ease transition of the AMS server to clf-2.0. this invocation should
     *    be changed to <code>debug(key, new Object[] {param0})</code>.
     */
    //public void debug(String key, Object param0, Object param1)
    //{
    //   if (!isDebugEnabled()) return;
    //   debug(key, new Object[]{param0, param1});
    //}

    /**
     * Log a message with DEBUG Level, with arguments and with a throwable arguments
     *
     * @param key resource bundle key for the message to log
     * @param param0 parameters passed to the message
     * @param param1 parameters passed to the message
     * @param param2 parameters passed to the message
     *
     * @deprecated only provided temporarily to ease transition of the AMS server to clf-2.0. this invocation should
     *    be changed to <code>debug(key, new Object[] {param0, param1, param2})</code>.
     */
    //public void debug(String key, Object param0, Object param1, Object param2)
    //{
    //   if (!isDebugEnabled()) return;
    //   debug(key, new Object[]{param0, param1, param2});
    //}

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
        if (defaultLogSet)
            m_defaultLog.info(message);
    }

    /**
     * Log a throwable message with te INFO Level
     * @param throwable The Throwable to log
     */
    public void info(Throwable throwable)
    {
        if (!isInfoEnabled()) return;
        String message = throwable.getLocalizedMessage();
        m_logInterface.info(message, throwable);
        if (defaultLogSet)
            m_defaultLog.info(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.info(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.info(message);
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
        if (defaultLogSet)
            m_defaultLog.info(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.warn(message);
    }

    /**
     * Log a throwable message with te WARN Level
     * @param throwable Throwable associated with the logging request
     */
    public void warn(Throwable throwable)
    {
        if (!isWarnEnabled()) return;
        String message = throwable.getLocalizedMessage();
        m_logInterface.warn(message, throwable);
        if (defaultLogSet)
            m_defaultLog.warn(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.warn(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.warn(message);
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
        if (defaultLogSet)
            m_defaultLog.warn(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.error(message);
    }

    /**
     * Log a throwable message with te ERROR Level
     * @param throwable Throwable associated with the logging request
     */
    public void error(Throwable throwable)
    {
        if (!isErrorEnabled()) return;
        String message = throwable.getLocalizedMessage();
        m_logInterface.error(message, throwable);
        if (defaultLogSet)
            m_defaultLog.error(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.error(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.error(message);
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
        if (defaultLogSet)
            m_defaultLog.error(message, throwable);
    }


    /**
     * Log a message with ERROR Level, with arguments and with a throwable arguments
     *
     * @param key resource bundle key for the message to log
     * @param param0 parameters passed to the message
     *
     * @deprecated only provided temporarily to ease transition of the AMS server to clf-2.0. this invocation should
     *    be changed to <code>error(key, new Object[] {param0, param1})</code>.
     */
    //public void error(String key, Object param0)
    //{
    //   if (!isErrorEnabled()) return;
    //   error(key, new Object[]{param0});
    //}

    /**
     * Log a message with ERROR Level, with arguments and with a throwable arguments
     *
     * @param key resource bundle key for the message to log
     * @param param0 parameters passed to the message
     * @param param1 parameters passed to the message
     *
     * @deprecated only provided temporarily to ease transition of the AMS server to clf-2.0. this invocation should
     *    be changed to <code>error(key, new Object[] {param0})</code>.
     */
    //public void error(String key, Object param0, Object param1)
    //{
    //   if (!isErrorEnabled()) return;
    //   error(key, new Object[]{param0, param1});
    //}

    /**
     * Log a message with ERROR Level, with arguments and with a throwable arguments
     *
     * @param key resource bundle key for the message to log
     * @param param0 parameters passed to the message
     * @param param1 parameters passed to the message
     * @param param2 parameters passed to the message
     *
     * @deprecated only provided temporarily to ease transition of the AMS server to clf-2.0. this invocation should
     *    be changed to <code>error(key, new Object[] {param0, param1, param2})</code>.
     */
    //public void error(String key, Object param0, Object param1, Object param2)
    //{
    //   if (!isErrorEnabled()) return;
    //   error(key, new Object[]{param0, param1, param2});
    //}

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
        if (defaultLogSet)
            m_defaultLog.fatal(message);
    }

    /**
     * Log a throwable message with te FATAL Level
     * @param throwable Throwable associated with the logging request
     */
    public void fatal(Throwable throwable)
    {
        if (!isFatalEnabled()) return;
        String message = throwable.getLocalizedMessage();
        m_logInterface.fatal(message, throwable);
        if (defaultLogSet)
            m_defaultLog.fatal(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.fatal(message, throwable);
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
        if (defaultLogSet)
            m_defaultLog.fatal(message);
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
        if (defaultLogSet)
            m_defaultLog.fatal(message, throwable);
    }


    /**
     * Log a message with the DEBUG Level
     * @param baseName The name of resource bundle to localize message
     * @param key The resource bundle key to retrieve a localised string
     */
    public void debugb(String baseName, String key)
    {
        if (!isDebugEnabled()) return;
        String message = evalResourceBundle(baseName, key);
        m_logInterface.debug(message);
        if (defaultLogSet)
            m_defaultLog.debug(message);
    }

    /**
     * Log a message with the DEBUG Level and with a throwable arguments
     * @param baseName The name of resource bundle to localize message
     * @param key The resource bundle key to retrieve a localised string
     * @param throwable Throwable associated with the log message
     */
    public void debugb(String baseName, String key, Throwable throwable)
    {
        if (!isDebugEnabled()) return;
        String message = evalResourceBundle(baseName, key);
        m_logInterface.debug(message, throwable);
        if (defaultLogSet)
            m_defaultLog.debug(message, throwable);
    }

    /**
     * Log a message with the DEBUG Level and  with arguments
     * @param baseName The name of resource bundle to localize message
     * @param key The resource bundle key to retrieve a localised string
     * @param params parameters passed to the message
     */
    public void debugb(String baseName, String key, Object[] params)
    {
        if (!isDebugEnabled()) return;
        String message = evalResourceBundle(baseName, key, params);
        m_logInterface.debug(message);
        if (defaultLogSet)
            m_defaultLog.debug(message);
    }

    /**
     * Log a message with the DEBUG Level, with arguments and with a throwable arguments
     * @param baseName The name of resource bundle to localize message
     * @param key The resource bundle key to retrieve a localised string
     * @param params parameters passed to the message
     * @param throwable Throwable associated with the log message
     */
    public void debugb(String baseName, String key, Object[] params, Throwable throwable)
    {
        if (!isDebugEnabled()) return;
        String message = evalResourceBundle(baseName, key, params);
        m_logInterface.debug(message, throwable);
        if (defaultLogSet)
            m_defaultLog.debug(message, throwable);
    }


    /**
     * Log a message with the INFO Level
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     */
    public void infob(String baseName, String key)
    {
        if (!isInfoEnabled()) return;
        String message = evalResourceBundle(baseName, key);
        m_logInterface.info(message);
        if (defaultLogSet)
            m_defaultLog.info(message);
    }

    /**
     * Log a message with the INFO Level and with a throwable arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param throwable Throwable associated with the log message
     */
    public void infob(String baseName, String key, Throwable throwable)
    {
        if (!isInfoEnabled()) return;
        String message = evalResourceBundle(baseName, key);
        m_logInterface.info(message, throwable);
        if (defaultLogSet)
            m_defaultLog.info(message, throwable);
    }

    /**
     * Log a message with the INFO Level and  with arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    public void infob(String baseName, String key, Object[] params)
    {
        if (!isInfoEnabled()) return;
        String message = evalResourceBundle(baseName, key, params);
        m_logInterface.info(message);
        if (defaultLogSet)
            m_defaultLog.info(message);
    }

    /**
     * Log a message with the INFO Level, with arguments and with a throwable arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     * @param throwable Throwable associated with the log message
     */
    public void infob(String baseName, String key, Object[] params, Throwable throwable)
    {
        if (!isInfoEnabled()) return;
        String message = evalResourceBundle(baseName, key, params);
        m_logInterface.info(message, throwable);
        if (defaultLogSet)
            m_defaultLog.info(message, throwable);
    }

    /**
     * Log a message with the WARN Level
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     */
    public void warnb(String baseName, String key)
    {
        if (!isWarnEnabled()) return;
        String message = evalResourceBundle(baseName, key);
        m_logInterface.warn(message);
        if (defaultLogSet)
            m_defaultLog.warn(message);
    }

    /**
     * Log a message with the WARN Level and with a throwable arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param throwable Throwable associated with the log message
     */
    public void warnb(String baseName, String key, Throwable throwable)
    {
        if (!isWarnEnabled()) return;
        String message = evalResourceBundle(baseName, key);
        m_logInterface.warn(message, throwable);
        if (defaultLogSet)
            m_defaultLog.warn(message, throwable);
    }

    /**
     * Log a message with the WARN Level and  with arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    public void warnb(String baseName, String key, Object[] params)
    {
        if (!isWarnEnabled()) return;
        String message = evalResourceBundle(baseName, key, params);
        m_logInterface.warn(message);
        if (defaultLogSet)
            m_defaultLog.warn(message);
    }

    /**
     * Log a message with the WARN Level, with arguments and with a throwable arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     * @param throwable Throwable associated with the log message
     */
    public void warnb(String baseName, String key, Object[] params, Throwable throwable)
    {
        if (!isWarnEnabled()) return;
        String message = evalResourceBundle(baseName, key, params);
        m_logInterface.warn(message, throwable);
        if (defaultLogSet)
            m_defaultLog.warn(message, throwable);
    }

    /**
     * Log a message with the ERROR Level
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     */
    public void errorb(String baseName, String key)
    {
        if (!isErrorEnabled()) return;
        String message = evalResourceBundle(baseName, key);
        m_logInterface.error(message);
        if (defaultLogSet)
            m_defaultLog.error(message);
    }

    /**
     * Log a message with the ERROR Level and with a throwable arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param throwable Throwable associated with the log message
     */
    public void errorb(String baseName, String key, Throwable throwable)
    {
        if (!isErrorEnabled()) return;
        String message = evalResourceBundle(baseName, key);
        m_logInterface.error(message, throwable);
        if (defaultLogSet)
            m_defaultLog.error(message, throwable);
    }

    /**
     * Log a message with the ERROR Level and  with arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    public void errorb(String baseName, String key, Object[] params)
    {
        if (!isErrorEnabled()) return;
        String message = evalResourceBundle(baseName, key, params);
        m_logInterface.error(message);
        if (defaultLogSet)
            m_defaultLog.error(message);
    }

    /**
     * Log a message with the ERROR Level, with arguments and with a throwable arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     * @param throwable Throwable associated with the log message
     */
    public void errorb(String baseName, String key, Object[] params, Throwable throwable)
    {
        if (!isErrorEnabled()) return;
        String message = evalResourceBundle(baseName, key, params);
        m_logInterface.error(message, throwable);
        if (defaultLogSet)
            m_defaultLog.error(message, throwable);
    }

    /**
     * Log a message with the FATAL Level
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     */
    public void fatalb(String baseName, String key)
    {
        if (!isFatalEnabled()) return;
        String message = evalResourceBundle(baseName, key);
        m_logInterface.fatal(message);
        if (defaultLogSet)
            m_defaultLog.fatal(message);
    }

    /**
     * Log a message with the FATAL Level and with a throwable arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param throwable Throwable associated with the log message
     */
    public void fatalb(String baseName, String key, Throwable throwable)
    {
        if (!isFatalEnabled()) return;
        String message = evalResourceBundle(baseName, key);
        m_logInterface.fatal(message, throwable);
        if (defaultLogSet)
            m_defaultLog.fatal(message, throwable);
    }

    /**
     * Log a message with the FATAL Level and  with arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    public void fatalb(String baseName, String key, Object[] params)
    {
        if (!isFatalEnabled()) return;
        String message = evalResourceBundle(baseName, key, params);
        m_logInterface.fatal(message);
        if (defaultLogSet)
            m_defaultLog.fatal(message);
    }

    /**
     * Log a message with the FATAL Level, with arguments and with a throwable arguments
     * @param baseName The name of resource bundle to localize message
     * @param key resource bundle key for the message to log
     * @param params parameters passed to the message
     * @param throwable Throwable associated with the log message
     */
    public void fatalb(String baseName, String key, Object[] params, Throwable throwable)
    {
        if (!isFatalEnabled()) return;
        String message = evalResourceBundle(baseName, key, params);
        m_logInterface.fatal(message, throwable);
        if (defaultLogSet)
            m_defaultLog.fatal(message, throwable);
    }



    // -------------------------------------------------------------------------------------
    // private methods
    // -------------------------------------------------------------------------------------

    private String evalResourceBundle(String key)
    {
        //if (key == null) key="null";
        //return (new ResourceMessage(m_resourceBundle, key)).toString();
        return getString(key);
    }

    private String evalResourceBundle(String key, Object[] params)
    {
        //if (key == null) key="null";
        //return (new ResourceMessage(m_resourceBundle, key, params)).toString();
        return getString(key, params);
    }

    private String evalResourceBundle(String resBundle, String key)
    {
        //if (key == null) key="null";
        //return (new ResourceMessage(resBundle, key)).toString();
        return getString(resBundle, key);
    }

    private String evalResourceBundle(String resBundle, String key, Object[] params)
    {
        //if (key == null) key="null";
        //return (new ResourceMessage(resBundle, key, params)).toString();
        return getString(resBundle, key, params);
    }

}
