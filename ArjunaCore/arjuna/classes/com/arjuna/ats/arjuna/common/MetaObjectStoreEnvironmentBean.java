/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.arjuna.ats.arjuna.common;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements an ObjectStoreEnvironmentBean instance that propagates all ObjectStoreEnvironmentBean
 * updates to all named instances of ObjectStoreEnvironmentBean. An instance of this class is
 * returned from a call to {@link arjPropertyManager#getObjectStoreEnvironmentBean()}
 */
public class MetaObjectStoreEnvironmentBean extends ObjectStoreEnvironmentBean {
    private ObjectStoreEnvironmentBean actionStoreEnvironmentBean;
    private List<ObjectStoreEnvironmentBean> instances = new ArrayList<>(3);

    public MetaObjectStoreEnvironmentBean() {
        actionStoreEnvironmentBean = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class);
        instances.add(actionStoreEnvironmentBean);
        instances.add(BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "stateStore"));
        instances.add(BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore"));
    }

    @Override
    public int getCacheStoreSize() {
        return actionStoreEnvironmentBean.getCacheStoreSize();
    }

    @Override
    public void setCacheStoreSize(int cacheStoreSize) {
        instances.forEach(i -> i.setCacheStoreSize(cacheStoreSize));
    }

    @Override
    public boolean isCacheStoreSync() {
        return actionStoreEnvironmentBean.isCacheStoreSync();
    }

    @Override
    public void setCacheStoreSync(boolean cacheStoreSync) {
        instances.forEach(i -> i.setCacheStoreSync(cacheStoreSync));
    }

    @Override
    public int getCacheStoreRemovedItems() {
        return actionStoreEnvironmentBean.getCacheStoreRemovedItems();
    }

    @Override
    public void setCacheStoreRemovedItems(int cacheStoreRemovedItems) {
        instances.forEach(i -> i.setCacheStoreRemovedItems(cacheStoreRemovedItems));
    }

    @Override
    public int getCacheStoreScanPeriod() {
        return actionStoreEnvironmentBean.getCacheStoreScanPeriod();
    }

    @Override
    public void setCacheStoreScanPeriod(int cacheStoreScanPeriod) {
        instances.forEach(i -> i.setCacheStoreScanPeriod(cacheStoreScanPeriod));
    }

    @Override
    public int getCacheStoreWorkItems() {
        return actionStoreEnvironmentBean.getCacheStoreWorkItems();
    }

    @Override
    public void setCacheStoreWorkItems(int cacheStoreWorkItems) {
        instances.forEach(i -> i.setCacheStoreWorkItems(cacheStoreWorkItems));
    }

    @Override
    public int getCacheStoreHash() {
        return actionStoreEnvironmentBean.getCacheStoreHash();
    }

    @Override
    public void setCacheStoreHash(int cacheStoreHash) {
        instances.forEach(i -> i.setCacheStoreHash(cacheStoreHash));
    }

    @Override
    public String getLocalOSRoot() {
        return actionStoreEnvironmentBean.getLocalOSRoot();
    }

    @Override
    public void setLocalOSRoot(String localOSRoot) {
        instances.forEach(i -> i.setLocalOSRoot(localOSRoot));
    }

    @Override
    public String getObjectStoreDir() {
        return actionStoreEnvironmentBean.getObjectStoreDir();
    }

    @Override
    public void setObjectStoreDir(String objectStoreDir) {
        instances.forEach(i -> i.setObjectStoreDir(objectStoreDir));
    }

    @Override
    public boolean isObjectStoreSync() {
        return actionStoreEnvironmentBean.isObjectStoreSync();
    }

    @Override
    public void setObjectStoreSync(boolean objectStoreSync) {
        instances.forEach(i -> i.setObjectStoreSync(objectStoreSync));
    }

    @Override
    public String getObjectStoreType() {
        return actionStoreEnvironmentBean.getObjectStoreType();
    }

    @Override
    public void setObjectStoreType(String objectStoreType) {
        instances.forEach(i -> i.setObjectStoreType(objectStoreType));
    }

    @Override
    public int getHashedDirectories() {
        return actionStoreEnvironmentBean.getHashedDirectories();
    }

    @Override
    public void setHashedDirectories(int hashedDirectories) {
        instances.forEach(i -> i.setHashedDirectories(hashedDirectories));
    }

    @Override
    public boolean isTransactionSync() {
        return actionStoreEnvironmentBean.isTransactionSync();
    }

    @Override
    public void setTransactionSync(boolean transactionSync) {
        instances.forEach(i -> i.setTransactionSync(transactionSync));
    }

    @Override
    public boolean isScanZeroLengthFiles() {
        return actionStoreEnvironmentBean.isScanZeroLengthFiles();
    }

    @Override
    public void setScanZeroLengthFiles(boolean scanZeroLengthFiles) {
        instances.forEach(i -> i.setScanZeroLengthFiles(scanZeroLengthFiles));
    }

    @Override
    public int getShare() {
        return actionStoreEnvironmentBean.getShare();
    }

    @Override
    public void setShare(int share) {
        instances.forEach(i -> i.setShare(share));
    }

    @Override
    public int getHierarchyRetry() {
        return actionStoreEnvironmentBean.getHierarchyRetry();
    }

    @Override
    public void setHierarchyRetry(int hierarchyRetry) {
        instances.forEach(i -> i.setHierarchyRetry(hierarchyRetry));
    }

    @Override
    public int getHierarchyTimeout() {
        return actionStoreEnvironmentBean.getHierarchyTimeout();
    }

    @Override
    public void setHierarchyTimeout(int hierarchyTimeout) {
        instances.forEach(i -> i.setHierarchyTimeout(hierarchyTimeout));
    }

    @Override
    public boolean isSynchronousRemoval() {
        return actionStoreEnvironmentBean.isSynchronousRemoval();
    }

    @Override
    public void setSynchronousRemoval(boolean synchronousRemoval) {
        instances.forEach(i -> i.setSynchronousRemoval(synchronousRemoval));
    }

    @Override
    public long getTxLogSize() {
        return actionStoreEnvironmentBean.getTxLogSize();
    }

    @Override
    public void setTxLogSize(long txLogSize) {
        instances.forEach(i -> i.setTxLogSize(txLogSize));
    }

    @Override
    public long getPurgeTime() {
        return actionStoreEnvironmentBean.getPurgeTime();
    }

    @Override
    public void setPurgeTime(long purgeTime) {
        instances.forEach(i -> i.setPurgeTime(purgeTime));
    }

    @Override
    public String getJdbcAccess() {
        return actionStoreEnvironmentBean.getJdbcAccess();
    }

    @Override
    public void setJdbcAccess(String connectionDetails) {
        instances.forEach(i -> i.setJdbcAccess(connectionDetails));
    }

    @Override
    public String getTablePrefix() {
        return actionStoreEnvironmentBean.getTablePrefix();
    }

    @Override
    public void setTablePrefix(String tablePrefix) {
        instances.forEach(i -> i.setTablePrefix(tablePrefix));
    }

    @Override
    public boolean getDropTable() {
        return actionStoreEnvironmentBean.getDropTable();
    }

    @Override
    public void setDropTable(boolean dropTable) {
        instances.forEach(i -> i.setDropTable(dropTable));
    }

    @Override
    public boolean getCreateTable() {
        return actionStoreEnvironmentBean.getCreateTable();
    }

    @Override
    public void setCreateTable(boolean createTable) {
        instances.forEach(i -> i.setCreateTable(createTable));
    }

    @Override
    public boolean getExposeAllLogRecordsAsMBeans() {
        return actionStoreEnvironmentBean.getExposeAllLogRecordsAsMBeans();
    }

    @Override
    public void setExposeAllLogRecordsAsMBeans(boolean exposeAllLogRecords) {
        instances.forEach(i -> i.setExposeAllLogRecordsAsMBeans(exposeAllLogRecords));
    }

    @Override
    public boolean isIgnoreMBeanHeuristics() {
        return actionStoreEnvironmentBean.isIgnoreMBeanHeuristics();
    }

    @Override
    public void setIgnoreMBeanHeuristics(boolean ignoreMBeanHeuristics) {
        instances.forEach(i -> i.setIgnoreMBeanHeuristics(ignoreMBeanHeuristics));
    }

    @Override
    public boolean isVolatileStoreSupportAllObjUids() {
        return actionStoreEnvironmentBean.isVolatileStoreSupportAllObjUids();
    }

    @Override
    public void setVolatileStoreSupportAllObjUids(boolean volatileStoreSupportAllObjUids) {
        instances.forEach(i -> i.setVolatileStoreSupportAllObjUids(volatileStoreSupportAllObjUids));
    }
}
