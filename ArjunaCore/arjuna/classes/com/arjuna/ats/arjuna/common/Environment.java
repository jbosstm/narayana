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
/*
 * Copyright (C) 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Environment.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.common;

/**
 * The various property variables that can be set at
 * runtime to configure the some of the classes within
 * the package.
 *
 * The various values are:
 * <ul>
 * <li> STATIC_INVENTORY_IMPLE = com.arjuna.ats.internal.arjuna.inventory.staticInventoryImple
 * <li> VAR_DIR = com.arjuna.ats.arjuna.common.varDir
 * <li> ACTION_STORE= com.arjuna.ats.arjuna.coordinator.actionStore
 * <li> ASYNC_COMMIT = com.arjuna.ats.arjuna.coordinator.asyncCommit
 * <li> ASYNC_PREPARE = com.arjuna.ats.arjuna.coordinator.asyncPrepare
 * <li> ASYNC_ROLLBACK = com.arjuna.ats.arjuna.coordinator.asyncRollback
 * <li> COMMIT_ONE_PHASE = com.arjuna.ats.arjuna.coordinator.commitOnePhase
 * <li> LOCALOSROOT = com.arjuna.ats.arjuna.objectstore.localOSRoot
 * <li> MAINTAIN_HEURISTICS = com.arjuna.ats.arjuna.coordinator.maintainHeuristics
 * <li> OBJECTSTORE_DIR = com.arjuna.ats.arjuna.objectstore.objectStoreDir
 * <li> OBJECTSTORE_SYNC = com.arjuna.ats.arjuna.objectstore.objectStoreSync
 * <li> OBJECTSTORE_TYPE = com.arjuna.ats.arjuna.objectstore.objectStoreType
 * <li> HASHED_DIRECTORIES = com.arjuna.ats.arjuna.objectstore.hashedDirectories
 * <li> TRANSACTION_LOG = com.arjuna.ats.arjuna.coordinator.transactionLog
 * <li> TRANSACTION_SYNC = com.arjuna.ats.arjuna.objectstore.transactionSync
 * <li> READONLY_OPTIMISATION = com.arjuna.ats.arjuna.coordinator.readonlyOptimisation
 * <li> CLASSIC_PREPARE = com.arjuna.ats.arjuna.coordinator.classicPrepare
 * <li> ENABLE_STATISTICS = com.arjuna.ats.arjuna.coordinator.enableStatistics
 * <li> SHARED_TRANSACTION_LOG = com.arjuna.ats.arjuna.coordinator.sharedTransactionLog
 * <li> START_DISABLED = com.arjuna.ats.arjuna.coordinator.startDisabled
 * <li> JDBC_USER_DB_ACCESS = com.arjuna.ats.arjuna.objectstore.jdbcUserDbAccess
 * <li> JDBC_TX_DB_ACCESS = com.arjuna.ats.arjuna.objectstore.jdbcTxDbAccess
 * <li> JDBC_POOL_SIZE_INIT = com.arjuna.ats.arjuna.objectstore.jdbcPoolSizeInitial
 * <li> JDBC_POOL_SIZE_MAX = com.arjuna.ats.arjuna.objectstore.jdbcPoolSizeMaximum
 * <li> JDBC_POOL_PUT = com.arjuna.ats.arjuna.objectstore.jdbcPoolPutConnections
 * <li> LICENCE = com.arjuna.ats.arjuna.licence
 * <li> CACHE_STORE_SIZE = com.arjuna.ats.internal.arjuna.objectstore.cacheStore.size
 * <li> CACHE_STORE_SYNC = com.arjuna.ats.internal.arjuna.objectstore.cacheStore.sync
 * <li> CACHE_STORE_REMOVED_ITEMS = com.arjuna.ats.internal.arjuna.objectstore.cacheStore.removedItems
 * <li> CACHE_STORE_SCAN_PERIOD = com.arjuna.ats.internal.arjuna.objectstore.cacheStore.scanPeriod
 * <li> CACHE_STORE_WORK_ITEMS = com.arjuna.ats.internal.arjuna.objectstore.cacheStore.workItems
 * <li> CACHE_STORE_HASH = com.arjuna.ats.internal.arjuna.objectstore.cacheStore.hash
 * <li> PERIODIC_RECOVERY_PERIOD = com.arjuna.ats.arjuna.recovery.periodicRecoveryPeriod
 * <li> RECOVERY_BACKOFF_PERIOD = com.arjuna.ats.arjuna.recovery.recoveryBackoffPeriod
 * <li> TX_REAPER_MODE = com.arjuna.ats.arjuna.coordinator.txReaperMode
 * <li> TX_REAPER_TIMEOUT = com.arjuna.ats.arjuna.coordinator.txReaperTimeout
 * <li> OBJECTSTORE_SHARE = com.arjuna.ats.arjuna.objectstore.share
 * <li> OBJECTSTORE_HIERARCHY_RETRY = com.arjuna.ats.arjuna.objectstore.hierarchyRetry
 * <li> OBJECTSTORE_HIERARCHY_TIMEOUT = com.arjuna.ats.arjuna.objectstore.hierarchyTimeout
 * <li> RECOVERY_MANAGER_PORT = com.arjuna.ats.internal.arjuna.recovery.recoveryPort
 * <li> XA_NODE_IDENTIFIER = com.arjuna.ats.arjuna.xa.nodeIdentifier
 * <li> DEFAULT_TIMEOUT = com.arjuna.ats.arjuna.coordinator.defaultTimeout
 * </ul>
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Environment.java 2342 2006-03-30 13:06:17Z  $
 * @since 1.0.
 */

public class Environment
{
    public static final String PROPERTIES_FILE = "com.arjuna.ats.arjuna.common.propertiesFile";
    public static final String STATIC_INVENTORY_IMPLE = "com.arjuna.ats.internal.arjuna.inventory.staticInventoryImple";
    public static final String VAR_DIR = "com.arjuna.ats.arjuna.common.varDir";
    
    public static final String ACTION_STORE= "com.arjuna.ats.arjuna.coordinator.actionStore";
    public static final String ASYNC_COMMIT = "com.arjuna.ats.arjuna.coordinator.asyncCommit";
    public static final String ASYNC_PREPARE = "com.arjuna.ats.arjuna.coordinator.asyncPrepare";
    public static final String ASYNC_ROLLBACK = "com.arjuna.ats.arjuna.coordinator.asyncRollback";
    public static final String COMMIT_ONE_PHASE = "com.arjuna.ats.arjuna.coordinator.commitOnePhase";
    public static final String LOCALOSROOT = "com.arjuna.ats.arjuna.objectstore.localOSRoot";
    public static final String MAINTAIN_HEURISTICS = "com.arjuna.ats.arjuna.coordinator.maintainHeuristics";
    public static final String OBJECTSTORE_DIR = "com.arjuna.ats.arjuna.objectstore.objectStoreDir";
    public static final String OBJECTSTORE_SYNC = "com.arjuna.ats.arjuna.objectstore.objectStoreSync";
    public static final String OBJECTSTORE_TYPE = "com.arjuna.ats.arjuna.objectstore.objectStoreType";
    public static final String HASHED_DIRECTORIES = "com.arjuna.ats.arjuna.objectstore.hashedDirectories";
    public static final String TRANSACTION_LOG = "com.arjuna.ats.arjuna.coordinator.transactionLog";
    public static final String TRANSACTION_LOG_WRITE_OPTIMISATION = "com.arjuna.ats.arjuna.coordinator.transactionLog.writeOptimisation";
    public static final String TRANSACTION_SYNC = "com.arjuna.ats.arjuna.objectstore.transactionSync";
    public static final String READONLY_OPTIMISATION = "com.arjuna.ats.arjuna.coordinator.readonlyOptimisation";
    public static final String CLASSIC_PREPARE = "com.arjuna.ats.arjuna.coordinator.classicPrepare";
    public static final String ENABLE_STATISTICS = "com.arjuna.ats.arjuna.coordinator.enableStatistics";
    public static final String SHARED_TRANSACTION_LOG = "com.arjuna.ats.arjuna.coordinator.sharedTransactionLog";
    public static final String START_DISABLED = "com.arjuna.ats.arjuna.coordinator.startDisabled";
    public static final String JDBC_USER_DB_ACCESS = "com.arjuna.ats.arjuna.objectstore.jdbcUserDbAccess";
    public static final String JDBC_TX_DB_ACCESS = "com.arjuna.ats.arjuna.objectstore.jdbcTxDbAccess";
    public static final String JDBC_POOL_SIZE_INIT = "com.arjuna.ats.arjuna.objectstore.jdbcPoolSizeInitial";
    public static final String JDBC_POOL_SIZE_MAX = "com.arjuna.ats.arjuna.objectstore.jdbcPoolSizeMaximum";
    public static final String JDBC_POOL_PUT = "com.arjuna.ats.arjuna.objectstore.jdbcPoolPutConnections";
    public static final String LICENCE = "com.arjuna.ats.arjuna.licence";
    public static final String CACHE_STORE_SIZE = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.size";
    public static final String CACHE_STORE_SYNC = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.sync";
    public static final String CACHE_STORE_REMOVED_ITEMS = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.removedItems";
    public static final String CACHE_STORE_SCAN_PERIOD = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.scanPeriod";
    public static final String CACHE_STORE_WORK_ITEMS = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.workItems";
    public static final String CACHE_STORE_HASH = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.hash";
    public static final String PERIODIC_RECOVERY_PERIOD = "com.arjuna.ats.arjuna.recovery.periodicRecoveryPeriod" ;
    public static final String RECOVERY_BACKOFF_PERIOD = "com.arjuna.ats.arjuna.recovery.recoveryBackoffPeriod" ;
    public static final String TX_REAPER_MODE = "com.arjuna.ats.arjuna.coordinator.txReaperMode";
    public static final String TX_REAPER_TIMEOUT = "com.arjuna.ats.arjuna.coordinator.txReaperTimeout";
    public static final String OBJECTSTORE_SHARE = "com.arjuna.ats.arjuna.objectstore.share";
    public static final String OBJECTSTORE_HIERARCHY_RETRY = "com.arjuna.ats.arjuna.objectstore.hierarchyRetry";
    public static final String OBJECTSTORE_HIERARCHY_TIMEOUT = "com.arjuna.ats.arjuna.objectstore.hierarchyTimeout";
    public static final String RECOVERY_MANAGER_PORT = "com.arjuna.ats.internal.arjuna.recovery.recoveryPort";
    public static final String XA_NODE_IDENTIFIER = "com.arjuna.ats.arjuna.xa.nodeIdentifier";
    public static final String DEFAULT_TIMEOUT = "com.arjuna.ats.arjuna.coordinator.defaultTimeout";
    
}

