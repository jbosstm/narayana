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
* LogFactory.java
*
* Copyright (c) 2003 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* Created on Jun 27, 2003, 3:40:14 PM by Thomas Rischbeck
*/
package com.arjuna.common.util.logging;

import com.arjuna.common.internal.util.logging.*;
import com.arjuna.common.util.exceptions.LogConfigurationException;

import java.lang.reflect.Constructor;

/**
 * Factory for {@link Logi18n} and {@link LogNoi18n} objects.
 *
 * LogFactory instantiates and returns different implementations of Logi18n and LogNoi18n according to
 * which logging subsystem is configured.
 *
 * The LoggingEnvironmentBean.loggingFactory property supplies factory setup information which is used to
 * instantiate a LogFactoryInterface implementation, from which a LogInterface implementation is then obtained.
 * See the environment bean for factory config options and also log level settings.
 * 
 * The LogInterface impl provides access to the underlying logging system for our logging abstraction layer.
 * Logi18n and LogNoi18n implementations wrap the LogInterface impl and are passed back to the user code.
 * Note the assumption that the underlying log system is not i18n aware i.e. message internationalization is done
 * in the Logi18n impl before messages are passed to the LogInterface.
 * TODO: this model need revision so we can support i18n aware underlying loggers.
 *
 * Note: Log subsystems are not configured through CLF but instead rely on their own configuration files for
 * the setup of eg. debug level, appenders, etc...
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * @since clf-2.0
 */
public class LogFactory {

	/**
     * Interface that encapsulates the underlying, log-system-specific log factory.
     */
    private static LogFactoryInterface m_logFactory = null;

    /**
     * variable for lazy initialization of the log subsystem to use.
     *
     * This replaces the static initializer which was prematuerly executed
     * (before setting the com.arjuna.common.util.logger system property)
     * by some embeddors (eg tomcat servlet engine).
     */
    private static boolean m_isInitialized = false;

    /**
     * Level for finer-debug logging.
     *
     * @see DebugLevel for possible values.
     */
    private static long m_debugLevel = DebugLevel.NO_DEBUGGING;

    /**
     * log level for visibility-based logging
     *
     * @see VisibilityLevel for possible values.
     */
    private static long m_visLevel = VisibilityLevel.VIS_ALL;

    /**
     * log level for facility code
     *
     * @see FacilityCode for possible values.
     */
    private static long m_facLevel = FacilityCode.FAC_ALL;

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped).
     * @return a LogNoi18n instance
     * <p>
     */
    public static LogNoi18n getLogNoi18n(String name) {
        setupLogSystem();
        LogInterface logInterface = m_logFactory.getLog(name);
        LogNoi18n log = new LogNoi18nImpl(logInterface, m_debugLevel, m_visLevel, m_facLevel);
        return log;
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped)
     * @param resBundle resource bundle associated with the returned logger.
     * @return a Logi18n instance
     */
    public static Logi18n getLogi18n(String name, String resBundle) {
        setupLogSystem();
        LogInterface logInterface = m_logFactory.getLog(name);
        Logi18n log = new Logi18nImpl(logInterface, resBundle, m_debugLevel, m_visLevel, m_facLevel);
        return log;
    }

    /**
     * set up the log subsystem to use.
     */
    private static synchronized void setupLogSystem() {
        if (m_isInitialized) {
            return;
        }

        String debugLevel;
        String facLevel;
        String visLevel;
        String logFactory;

        try
        {
                // find out which log subsystem to use:
                logFactory = commonPropertyManager.getLoggingEnvironmentBean().getLoggingFactory();

                debugLevel = commonPropertyManager.getLoggingEnvironmentBean().getDebugLevel();
                facLevel = commonPropertyManager.getLoggingEnvironmentBean().getFacilityLevel();
                visLevel = commonPropertyManager.getLoggingEnvironmentBean().getVisibilityLevel();

            try {
                m_debugLevel = Long.decode(debugLevel);
            } catch (NumberFormatException nfe) {
                m_debugLevel = 0x0;
            }

            try {
                m_facLevel = Long.decode(facLevel);
            }
            catch (NumberFormatException nfe )
            {
                m_debugLevel = 0xfffffff;
            }

            try {
                m_visLevel = Long.decode(visLevel);
            }
            catch (NumberFormatException nfe )
            {
                m_debugLevel = 0xfffffff;
            }
            
            int semicolonIndex = logFactory.indexOf(";");
            if(semicolonIndex == -1) {
                m_logFactory = loadFactory(logFactory, null);
            } else {
                m_logFactory = loadFactory(logFactory.substring(0, semicolonIndex), logFactory.substring(semicolonIndex+1));
            }

        } catch (LogConfigurationException e) {
            throw new RuntimeException("An unexpected exception occurred while creating the logger factory: " + e.getMessage(), e);
        }
        m_isInitialized = true;
    }

    /*
     * Use reflection to avoid linking against factories that may need specific libs to be present at runtime.
     */
    private static LogFactoryInterface loadFactory(String classname, String arg) throws LogConfigurationException
    {
        try {
            Class factoryClass = Thread.currentThread().getContextClassLoader().loadClass(classname);
            LogFactoryInterface logFactoryInterface;
            if(arg == null) {
                logFactoryInterface = (LogFactoryInterface)factoryClass.newInstance();
            } else {
                Constructor ctor = factoryClass.getConstructor(new Class[] { String.class});
                logFactoryInterface = (LogFactoryInterface)ctor.newInstance(arg);
            }
            return logFactoryInterface;
        } catch (Exception e) {
            throw new LogConfigurationException(e);
        }
    }
}
