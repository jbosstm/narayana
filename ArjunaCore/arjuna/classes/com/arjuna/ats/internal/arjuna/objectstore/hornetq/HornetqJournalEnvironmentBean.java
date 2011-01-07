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
package com.arjuna.ats.internal.arjuna.objectstore.hornetq;

import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

import java.io.File;

/**
 * A JavaBean containing assorted configuration properties for the HornetQ Journal based transaction logging system.
 *
 * Parameters on this file serve a similar role to their counterparts in HornetQ.
 * The HornetQ docs therefore provide relevant information on usage:
 * http://hornetq.sourceforge.net/docs/hornetq-2.1.1.Final/user-manual/en/html/persistence.html#configuring.message.journal.journal-type
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-03
 */
@PropertyPrefix(prefix = "com.arjuna.ats.arjuna.hornetqjournal.")
public class HornetqJournalEnvironmentBean implements HornetqJournalEnvironmentBeanMBean
{
    private volatile int fileSize = 1024*1024*2;

    private volatile int minFiles = 4;

    private volatile int compactMinFiles = 10;

    private volatile int compactPercentage = 30;

    private volatile String filePrefix = "jbossts";

    private volatile String fileExtension = "txlog";

    private volatile int maxIO = 1;

    private volatile String storeDir = System.getProperty("user.dir") + File.separator + "HornetqJournalStore";

    private volatile boolean syncWrites = true;

    private volatile boolean syncDeletes = true;


    /**
     * Returns the desired size in bytes of each log file.
     * Minimum 1024.
     *
     * Default: 2MB (2097152 bytes)
     *
     * @return The individual log file size, in bytes.
     */
    public int getFileSize()
    {
        return fileSize;
    }

    /**
     * Sets the desired size in bytes for each log file.
     * 
     * @param fileSize the individual log file size, in bytes.
     */
    public void setFileSize(int fileSize)
    {
        this.fileSize = fileSize;
    }

    /**
     * Returns the minimum number of log files to use.
     * Minimum 2.
     *
     * Default: 4
     *
     * @return the minimum number of individual log files.
     */
    public int getMinFiles()
    {
        return minFiles;
    }

    /**
     * Sets the minimum number of log files to use.
     *
     * @param minFiles the minimum number of individual log files.
     */
    public void setMinFiles(int minFiles)
    {
        this.minFiles = minFiles;
    }

    /**
     * Gets the minimal number of files before we can consider compacting.
     *
     * Default: 10
     *
     * @return the threshold file count.
     */
    public int getCompactMinFiles()
    {
        return compactMinFiles;
    }

    /**
     * Sets the minimal number of files before we can consider compacting.
     *
     * @param compactMinFiles the threshold file count.
     */
    public void setCompactMinFiles(int compactMinFiles)
    {
        this.compactMinFiles = compactMinFiles;
    }

    /**
     * Gets the percentage minimum capacity usage at which to start compacting.
     *
     * Default: 30
     *
     * @return the threshold percentage.
     */
    public int getCompactPercentage()
    {
        return compactPercentage;
    }

    /**
     * Sets the percentage minimum capacity usage at which to start compacting.
     *
     * @param compactPercentage the threshold percentage.
     */
    public void setCompactPercentage(int compactPercentage)
    {
        this.compactPercentage = compactPercentage;
    }

    /**
     * Returns the prefix to be used when naming each log file.
     *
     * Default: "jbossts"
     *
     * @return the prefix used to construct individual log file names.
     */
    public String getFilePrefix()
    {
        return filePrefix;
    }

    /**
     * Sets the prefix to be used when naming each log file.
     *
     * @param filePrefix the prefix used to construct individual log file names.
     */
    public void setFilePrefix(String filePrefix)
    {
        this.filePrefix = filePrefix;
    }

    /**
     * Returns the suffix to be used then naming each log file.
     *
     * Default: "txlog"
     *
     * @return the suffix used to construct individual log file names.
     */
    public String getFileExtension()
    {
        return fileExtension;
    }

    /**
     * Sets the suffix to be used when naming each log file.
     *
     * @param fileExtension the suffix used to construct individual log file names.
     */
    public void setFileExtension(String fileExtension)
    {
        this.fileExtension = fileExtension;
    }

    /**
     * Gets the maximum write requests queue depth.
     * Minimum 1. Use 1 for NIO. For AIO, recommended 500.
     *
     * Default: 1
     *
     * @return the max number of outstanding requests.
     */
    public int getMaxIO()
    {
        return maxIO;
    }

    /**
     * Sets the maximum write requests queue depth.
     *
     * @param maxIO the max number of outstanding requests.
     */
    public void setMaxIO(int maxIO)
    {
        this.maxIO = maxIO;
    }

    /**
     * Returns the log directory path
     *
     * Default: {user.dir}/HornetqJournalStore
     *
     * @return the log directory name
     */
    public String getStoreDir()
    {
        return storeDir;
    }

    /**
     * Sets the log directory path.
     *
     * @param storeDir the path to the log directory.
     */
    public void setStoreDir(String storeDir)
    {
        this.storeDir = storeDir;
    }

    /**
     * Returns the sync setting for transaction log write operations.
     * To preserve ACID properties this value must be set to true, in which case
     * log write operations block until data is forced to the physical storage device.
     * Turn sync off only if you don't care about data integrity.
     *
     * Default: true.
     *
     * @return true if log writes should be synchronous, false otherwise.
     */
    public boolean isSyncWrites()
    {
        return syncWrites;
    }

    /**
     * Sets if log write operations should be synchronous or not.
     *
     * @param syncWrites true for synchronous operation, false otherwise.
     */
    public void setSyncWrites(boolean syncWrites)
    {
        this.syncWrites = syncWrites;
    }

    /**
     * Returns the sync setting for transaction log delete operations.
     * For optimal crash recovery this value should be set to true.
     * Asynchronous deletes may give rise to unnecessary crash recovery complications.
     *
     * Default: true.
     * 
     * @return true if log deletes should be synchronous, false otherwise.
     */
    public boolean isSyncDeletes()
    {
        return syncDeletes;
    }

    /**
     * Sets if log delete operations should be synchronous or not.
     *
     * @param syncDeletes true for synchronous operation, false otherwise.
     */
    public void setSyncDeletes(boolean syncDeletes)
    {
        this.syncDeletes = syncDeletes;
    }
}
