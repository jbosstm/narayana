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
package com.arjuna.ats.arjuna.common;

/**
 * A JMX MBean interface containing configuration for the objectstore and various implementations thereof.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface ObjectStoreEnvironmentBeanMBean
{
    int getCacheStoreSize();

    void setCacheStoreSize(int cacheStoreSize);

    boolean isCacheStoreSync();

    void setCacheStoreSync(boolean cacheStoreSync);

    int getCacheStoreRemovedItems();

    void setCacheStoreRemovedItems(int cacheStoreRemovedItems);

    int getCacheStoreScanPeriod();

    void setCacheStoreScanPeriod(int cacheStoreScanPeriod);

    int getCacheStoreWorkItems();

    void setCacheStoreWorkItems(int cacheStoreWorkItems);

    int getCacheStoreHash();

    void setCacheStoreHash(int cacheStoreHash);

    String getLocalOSRoot();

    void setLocalOSRoot(String localOSRoot);

    String getObjectStoreDir();

    void setObjectStoreDir(String objectStoreDir);

    boolean isObjectStoreSync();

    void setObjectStoreSync(boolean objectStoreSync);

    String getObjectStoreType();

    void setObjectStoreType(String objectStoreType);

    int getHashedDirectories();

    void setHashedDirectories(int hashedDirectories);

    boolean isTransactionSync();

    void setTransactionSync(boolean transactionSync);

    String getJdbcUserDbAccess();

    void setJdbcUserDbAccess(String jdbcUserDbAccess);

    String getJdbcTxDbAccess();

    void setJdbcTxDbAccess(String jdbcTxDbAccess);

    int getJdbcPoolSizeInitial();

    void setJdbcPoolSizeInitial(int jdbcPoolSizeInitial);

    int getJdbcPoolSizeMaximum();

    void setJdbcPoolSizeMaximum(int jdbcPoolSizeMaximum);

    boolean isJdbcPoolPutConnections();

    void setJdbcPoolPutConnections(boolean jdbcPoolPutConnections);

    int getShare();

    void setShare(int share);

    int getHierarchyRetry();

    void setHierarchyRetry(int hierarchyRetry);

    int getHierarchyTimeout();

    void setHierarchyTimeout(int hierarchyTimeout);

    boolean isSynchronousRemoval();

    void setSynchronousRemoval(boolean synchronousRemoval);

    long getTxLogSize();

    void setTxLogSize(long txLogSize);

    long getPurgeTime();

    void setPurgeTime(long purgeTime);

    boolean isJmxEnabled();

    void setJmxEnabled(boolean enable);

}
