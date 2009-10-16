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

    @FullPropertyName(name = "com.arjuna.common.util.logging.default")
    private volatile boolean useDefaultLog = false;

    @FullPropertyName(name = "com.arjuna.common.util.logger")
    private volatile String loggingSystem = "log4j";

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
     * Should the internal default log system be used in addition to the underlying logger.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.common.util.logging.default
     *
     * @return true if the default log should be enabled, otherwise false.
     */
    public boolean isUseDefaultLog()
    {
        return useDefaultLog;
    }

    /**
     * Enables the default log system.
     *
     * @param useDefaultLog true if default log should be enabled, false otherwise.
     */
    public void setUseDefaultLog(boolean useDefaultLog)
    {
        this.useDefaultLog = useDefaultLog;
    }

    /**
     * Returns the identifier for the underlying logging system.
     *
     * Default: "log4j"
     * Equivalent deprecated property: com.arjuna.common.util.logger
     *
     * @return the identifier of the underlying logging system.
     */
    public String getLoggingSystem()
    {
        return loggingSystem;
    }

    /**
     * Sets the identifier for the underlying logging system.
     *
     * @param loggingSystem the identifier for the underlying logging system.
     */
    public void setLoggingSystem(String loggingSystem)
    {
        this.loggingSystem = loggingSystem;
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
