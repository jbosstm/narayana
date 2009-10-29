/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.common.internal.util.logging;

import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;

/**
 * A JavaBean containing configuration properties for the logging abstraction system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.common.util.logging.")
public class LoggingEnvironmentBean implements LoggingEnvironmentBeanMBean
{
    // don't mess with language and country defaults unless
    // you also change the way logging bundles are built.
    private volatile String language = "en";
    private volatile String country = "US";

    private volatile String loggingFactory = "com.arjuna.common.internal.util.logging.jakarta.JakartaLogFactory;com.arjuna.common.internal.util.logging.jakarta.Log4JLogger";

    @FullPropertyName(name = "com.arjuna.common.util.logging.DebugLevel")
    private volatile String debugLevel = "0x00000000";

    @FullPropertyName(name = "com.arjuna.common.util.logging.FacilityLevel")
    private volatile String facilityLevel = "0xffffffff";

    @FullPropertyName(name = "com.arjuna.common.util.logging.VisibilityLevel")
    private volatile String visibilityLevel = "0xffffffff";


    /**
     * Returns the language code. ISO language code format as used with Locale objects.
     * 
     * Default: "en"
     * Equivalent deprecated property: com.arjuna.common.util.logging.language
     *
     * @return the ISO language code.
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Sets the language code. Use ISO language code format as with Locale objects.
     *
     * @param language the ISO language code.
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * Returns the country code. ISO country code format as used with Locale objects.
     *
     * Default: "US"
     * Equivalent deprecated property: com.arjuna.common.util.logging.country
     *
     * @return the ISO country code.
     */
    public String getCountry()
    {
        return country;
    }

    /**
     * Sets the country code. Use ISO country code format as with Locale objects.
     *
     * @param country the ISO country code.
     */
    public void setCountry(String country)
    {
        this.country = country;
    }

    /**
     * Returns the factory information for the underlying logging system.
     * The string should consist of a classname for a class which implements LogFactoryInterface,
     * plus an optional semicolon separated suffix containing a parameter string to pass to the factory impl.
     *
     * Options corresponding to the old log configuration mechanism are as follows:
     *
     * log4j: com.arjuna.common.internal.util.logging.jakarta.JakartaLogFactory;com.arjuna.common.internal.util.logging.jakarta.Log4JLogger
     * jdk14: com.arjuna.common.internal.util.logging.jakarta.JakartaLogFactory;org.apache.commons.logging.impl.Jdk14Logger
     * simple: com.arjuna.common.internal.util.logging.jakarta.JakartaLogFactory;org.apache.commons.logging.impl.SimpleLog
     * noop: com.arjuna.common.internal.util.logging.jakarta.JakartaLogFactory;org.apache.commons.logging.impl.NoOpLog
     * log4j_releveler: com.arjuna.common.internal.util.logging.jakarta.JakartaRelevelingLogFactory;com.arjuna.common.internal.util.logging.jakarta.Log4JLogger
     * jakarta: com.arjuna.common.internal.util.logging.jakarta.JakartaLogFactory
     *
     * Note that the above are run through commons logging, so that jar is required on the classpath in most cases.
     *
     * built-in (no 3rd party deps): "com.arjuna.common.internal.util.logging.basic.BasicLogFactory"
     *
     * Default: log4j via. commons logging (the first option in the list above)
     *
     * @return the factory information for the underlying logging system.
     */
    public String getLoggingFactory()
    {
        return loggingFactory;
    }

    /**
     * Sets the factory information for the underlying logging system.
     *
     * @param loggingFactory the factory information for the underlying logging system.
     */
    public void setLoggingFactory(String loggingFactory)
    {
        this.loggingFactory = loggingFactory;
    }

    /**
     * Returns the debug log level in hex form.
     *
     * Default: "0x00000000"
     * Equivalent deprecated property: com.arjuna.common.util.logging.DebugLevel
     *
     * @return the debug log level.
     */
    public String getDebugLevel()
    {
        return debugLevel;
    }

    /**
     * Sets the debug log level.
     *
     * @param debugLevel the debug log level, expressed as a hex string.
     */
    public void setDebugLevel(String debugLevel)
    {
        this.debugLevel = debugLevel;
    }

    /**
     * Returns the logging facility level in hex form.
     *
     * Default: "0xffffffff"
     * Equivalent deprecated property: com.arjuna.common.util.logging.FacilityLevel
     *
     * @return the facility log level.
     */
    public String getFacilityLevel()
    {
        return facilityLevel;
    }

    /**
     * Sets the facility log level.
     *
     * @param facilityLevel the facility log level, expressed as a hex string.
     */
    public void setFacilityLevel(String facilityLevel)
    {
        this.facilityLevel = facilityLevel;
    }

    /**
     * Returns the visibility log level in hex form.
     *
     * Default: "0xffffffff"
     * Equivalent deprecated property: com.arjuna.common.util.logging.VisibilityLevel
     *
     * @return the visibility log level.
     */
    public String getVisibilityLevel()
    {
        return visibilityLevel;
    }

    /**
     * Sets the visibility log level.
     *
     * @param visibilityLevel the visibility log level, expressed as a hex string.
     */
    public void setVisibilityLevel(String visibilityLevel)
    {
        this.visibilityLevel = visibilityLevel;
    }
}
