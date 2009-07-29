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

import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.internal.arjuna.objectstore.HashedStore;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

import java.io.File;

/**
 * A JavaBean containing configuration properties for the objectstore and various implementations thereof.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.arjuna.objectstore.")
public class ObjectStoreEnvironmentBean
{
    private String localOSRoot = "defaultStore";
    private String objectStoreDir = System.getProperty("user.dir") + File.separator + "ObjectStore";
    private boolean objectStoreSync = true;
    private String objectStoreType = ArjunaNames.Implementation_ObjectStore_defaultStore().stringForm();
    private int hashedDirectories = HashedStore.DEFAULT_NUMBER_DIRECTORIES;
    private boolean transactionSync = true;
    
    private String jdbcUserDbAccess = null;
    private String jdbcTxDbAccess = null;
    private int jdbcPoolSizeInitial = 1;
    private int jdbcPoolSizeMaximum = 1;
    private boolean jdbcPoolPutConnections = false;

    private int share = ObjectStore.OS_UNKNOWN;
    private int hierarchyRetry = 100;
    private int hierarchyTimeout = 100;

    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.size")
    private int cacheStoreSize = 10240;  // size in bytes
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.sync")
    private boolean cacheStoreSync = false;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.removedItems")
    private int cacheStoreRemovedItems = 256;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.scanPeriod")
    private int cacheStoreScanPeriod = 120000;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.workItems")
    private int cacheStoreWorkItems = 100;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.hash")
    private int cacheStoreHash = 128;

    @FullPropertyName(name = "com.arjuna.ats.arjuna.coordinator.transactionLog.synchronousRemoval")
    private boolean synchronousRemoval = true;
    @FullPropertyName(name = "com.arjuna.ats.arjuna.coordinator.transactionLog.size")
    private long txLogSize = 10 * 1024 * 1024;  // default maximum log txLogSize in bytes;
    @FullPropertyName(name = "com.arjuna.ats.arjuna.coordinator.transactionLog.purgeTime")
    private long purgeTime = 100000; // in milliseconds

    
//    public static final String CACHE_STORE_SIZE = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.size";
    public int getCacheStoreSize()
    {
        return cacheStoreSize;
    }

    public void setCacheStoreSize(int cacheStoreSize)
    {
        this.cacheStoreSize = cacheStoreSize;
    }

//    public static final String CACHE_STORE_SYNC = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.sync";
    public boolean isCacheStoreSync()
    {
        return cacheStoreSync;
    }

    public void setCacheStoreSync(boolean cacheStoreSync)
    {
        this.cacheStoreSync = cacheStoreSync;
    }

//    public static final String CACHE_STORE_REMOVED_ITEMS = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.removedItems";
    public int getCacheStoreRemovedItems()
    {
        return cacheStoreRemovedItems;
    }

    public void setCacheStoreRemovedItems(int cacheStoreRemovedItems)
    {
        this.cacheStoreRemovedItems = cacheStoreRemovedItems;
    }

//    public static final String CACHE_STORE_SCAN_PERIOD = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.scanPeriod";
    public int getCacheStoreScanPeriod()
    {
        return cacheStoreScanPeriod;
    }

    public void setCacheStoreScanPeriod(int cacheStoreScanPeriod)
    {
        this.cacheStoreScanPeriod = cacheStoreScanPeriod;
    }

//    public static final String CACHE_STORE_WORK_ITEMS = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.workItems";
    public int getCacheStoreWorkItems()
    {
        return cacheStoreWorkItems;
    }

    public void setCacheStoreWorkItems(int cacheStoreWorkItems)
    {
        this.cacheStoreWorkItems = cacheStoreWorkItems;
    }

//    public static final String CACHE_STORE_HASH = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.hash";
    public int getCacheStoreHash()
    {
        return cacheStoreHash;
    }

    public void setCacheStoreHash(int cacheStoreHash)
    {
        this.cacheStoreHash = cacheStoreHash;
    }
////////////////////////////////////

//    public static final String LOCALOSROOT = "com.arjuna.ats.arjuna.objectstore.localOSRoot";
    public String getLocalOSRoot()
    {
        return localOSRoot;
    }

    public void setLocalOSRoot(String localOSRoot)
    {
        this.localOSRoot = localOSRoot;
    }

//    public static final String OBJECTSTORE_DIR = "com.arjuna.ats.arjuna.objectstore.objectStoreDir";
    public String getObjectStoreDir()
    {
        return objectStoreDir;
    }

    public void setObjectStoreDir(String objectStoreDir)
    {
        this.objectStoreDir = objectStoreDir;
    }

//    public static final String OBJECTSTORE_SYNC = "com.arjuna.ats.arjuna.objectstore.objectStoreSync";
    public boolean isObjectStoreSync()
    {
        return objectStoreSync;
    }

    public void setObjectStoreSync(boolean objectStoreSync)
    {
        this.objectStoreSync = objectStoreSync;
    }

//    public static final String OBJECTSTORE_TYPE = "com.arjuna.ats.arjuna.objectstore.objectStoreType";
    public String getObjectStoreType()
    {
        return objectStoreType;
    }

    public void setObjectStoreType(String objectStoreType)
    {
        this.objectStoreType = objectStoreType;
    }

//    public static final String HASHED_DIRECTORIES = "com.arjuna.ats.arjuna.objectstore.hashedDirectories";
    public int getHashedDirectories()
    {
        return hashedDirectories;
    }

    public void setHashedDirectories(int hashedDirectories)
    {
        this.hashedDirectories = hashedDirectories;
    }

//    public static final String TRANSACTION_SYNC = "com.arjuna.ats.arjuna.objectstore.transactionSync";
    public boolean isTransactionSync()
    {
        return transactionSync;
    }

    public void setTransactionSync(boolean transactionSync)
    {
        this.transactionSync = transactionSync;
    }

//    public static final String JDBC_USER_DB_ACCESS = "com.arjuna.ats.arjuna.objectstore.jdbcUserDbAccess";
    public String getJdbcUserDbAccess()
    {
        return jdbcUserDbAccess;
    }

    public void setJdbcUserDbAccess(String jdbcUserDbAccess)
    {
        this.jdbcUserDbAccess = jdbcUserDbAccess;
    }

//    public static final String JDBC_TX_DB_ACCESS = "com.arjuna.ats.arjuna.objectstore.jdbcTxDbAccess";
    public String getJdbcTxDbAccess()
    {
        return jdbcTxDbAccess;
    }

    public void setJdbcTxDbAccess(String jdbcTxDbAccess)
    {
        this.jdbcTxDbAccess = jdbcTxDbAccess;
    }

//    public static final String JDBC_POOL_SIZE_INIT = "com.arjuna.ats.arjuna.objectstore.jdbcPoolSizeInitial";
    public int getJdbcPoolSizeInitial()
    {
        return jdbcPoolSizeInitial;
    }

    public void setJdbcPoolSizeInitial(int jdbcPoolSizeInitial)
    {
        this.jdbcPoolSizeInitial = jdbcPoolSizeInitial;
    }

//    public static final String JDBC_POOL_SIZE_MAX = "com.arjuna.ats.arjuna.objectstore.jdbcPoolSizeMaximum";
    public int getJdbcPoolSizeMaximum()
    {
        return jdbcPoolSizeMaximum;
    }

    public void setJdbcPoolSizeMaximum(int jdbcPoolSizeMaximum)
    {
        this.jdbcPoolSizeMaximum = jdbcPoolSizeMaximum;
    }

//    public static final String JDBC_POOL_PUT = "com.arjuna.ats.arjuna.objectstore.jdbcPoolPutConnections";
    public boolean isJdbcPoolPutConnections()
    {
        return jdbcPoolPutConnections;
    }

    public void setJdbcPoolPutConnections(boolean jdbcPoolPutConnections)
    {
        this.jdbcPoolPutConnections = jdbcPoolPutConnections;
    }

//    public static final String OBJECTSTORE_SHARE = "com.arjuna.ats.arjuna.objectstore.share";
    public int getShare()
    {
        return share;
    }

    public void setShare(int share)
    {
        this.share = share;
    }

//    public static final String OBJECTSTORE_HIERARCHY_RETRY = "com.arjuna.ats.arjuna.objectstore.hierarchyRetry";
    public int getHierarchyRetry()
    {
        return hierarchyRetry;
    }

    public void setHierarchyRetry(int hierarchyRetry)
    {
        this.hierarchyRetry = hierarchyRetry;
    }

//    public static final String OBJECTSTORE_HIERARCHY_TIMEOUT = "com.arjuna.ats.arjuna.objectstore.hierarchyTimeout";
    public int getHierarchyTimeout()
    {
        return hierarchyTimeout;
    }

    public void setHierarchyTimeout(int hierarchyTimeout)
    {
        this.hierarchyTimeout = hierarchyTimeout;
    }

    //    public static final String TRANSACTION_LOG_SYNC_REMOVAL = "com.arjuna.ats.arjuna.coordinator.transactionLog.synchronousRemoval";
    public boolean isSynchronousRemoval()
    {
        return synchronousRemoval;
    }

    public void setSynchronousRemoval(boolean synchronousRemoval)
    {
        this.synchronousRemoval = synchronousRemoval;
    }

//    public static final String TRANSACTION_LOG_SIZE = "com.arjuna.ats.arjuna.coordinator.transactionLog.txLogSize";
    public long getTxLogSize()
    {
        return txLogSize;
    }

    public void setTxLogSize(long txLogSize)
    {
        this.txLogSize = txLogSize;
    }

//    public static final String TRANSACTION_LOG_PURGE_TIME = "com.arjuna.ats.arjuna.coordinator.transactionLog.purgeTime";
    public long getPurgeTime()
    {
        return purgeTime;
    }

    public void setPurgeTime(long purgeTime)
    {
        this.purgeTime = purgeTime;
    }
}
