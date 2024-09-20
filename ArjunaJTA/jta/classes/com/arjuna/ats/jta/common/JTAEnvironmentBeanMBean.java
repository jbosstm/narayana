/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.common;

import java.util.List;

/**
 * A JMX MBean interface containing configuration for the JTA system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface JTAEnvironmentBeanMBean
{
    boolean isSupportSubtransactions();

    String getTransactionManagerClassName();

    String getUserTransactionClassName();

    String getTransactionSynchronizationRegistryClassName();

    List<String> getXaRecoveryNodes();

    List<String> getXaResourceRecoveryClassNames();

    List<String> getXaResourceOrphanFilterClassNames();

    @Deprecated
    boolean isXaRollbackOptimization();

    boolean isXaAssumeRecoveryComplete();

    String getUserTransactionJNDIContext();

    String getTransactionManagerJNDIContext();

    String getTransactionSynchronizationRegistryJNDIContext();

    List<String> getXaResourceMapClassNames();

    boolean isXaTransactionTimeoutEnabled();

    String getLastResourceOptimisationInterfaceClassName();
}