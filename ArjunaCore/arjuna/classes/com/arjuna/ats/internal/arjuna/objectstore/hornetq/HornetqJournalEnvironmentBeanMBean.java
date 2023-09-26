/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.hornetq;

/**
 * A JMX MBean interface containing configuration for the HornetQ Journal based transaction logging system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface HornetqJournalEnvironmentBeanMBean
{
    public int getFileSize();

    public int getMinFiles();

    public int getPoolSize();

    public int getCompactMinFiles();

    public int getCompactPercentage();

    public String getFilePrefix();

    public String getFileExtension();

    public int getMaxIO();

    public String getStoreDir();

    public boolean isSyncWrites();

    public boolean isSyncDeletes();

    public int getBufferFlushesPerSecond();

    public int getBufferSize();

    public boolean isLogRates();

    public boolean isAsyncIO();
}