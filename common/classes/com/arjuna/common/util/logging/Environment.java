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
package com.arjuna.common.util.logging;

import com.arjuna.common.internal.util.logging.commonPropertyManager;

//import com.hp.mwlabs.common.util.logging.commonPropertyManager;

/**
 * This class defines the property used by the LogManager to determine the LoggerFactory implementation
 * class that should be loaded. 
 * The name of the property is <pre>com.arjuna.common.utils.logging.factory</pre> while its possible values,
 * defined by this distribution are:
 * <ul>
 * <li><pre>com.hp.mwlabs.common.util.logging.simpleLog.ArjSimpleLoggerFactory</pre>. This is the default loaded.
 * <li><pre>com.hp.mwlabs.common.util.logging.csf.ArjChannelFactory</pre>
 * <li><pre>com.hp.mwlabs.common.util.logging.log4j.ArjLoggerFactory</pre>
 * </ul>
 *
 * @deprecated since clf-2.0 this class was only used to obtain information about the underlying logger. Such
 *    an interface is not provided by Jakarta commons logging (upon which clf-2.0 builds).
 */

public class Environment {

    /**
     * @deprecated since clf-2.0
     */
    public static final String LOGGER_FACTORY_PROPERTY = "com.hp.mwlabs.common.utils.logging.factory";

    /**
     * @deprecated since clf-2.0
     */
    public static final String LOGGER_FACTORY_DEFAULT = "com.hp.mwlabs.common.util.logging.log4j.ArjLoggerFactory";

    /**
     * @deprecated since clf-2.0
     */
    public static final String LOGGER_FACTORY_CSF = "com.hp.mwlabs.common.util.logging.csf.ArjChannelFactory";

    /**
     * @deprecated since clf-2.0
     */
    public static final String LOGGER_FACTORY_LOG4J = "com.hp.mwlabs.common.util.logging.log4j.ArjLoggerFactory";

    /**
     * @deprecated since clf-2.0
     */
    public static final String LOGGER_FACTORY_SIMPLE = "com.hp.mwlabs.common.util.logging.simpleLog.ArjSimpleLoggerFactory";

    public final static String LOGGING_PROPERTIES_NAMESPACE = "com.arjuna.logging";

    public static final String LOGGING_LANGUAGE_PROPERTY = LOGGING_PROPERTIES_NAMESPACE + ".language";
    public static final String LOGGING_LANGUAGE_DEFAULT = "en";

    public static final String LOGGING_COUNTRY_PROPERTY = LOGGING_PROPERTIES_NAMESPACE + ".country";
    public static final String LOGGING_COUNTRY_DEFAULT = "US";

    public static final String LOGGER_OUTPUT_PROPERTY = LOGGING_PROPERTIES_NAMESPACE + ".output";
    public static final String LOGGER_OUTPUT_DEFAULT = "console";

    public static final String LOG_DIRECTORY_PROPERTY = LOGGING_PROPERTIES_NAMESPACE + ".log.dir";
    public static final String LOG_DIRECTORY_DEFAULT = ".";

    public static final String LOG_FILE_PROPERTY = LOGGING_PROPERTIES_NAMESPACE + ".log.file";
    public static final String LOG_FILE_DEFAULT = "loggingFile";

    public static final String LOG_DISABLED_PROPERTY = LOGGING_PROPERTIES_NAMESPACE + ".log.disabled";
    public static final String LOG_DISABLED_DEFAULT = "NO";

    public static final String FINER_DEBUG_SUFFIX = ".FinerDebug";
    public static final String FINER_DEBUG_DEFAULT = "NO";

    public static final String DEBUG_CLASS_SUFFIX = ".dClass";
    public static final String DEBUG_CLASS_DEFAULT = "com.hp.mw.common.util.logging.DebugLevel";

    public static final String FACILITY_CLASS_SUFFIX = ".fClass";
    public static final String FACILITY_CLASS_DEFAULT = "com.hp.mw.common.util.logging.FacilityCode";

    public static final String VISIBILITY_CLASS_SUFFIX = ".vClass";
    public static final String VISIBILITY_CLASS_DEFAULT = "com.hp.mw.common.util.logging.VisibilityLevel";

    public static final String DEBUG_VALUE_SUFFIX = ".debugValue";
    public static final String FACILITY_VALUE_SUFFIX = ".facilValue";
    public static final String VISIBILITY_VALUE_SUFFIX = ".visibValue";

    public static final String LOGGER_LEVEL_PROPERTY = LOGGING_PROPERTIES_NAMESPACE + ".log.level";
    public static final String LOGGER_LEVEL_DEFAULT = "DEBUG";

    public static final String YES_VALUE = "YES";

    public final static String getLogLevel() {
        return commonPropertyManager.propertyManager.getProperty(LOGGER_LEVEL_PROPERTY, LOGGER_LEVEL_DEFAULT);
    }

    public final static String getDebugValue(String key) {
        return commonPropertyManager.propertyManager.getProperty(key + DEBUG_VALUE_SUFFIX);
    }

    public final static String getFacilityValue(String key) {
        return commonPropertyManager.propertyManager.getProperty(key + FACILITY_VALUE_SUFFIX);
    }

    public final static String getVisibilityValue(String key) {
        return commonPropertyManager.propertyManager.getProperty(key + VISIBILITY_VALUE_SUFFIX);
    }

    public final static String getDebugClass(String key) {
        return commonPropertyManager.propertyManager.getProperty(key + DEBUG_CLASS_SUFFIX, DEBUG_CLASS_DEFAULT);
    }

    public final static String getFacilityClass(String key) {
        return commonPropertyManager.propertyManager.getProperty(key + FACILITY_CLASS_SUFFIX, FACILITY_CLASS_DEFAULT);
    }

    public final static String getVisibilityClass(String key) {
        return commonPropertyManager.propertyManager.getProperty(key + VISIBILITY_CLASS_SUFFIX, VISIBILITY_CLASS_DEFAULT);
    }

    public final static boolean isFinerDebug(String key) {
        return getYesNoProperty(commonPropertyManager.propertyManager.getProperty(key + FINER_DEBUG_SUFFIX, FINER_DEBUG_DEFAULT));
    }

    public final static boolean isLogDisabled() {
        return getYesNoProperty(commonPropertyManager.propertyManager.getProperty(LOG_DISABLED_PROPERTY, LOG_DISABLED_DEFAULT));
    }

    /**
     * @deprecated since clf-2.0
     */
    public final static String getLoggerOutput() {
        return commonPropertyManager.propertyManager.getProperty(LOGGER_OUTPUT_PROPERTY, LOGGER_OUTPUT_DEFAULT);
    }

    /**
     * @deprecated since clf-2.0
     */
    public final static String getLoggingDirectory() {
        return commonPropertyManager.propertyManager.getProperty(LOG_DIRECTORY_PROPERTY, LOG_DIRECTORY_DEFAULT);
    }

    /**
     * @deprecated since clf-2.0
     */
    public final static String getLoggingFile() {
        return commonPropertyManager.propertyManager.getProperty(LOG_FILE_PROPERTY, LOG_FILE_DEFAULT);
    }

    public final static boolean getYesNoProperty(String property) {
        return property.equalsIgnoreCase(YES_VALUE);
    }

    /**
     * @deprecated since clf-2.0
     */
    public final static String getLoggingLanguage() {
        return commonPropertyManager.propertyManager.getProperty(LOGGING_LANGUAGE_PROPERTY, LOGGING_LANGUAGE_DEFAULT);
    }

    /**
     * @deprecated since clf-2.0
     */
    public final static String getLoggingCountry() {
        return commonPropertyManager.propertyManager.getProperty(LOGGING_COUNTRY_PROPERTY, LOGGING_COUNTRY_DEFAULT);
    }

}
