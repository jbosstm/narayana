/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jts.common;

/**
 * A JMX MBean interface containing configuration for the JTS system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface JTSEnvironmentBeanMBean
{
    boolean isTransactionManager();

    boolean isNeedTranContext();

    boolean isAlwaysPropagateContext();

    String getInterposition();

    boolean isCheckedTransactions();

    boolean isSupportSubtransactions();

    boolean isSupportRollbackSync();

    boolean isSupportInterposedSynchronization();

    boolean isPropagateTerminator();

    String getContextPropMode();

    int getRecoveryManagerPort();

    String getRecoveryManagerAddress();

    boolean isTimeoutPropagation();

    boolean isIssueRecoveryRollback();

    int getCommitedTransactionRetryLimit();
}