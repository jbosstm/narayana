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
import com.arjuna.common.internal.util.logging.simpleLog.SimpleLogFactory;
import com.arjuna.common.internal.util.logging.jakarta.JakartaLogFactory;
import com.arjuna.common.internal.util.logging.jakarta.JakartaRelevelingLogFactory;
import com.arjuna.common.util.exceptions.LogConfigurationException;

/**
 * Factory for {@link Logi18n Log} objects.
 *
 * LogFactory returns different subclasses of logger according to which logging subsystem is chosen. The
 * log system is selected through the property <code>com.arjuna.common.utils.logger</code>.
 * Supported values for this property are:
 * <ul>
 *    <li><code><b>jakarta</b></code> Jakarta Commons Logging (JCL). JCL can delegate to various other logging subsystems, such as:
 *    <ul>
 *       <li>log4j</li>
 *       <li>JDK 1.4 logging</li>
 *       <li>Windows NT syslog</li>
 *       <li>console</li>
 *    </ul>
 *    </li>
 *    <li><code><b>dotnet</b></code> .net logging. (must be JDK 1.1 compliant for compilation by the Microsoft compiler)</li>
 * </ul>
 * Note: Log subsystems are not configured through CLF but instead rely on their own configuration files for
 * the setup of eg. debug level, appenders, etc...
 *
 * <h4>The Default <code>LogFactory</code> Implementation</h4>
 *
 * <p>The Logging Package APIs include a default <code>LogFactory</code>
 * implementation class (<a href="impl/LogFactoryImpl.html">
 * org.apache.commons.logging.impl.LogFactoryImpl</a>) that is selected if no
 * other implementation class name can be discovered.  Its primary purpose is
 * to create (as necessary) and return <a href="Log.html">Log</a> instances
 * in response to calls to the <code>getInstance()</code> method.  The default
 * implementation uses the following rules:</p>
 * <ul>
 * <li>At most one <code>Log</code> instance of the same name will be created.
 *     Subsequent <code>getInstance()</code> calls to the same
 *     <code>LogFactory</code> instance, with the same name or <code>Class</code>
 *     parameter, will return the same <code>Log</code> instance.</li>
 * <li>When a new <code>Log</code> instance must be created, the default
 *     <code>LogFactory</code> implementation uses the following discovery
 *     process is used:
 *     <ul>
 *     <li>Look for a configuration attribute of this factory named
 *         <code>org.apache.commons.logging.Log</code> (for backwards
 *         compatibility to pre-1.0 versions of this API, an attribute
 *         <code>org.apache.commons.logging.log</code> is also consulted)..</li>
 *     <li>Look for a system property named
 *         <code>org.apache.commons.logging.Log</code> (for backwards
 *         compatibility to pre-1.0 versions of this API, a system property
 *         <code>org.apache.commons.logging.log</code> is also consulted).</li>
 *     <li>If the Log4J logging system is available in the application
 *         class path, use the corresponding wrapper class
 *         (<a href="impl/Log4JLogger.html">Log4JLogger</a>).</li>
 *     <li>If the application is executing on a JDK 1.4 system, use
 *         the corresponding wrapper class
 *         (<a href="impl/Jdk14Logger.html">Jdk14Logger</a>).</li>
 *     <li>Fall back to the default simple logging wrapper
 *         (<a href="impl/SimpleLog.html">SimpleLog</a>).</li>
 *     </ul></li>
 * <li>Load the class of the specified name from the thread context class
 *     loader (if any), or from the class loader that loaded the
 *     <code>LogFactory</code> class otherwise.</li>
 * <li>Instantiate an instance of the selected <code>Log</code>
 *    implementation class, passing the specified name as the single
 *     argument to its constructor.</li>
 * </ul>
 *
 * @author Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $ $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * @since clf-2.0
 */
public class LogFactory {

    /**
     * this is the name of a system property that can be used to explicitly select a particular logging
     * subsystem.
     *
     * See the class description for supported values.
     */
    public static final String LOGGER_PROPERTY = "com.arjuna.common.util.logger";


    /***************************************************************************
     * Property names to control fine-grained logging
     *
     * TODO: TR: this requires some more thought. currently the values can only be set
     * for the root logger, but some hierarchical scheme would be nice
     * we might also want to explore dynamic proxies to automaticlaly obtain debug
     * output for all methods & constructors, etc ...
     */
    public static final String DEBUG_LEVEL = "com.arjuna.common.util.logging.DebugLevel";
    public static final String FACILITY_LEVEL = "com.arjuna.common.util.logging.FacilityLevel";
    public static final String VISIBILITY_LEVEL = "com.arjuna.common.util.logging.VisibilityLevel";

    /**
     * this property is used by the Jakarta Commons Logging implementation to select the underlying
     * logging framework to use. This can be set as a system property. if this property is not set,
     * JCL will select the implementation to use by
     */
    private static final String JCL_LOG_CONFIGURATION = "org.apache.commons.logging.Log";

    //private static final String CSF_LOGGER = "csf";
    private static final String JAKARTA_LOGGER = "jakarta";
    private static final String DOTNET_LOGGER = "dotnet";
    private static final String LOG4J = "log4j";
    private static final String JDK14 = "jdk14";
    private static final String SIMPLE = "simple";
    private static final String NOOP = "noop";
    //private static final String AVALON = "avalon";
	private static final String RELEVELER = "log4j_releveler";

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
     * <p>
     */
    public static LogNoi18n getLogNoi18n(String name) {
        LogNoi18n log = null;
        setupLogSystem();
        AbstractLogInterface logInterface = m_logFactory.getLog(name);
        if (logInterface instanceof LogInterface) {
            log = new LogNoi18nImpl((LogInterface) logInterface);
        } else {
            throw new RuntimeException("non i18n loggers are not supported for CSF!");
        }
        log.setLevels(m_debugLevel, m_visLevel, m_facLevel);
        return log;
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param clazz Class for which a log name will be derived
     */
    public static Logi18n getLogi18n(Class clazz) {
        Logi18n log = null;
        setupLogSystem();
        AbstractLogInterface logInterface = m_logFactory.getLog(clazz);
        log = new LogImpl((LogInterface) logInterface);
        log.setLevels(m_debugLevel, m_visLevel, m_facLevel);
        return log;
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped).
     * <p>
     *  Note that <code>name</code> is also used as the default resource bundle
     *  associated with the logger (although an explicit resource bundle is not
     *  required for the debugb, warnb, etc methods.
     */
    public static Logi18n getLogi18n(String name) {
        Logi18n log = null;
        setupLogSystem();
        AbstractLogInterface logInterface = m_logFactory.getLog(name);
        log = new LogImpl((LogInterface) logInterface, name);
        log.setLevels(m_debugLevel, m_visLevel, m_facLevel);
        return log;
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param clazz Class for which a log name will be derived
     * @param resBundle resource bundle to use for the logger
     */
    public static Logi18n getLogi18n(Class clazz, String resBundle) {
        Logi18n log = null;
        setupLogSystem();
        AbstractLogInterface logInterface = m_logFactory.getLog(clazz);
        log = new LogImpl((LogInterface) logInterface, resBundle);
        log.setLevels(m_debugLevel, m_visLevel, m_facLevel);
        return log;
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param clazz Class for which a log name will be derived
     * @param resBundles set of resource bundles to use for the logger
     *
     * @deprecated Note: This implementation is optimised for using a single per-module resource bundle or direct
     *   resource use of multiple resource bundles reduces performance -- use this only if really necessary.
     */
    public static Logi18n getLogi18n(Class clazz, String[] resBundles) {
        Logi18n log = null;
        setupLogSystem();
        AbstractLogInterface logInterface = m_logFactory.getLog(clazz);
        log = new LogImpl((LogInterface) logInterface, resBundles);
        log.setLevels(m_debugLevel, m_visLevel, m_facLevel);
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
     */
    public static Logi18n getLogi18n(String name, String resBundle) {
        Logi18n log = null;
        setupLogSystem();
        AbstractLogInterface logInterface = m_logFactory.getLog(name);
        log = new LogImpl((LogInterface) logInterface, resBundle);
        log.setLevels(m_debugLevel, m_visLevel, m_facLevel);
        return log;
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped)
     * @param resBundles set of resource bundles to use for the logger
     */
    public static Logi18n getLogi18n(String name, String[] resBundles) {
        Logi18n log = null;
        setupLogSystem();
        AbstractLogInterface logInterface = m_logFactory.getLog(name);
        log = new LogImpl((LogInterface) logInterface, resBundles);
        log.setLevels(m_debugLevel, m_visLevel, m_facLevel);
        return log;
    }

    /**
     * set up the log subsystem to use.
     */
    private static synchronized void setupLogSystem() {
        if (m_isInitialized) {
            return;
        }

        String debugLevel = "0xffffffff";
        String facLevel = "0xfffffff";
        String visLevel = "0xfffffff";
        String logSystem = NOOP;

        try
        {
            try
            {
                // find out which log subsystem to use; by default Jakarta Commons Logging:
                logSystem = commonPropertyManager.propertyManager.getProperty(LOGGER_PROPERTY, null);
                // if the property manager has no info set, use the system property
                // and if this isn't set either, default to JAKARTA simple logging.
                if (logSystem == null) {
                    logSystem = System.getProperty(LOGGER_PROPERTY, NOOP);
                }

                debugLevel = commonPropertyManager.propertyManager.getProperty(DEBUG_LEVEL, "0xffffffff");
                facLevel = commonPropertyManager.propertyManager.getProperty(FACILITY_LEVEL, "0xfffffff");
                visLevel = commonPropertyManager.propertyManager.getProperty(VISIBILITY_LEVEL, "0xfffffff");
            }
            catch (Throwable t)
            {
                // an exception could occur when trying to read system properties when we run in an applet
                // sandbox. therefore, ignore the trowable and just keep with the default settings above.

            }

            try {
                m_debugLevel = Long.decode(debugLevel).longValue();
            } catch (NumberFormatException nfe) {
                m_debugLevel = 0x0;
            }

            try {
                m_facLevel = Long.decode(facLevel).longValue();
            }
            catch (NumberFormatException nfe )
            {
                m_debugLevel = 0xfffffff;
            }

            try {
                m_visLevel = Long.decode(visLevel).longValue();
            }
            catch (NumberFormatException nfe )
            {
                m_debugLevel = 0xfffffff;
            }

            // .net simple logging is not currenlty supported, instead use
            // jakarta commons simple logging (it is pure Java 1.1) in the
            // current release:
            if (logSystem.equals(DOTNET_LOGGER)) logSystem = SIMPLE;


            // ALL THESE ARE SUPPORTED BY JAKARTA COMMONS LOGGING
            if (logSystem.equals(LOG4J)) {
                System.setProperty(JCL_LOG_CONFIGURATION, "org.apache.commons.logging.impl.Log4JLogger");
                m_logFactory = new JakartaLogFactory();
            } else if (logSystem.equals(JDK14)) {
                System.setProperty(JCL_LOG_CONFIGURATION, "org.apache.commons.logging.impl.Jdk14Logger");
                m_logFactory = new JakartaLogFactory();
            } else if (logSystem.equals(SIMPLE)) {
                System.setProperty(JCL_LOG_CONFIGURATION, "org.apache.commons.logging.impl.SimpleLog");
                m_logFactory = new JakartaLogFactory();
            } else if (logSystem.equals(NOOP)) {
                System.setProperty(JCL_LOG_CONFIGURATION, "org.apache.commons.logging.impl.NoOpLog");
                m_logFactory = new JakartaLogFactory();

            }

			// we use a slightly modified wrapper to do log statement level modification
			// for support of JBossAS log level semantics, see JakartaRelevelingLogger javadoc
			else if (logSystem.equals(RELEVELER)) {
				System.setProperty(JCL_LOG_CONFIGURATION, "org.apache.commons.logging.impl.Log4JLogger");
				m_logFactory = new JakartaRelevelingLogFactory();
			}

			// USE JAKARTA COMMONS LOGGINGS OWN DISCOVERY MECHANISM
            else if (logSystem.equals(JAKARTA_LOGGER)) {
                m_logFactory = new JakartaLogFactory();
            }

            // OUR IMPLEMNETATION OF .net LOGGING BYPASSES JAKARTA COMMONS LOGGING
            else if (logSystem.equals(DOTNET_LOGGER)) {
                m_logFactory = new SimpleLogFactory();
            }

            // by default, use jakarta logging ...
            else {
                m_logFactory = new JakartaLogFactory();
            }

        } catch (LogConfigurationException e) {
            //throw new ExceptionInInitializerError("An unexpected exception occurred while creating the logger factory:" + e);
            throw new RuntimeException("An unexpected exception occurred while creating the logger factory: " + e.getMessage());
        }
        m_isInitialized = true;
    }
}
