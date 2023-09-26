/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.txoj.common;

/**
 * A JMX MBean interface containing configuration for the transactional object system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface TxojEnvironmentBeanMBean
{
    @Deprecated
    String getLockStoreDir();

    @Deprecated
    String getMultipleLockStore();

    @Deprecated
    String getSingleLockStore();

    boolean isAllowNestedLocking();
}