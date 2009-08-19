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

/**
 * A JavaBean containing configuration properties for the default log system.
 *
 * TODO testcase
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.common.util.logging.default.")
public class DefaultLogEnvironmentBean
{
    private boolean showLogName = false;
    private boolean showShortLogName = true;
    private boolean showDate = true;
    private boolean logFileAppend = true;

    private String defaultLevel = "info";

    private String logFile = "error.log";


    public boolean isShowLogName()
    {
        return showLogName;
    }

    public void setShowLogName(boolean showLogName)
    {
        this.showLogName = showLogName;
    }

    public boolean isShowShortLogName()
    {
        return showShortLogName;
    }

    public void setShowShortLogName(boolean showShortLogName)
    {
        this.showShortLogName = showShortLogName;
    }

    public boolean isShowDate()
    {
        return showDate;
    }

    public void setShowDate(boolean showDate)
    {
        this.showDate = showDate;
    }

    public boolean isLogFileAppend()
    {
        return logFileAppend;
    }

    public void setLogFileAppend(boolean logFileAppend)
    {
        this.logFileAppend = logFileAppend;
    }

    public String getDefaultLevel()
    {
        return defaultLevel;
    }

    public void setDefaultLevel(String defaultLevel)
    {
        this.defaultLevel = defaultLevel;
    }

    public String getLogFile()
    {
        return logFile;
    }

    public void setLogFile(String logFile)
    {
        this.logFile = logFile;
    }
}
