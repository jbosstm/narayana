/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.common.internal.util.logging;

/**
 * Variant of Logi18nImpl that relies on the underlying logging framework to perform i18n on messages.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-01
 */
public class Logi18nDelegatingImpl extends Logi18nImpl
{
    private final Logi18nInterface logi18nInterface;

    public Logi18nDelegatingImpl(Logi18nInterface logi18nInterface, String resBundle)
    {
        super(logi18nInterface, resBundle);
        this.logi18nInterface = logi18nInterface;
    }

    /**
     * Log a message with DEBUG Level and with arguments
     *
     * @param key    resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    @Override
    public void debug(String key, Object[] params)
    {
        if (!isDebugEnabled()) return;
        logi18nInterface.debug(key, params);
    }

    /**
     * Log a message with DEBUG Level, with arguments and with a throwable arguments
     *
     * @param key       resource bundle key for the message to log
     * @param params    parameters passed to the message
     * @param throwable The Throwable to log
     */
    @Override
    public void debug(String key, Object[] params, Throwable throwable)
    {
        if (!isDebugEnabled()) return;
        logi18nInterface.debug(key, params, throwable);
    }

    /**
     * Log a message with the INFO Level and  with arguments
     *
     * @param key    resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    @Override
    public void info(String key, Object[] params)
    {
        if (!isInfoEnabled()) return;
        logi18nInterface.info(key, params);
    }

    /**
     * Log a message with the INFO Level, with arguments and with a throwable arguments
     *
     * @param key       resource bundle key for the message to log
     * @param params    parameters passed to the message
     * @param throwable Throwable associated with the logging request
     */
    @Override
    public void info(String key, Object[] params, Throwable throwable)
    {
        if (!isInfoEnabled()) return;
        logi18nInterface.info(key, params, throwable);
    }

    /**
     * Log a message with the WARN Level and  with arguments
     *
     * @param key    resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    @Override
    public void warn(String key, Object[] params)
    {
        if (!isWarnEnabled()) return;
        logi18nInterface.warn(key, params);
    }

    /**
     * Log a message with the WARN Level, with arguments and with a throwable arguments
     *
     * @param key       resource bundle key for the message to log
     * @param params    parameters passed to the message
     * @param throwable Throwable associated with the logging request
     */
    @Override
    public void warn(String key, Object[] params, Throwable throwable)
    {
        if (!isWarnEnabled()) return;
        logi18nInterface.warn(key, params, throwable);
    }

    /**
     * Log a message with the ERROR Level and  with arguments
     *
     * @param key    resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    @Override
    public void error(String key, Object[] params)
    {
        if (!isErrorEnabled()) return;
        logi18nInterface.error(key, params);
    }

    /**
     * Log a message with the ERROR Level, with arguments and with a throwable arguments
     *
     * @param key       resource bundle key for the message to log
     * @param params    parameters passed to the message
     * @param throwable Throwable associated with the logging request
     */
    @Override
    public void error(String key, Object[] params, Throwable throwable)
    {
        if (!isErrorEnabled()) return;
        logi18nInterface.error(key, params, throwable);
    }

    /**
     * Log a message with the FATAL Level and  with arguments
     *
     * @param key    resource bundle key for the message to log
     * @param params parameters passed to the message
     */
    @Override
    public void fatal(String key, Object[] params)
    {
        if (!isFatalEnabled()) return;
        logi18nInterface.fatal(key, params);
    }

    /**
     * Log a message with the FATAL Level, with arguments and with a throwable arguments
     *
     * @param key       resource bundle key for the message to log
     * @param params    parameters passed to the message
     * @param throwable Throwable associated with the logging request
     */
    @Override
    public void fatal(String key, Object[] params, Throwable throwable)
    {
        if (!isFatalEnabled()) return;
        logi18nInterface.fatal(key, params, throwable);
    }

    @Override
    protected String evalResourceBundle(String key)
    {
        // postpone key resolution, effectively passing the key verbatim to the delegate, which will then resolve it.
        return key;
    }

    @Override
    protected String evalResourceBundle(String key, Object[] params)
    {
        throw new RuntimeException("this should never be called, anything that uses it should be overridden");
    }
}
