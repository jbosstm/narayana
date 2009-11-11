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
package com.arjuna.common.internal.util.logging.basic;

import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;

/**
 * A JavaBean containing configuration properties for the built-in log system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.common.util.logging.default.")
public class BasicLogEnvironmentBean
{
    private volatile boolean showLogName = false;
    private volatile boolean showShortLogName = true;
    private volatile boolean showDate = true;
    private volatile boolean logFileAppend = true;

    @FullPropertyName(name = "com.arjuna.common.util.logging.default.defaultLevel")
    private volatile String level = "info";

    private volatile String logFile = "error.log";


    /**
     * Returns true if the log output should contain the log name element.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.common.util.logging.default.showLogName
     *
     * @return true if the log name should appear in the log output, false otherwise.
     */
    public boolean isShowLogName()
    {
        return showLogName;
    }

    /**
     * Enables the use of the log name element in log output.
     *
     * @param showLogName true if the log name should appear in the log output, false otherwise.
     */
    public void setShowLogName(boolean showLogName)
    {
        this.showLogName = showLogName;
    }

    /**
     * Returns true if the log output should contain the short form log name element.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.common.util.logging.default.showShortLogName
     *
     * @return true if the short log name should appear in the log output, false otherwise.
     */
    public boolean isShowShortLogName()
    {
        return showShortLogName;
    }

    /**
     * Enables use of the short log name element in log output.
     *
     * @param showShortLogName true if the log name should appear in the log output, false otherwise.
     */
    public void setShowShortLogName(boolean showShortLogName)
    {
        this.showShortLogName = showShortLogName;
    }

    /**
     * Returns true if the log output should contain the date/time element.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.common.util.logging.default.showDate
     *
     * @return true if the date/time should appear in the log output, false otherwise.
     */
    public boolean isShowDate()
    {
        return showDate;
    }

    /**
     * Enables use of the date/time element in log output.
     *
     * @param showDate true if the date/time should appear in log output, false otherwise.
     */
    public void setShowDate(boolean showDate)
    {
        this.showDate = showDate;
    }

    /**
     * Returns true if the log file should be appended to.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.common.util.logging.default.logFileAppend
     *
     * @return true if append behaviour is required, false for overwrite behaviour.
     */
    public boolean isLogFileAppend()
    {
        return logFileAppend;
    }

    /**
     * Enables appending to an existing log file rather than overwiting it.
     *
     * @param logFileAppend true for appending, false otherwise.
     */
    public void setLogFileAppend(boolean logFileAppend)
    {
        this.logFileAppend = logFileAppend;
    }

    /**
     * Returns the threshold level for logging.
     *
     * Default: "info"
     * Equivalent deprecated property: com.arjuna.common.util.logging.default.defaultLevel
     *
     * @return the default threshold logging level.
     */
    public String getLevel()
    {
        return level;
    }

    /**
     * Sets the default log threshold.
     * Valid values (in order) are: all, trace, debug, info, warn, error, fatal, off
     *
     * @param level the default log threshold.
     */
    public void setLevel(String level)
    {
        this.level = level;
    }

    /**
     * Returns the name of the log file to use.
     *
     * Default: "error.log"
     * Equivalent deprecated property: com.arjuna.common.util.logging.default.logFile
     *
     * @return the name of the file to which the log statements will be written.
     */
    public String getLogFile()
    {
        return logFile;
    }

    /**
     * Sets the name of the log file to write output to.
     *
     * @param logFile the filename, relative or absolute.
     */
    public void setLogFile(String logFile)
    {
        this.logFile = logFile;
    }
}
