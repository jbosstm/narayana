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
 * A JavaBean containing configuration properties for the logging system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.common.util.logging.")
public class LoggingEnvironmentBean
{
    // don't mess with language and country defaults unless
    // you also change the way logging bundles are built.
    private String language = "en";
    private String country = "US";

    @FullPropertyName(name = "com.arjuna.common.util.logging.default")
    private boolean useDefaultLog = false;

    @FullPropertyName(name = "com.arjuna.common.util.logger")
    private String loggingSystem = "log4j";

    @FullPropertyName(name = "com.arjuna.common.util.logging.DebugLevel")
    private String debugLevel = "0xffffffff";

    @FullPropertyName(name = "com.arjuna.common.util.logging.FacilityLevel")
    private String facilityLevel = "0xffffffff";

    @FullPropertyName(name = "com.arjuna.common.util.logging.VisibilityLevel")
    private String visibilityLevel = "0xffffffff";


    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public boolean isUseDefaultLog()
    {
        return useDefaultLog;
    }

    public void setUseDefaultLog(boolean useDefaultLog)
    {
        this.useDefaultLog = useDefaultLog;
    }

    public String getLoggingSystem()
    {
        return loggingSystem;
    }

    public void setLoggingSystem(String loggingSystem)
    {
        this.loggingSystem = loggingSystem;
    }

    public String getDebugLevel()
    {
        return debugLevel;
    }

    public void setDebugLevel(String debugLevel)
    {
        this.debugLevel = debugLevel;
    }

    public String getFacilityLevel()
    {
        return facilityLevel;
    }

    public void setFacilityLevel(String facilityLevel)
    {
        this.facilityLevel = facilityLevel;
    }

    public String getVisibilityLevel()
    {
        return visibilityLevel;
    }

    public void setVisibilityLevel(String visibilityLevel)
    {
        this.visibilityLevel = visibilityLevel;
    }
}
