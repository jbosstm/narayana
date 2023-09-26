/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.common;

/**
 * A JMX MBean interface containing configuration for the core transaction coordinator.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface CoordinatorEnvironmentBeanMBean
{
    boolean isAsyncCommit();

    boolean isAsyncPrepare();

    boolean isAsyncRollback();

    boolean isCommitOnePhase();

    boolean isMaintainHeuristics();

    boolean isWriteOptimisation();

    boolean isReadonlyOptimisation();

    boolean isClassicPrepare();

    boolean isEnableStatistics();

    void setEnableStatistics(boolean enableStatistics);

    @Deprecated
    boolean isSharedTransactionLog();

    boolean isStartDisabled();

    String getTxReaperMode();

    long getTxReaperTimeout();

    long getTxReaperCancelWaitPeriod();

    long getTxReaperCancelFailWaitPeriod();

    int getTxReaperZombieMax();

    long getTxReaperTraceGracePeriod();

    long getTxReaperTraceInterval();

    int getDefaultTimeout();

    boolean isTransactionStatusManagerEnable();

    boolean isBeforeCompletionWhenRollbackOnly();

    String getCheckedActionFactoryClassName();

    @Deprecated
    String getCommunicationStore();
}