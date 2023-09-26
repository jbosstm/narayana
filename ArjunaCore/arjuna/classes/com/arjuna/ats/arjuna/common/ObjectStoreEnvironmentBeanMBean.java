/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.common;

import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;

/**
 * A JMX MBean interface containing configuration for the objectstore and various implementations thereof.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface ObjectStoreEnvironmentBeanMBean
{
    int getCacheStoreSize();

    boolean isCacheStoreSync();

    int getCacheStoreRemovedItems();

    int getCacheStoreScanPeriod();

    int getCacheStoreWorkItems();

    int getCacheStoreHash();

    String getLocalOSRoot();

    String getObjectStoreDir();

    boolean isObjectStoreSync();

    String getObjectStoreType();

    int getHashedDirectories();

    boolean isTransactionSync();

    int getShare();

    int getHierarchyRetry();

    int getHierarchyTimeout();

    boolean isSynchronousRemoval();

    long getTxLogSize();

    long getPurgeTime();
    
	/**
	 * Get the JDBCAccess details.
	 */
	public String getJdbcAccess();

	/**
	 * Sets the instance of JDBCAccess
	 * 
	 * @param connectionDetails
	 *            an Object that provides JDBCAccess, or null.
	 */
	public void setJdbcAccess(String connectionDetails);

	/**
	 * Get the table prefix
	 * 
	 * @return The prefix to apply to the table
	 */
	public String getTablePrefix();

	/**
	 * Set the table prefix
	 * 
	 * @param tablePrefix
	 *            A prefix to use on the tables
	 */
	public void setTablePrefix(String tablePrefix);

	/**
	 * Should the store drop the table
	 * 
	 * @return Whether to drop the table
	 */
	public boolean getDropTable();

	/**
	 * Set whether to drop the table.
	 * 
	 * @param dropTable
	 *            Drop the table
	 */
	public void setDropTable(boolean dropTable);

}