/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.arjuna.ats.internal.jbossatx.logging;

import com.arjuna.common.internal.util.logging.LogInterface;

import com.arjuna.common.internal.util.logging.Logi18nInterface;
import org.jboss.logmanager.ExtLogRecord;
import org.jboss.logmanager.Logger;
import org.jboss.logmanager.Level;

/**
 * CLF log impl for integration with JBoss log manager.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-11
 */
public final class JBossLogger implements Logi18nInterface
{
    private final Logger logger;
    private static final String FQCN = JBossLogger.class.getName();

    public JBossLogger(final String categoryName)
    {
        logger = Logger.getLogger(categoryName);
    }

    public JBossLogger(final String categoryName, final String resourceBundleName) {
        logger = Logger.getLogger(categoryName, resourceBundleName);
    }

    /////////////

    /**
     * Is DEBUG logging currently enabled?
     * <p/>
     * Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than DEBUG.
     *
     * @return True if the logger is enabled for DEBUG, false otherwise
     */
    @Override
    public boolean isDebugEnabled()
    {
        return logger.isLoggable(Level.DEBUG);
    }

    /**
     * Is INFO logging currently enabled?
     * <p/>
     * Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than INFO.
     *
     * @return True if the logger is enabled for INFO, false otherwise
     */
    @Override
    public boolean isInfoEnabled()
    {
        return logger.isLoggable(Level.INFO);
    }

    /**
     * Is WARN logging currently enabled?
     * <p/>
     * Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than WARN.
     *
     * @return True if the logger is enabled for WARN, false otherwise
     */
    @Override
    public boolean isWarnEnabled()
    {
        return logger.isLoggable(Level.WARN);
    }

    /**
     * Is ERROR logging currently enabled?
     * <p/>
     * Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than ERROR.
     *
     * @return True if the logger is enabled for ERROR, false otherwise
     */
    @Override
    public boolean isErrorEnabled()
    {
        return logger.isLoggable(Level.ERROR);
    }

    /**
     * Is FATAL logging currently enabled?
     * <p/>
     * Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than FATAL.
     *
     * @return True if the logger is enabled for FATAL, false otherwise
     */
    @Override
    public boolean isFatalEnabled()
    {
        return logger.isLoggable(Level.FATAL);
    }

    /**
     * Is TRACE logging currently enabled?
     * <p/>
     * Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than TRACE.
     */
    @Override
    public boolean isTraceEnabled()
    {
        return logger.isLoggable(Level.TRACE);
    }

    /**
     * <p> Log a message with trace log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void trace(String message)
    {
        logger.log(FQCN, Level.TRACE, message, null);
    }

    /**
     * <p> Log an error with trace log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void trace(String message, Throwable t)
    {
        logger.log(FQCN, Level.TRACE, message, t);
    }
    /**
     * <p> Log a message with debug log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void debug(String message)
    {
        logger.log(FQCN, Level.DEBUG, message, null);
    }

    /**
     * <p> Log an error with debug log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void debug(String message, Throwable t)
    {
        logger.log(FQCN, Level.DEBUG, message, t);
    }


    @Override
    public void debug(String key, Object[] params)
    {
        logger.log(FQCN, Level.DEBUG, key, ExtLogRecord.FormatStyle.MESSAGE_FORMAT, params, null);
    }

    @Override
    public void debug(String key, Object[] params, Throwable throwable)
    {
        logger.log(FQCN, Level.DEBUG, key, ExtLogRecord.FormatStyle.MESSAGE_FORMAT, params, throwable);
    }

    /**
     * <p> Log a message with info log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void info(String message)
    {
        logger.log(FQCN, Level.INFO, message, null);
    }

    /**
     * <p> Log an error with info log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void info(String message, Throwable t)
    {
        logger.log(FQCN, Level.INFO, message, t);
    }

     @Override
    public void info(String key, Object[] params)
    {
        logger.log(FQCN, Level.INFO, key, ExtLogRecord.FormatStyle.MESSAGE_FORMAT, params, null);
    }

    @Override
    public void info(String key, Object[] params, Throwable throwable)
    {
        logger.log(FQCN, Level.INFO, key, ExtLogRecord.FormatStyle.MESSAGE_FORMAT, params, throwable);
    }

   /**
     * <p> Log a message with warn log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void warn(String message)
    {
        logger.log(FQCN, Level.WARN, message, null);
    }

    @Override
    public void warn(String key, Object[] params)
    {
        logger.log(FQCN, Level.WARN, key, ExtLogRecord.FormatStyle.MESSAGE_FORMAT, params, null);

    }

    @Override
    public void warn(String key, Object[] params, Throwable throwable)
    {
        logger.log(FQCN, Level.WARN, key, ExtLogRecord.FormatStyle.MESSAGE_FORMAT, params, throwable);
    }

    /**
     * <p> Log an error with warn log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void warn(String message, Throwable t)
    {
        logger.log(FQCN, Level.WARN, message, t);
    }

    /**
     * <p> Log a message with error log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void error(String message)
    {
        logger.log(FQCN, Level.ERROR, message, null);
    }

    /**
     * <p> Log an error with error log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void error(String message, Throwable t)
    {
        logger.log(FQCN, Level.WARN, message, t);
    }

    @Override
    public void error(String key, Object[] params)
    {
        logger.log(FQCN, Level.ERROR, key, ExtLogRecord.FormatStyle.MESSAGE_FORMAT, params, null);

    }

    @Override
    public void error(String key, Object[] params, Throwable throwable)
    {
        logger.log(FQCN, Level.ERROR, key, ExtLogRecord.FormatStyle.MESSAGE_FORMAT, params, throwable);
    }

    /**
     * <p> Log a message with fatal log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void fatal(String message)
    {
        logger.log(FQCN, Level.FATAL, message, null);
    }

    /**
     * <p> Log an error with fatal log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void fatal(String message, Throwable t)
    {
        logger.log(FQCN, Level.FATAL, message, t);
    }

    @Override
    public void fatal(String key, Object[] params)
    {
        logger.log(FQCN, Level.FATAL, key, ExtLogRecord.FormatStyle.MESSAGE_FORMAT, params, null);

    }

    @Override
    public void fatal(String key, Object[] params, Throwable throwable)
    {
        logger.log(FQCN, Level.FATAL, key, ExtLogRecord.FormatStyle.MESSAGE_FORMAT, params, throwable);
    }
}
