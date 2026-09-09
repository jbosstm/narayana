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
    /**
     * Returns true if anynchronous commit behaviour is enabled.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.asyncCommit
     *
     * @return true if asynchronous commit is enabled, value otherwise.
     */
    boolean isAsyncCommit();

    /**
     * Returns true if asynchronous prepare behaviour is enabled.
     *
     * If true then during the prepare phase of an action a separate thread will be created for
     * preparing each participant registered with the action
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.asyncPrepare
     *
     * @return true if asynchronous prepare is enabled, false otherwise.
     */
    boolean isAsyncPrepare();

    /**
     * Returns true if asynchronous rollback behaviour is enabled.
     *
     * If true then a separate thread will be created to complete the second phase of the action
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.asyncRollback
     *
     * @return true if asynchronous rollback is enabled, false otherwise.
     */
    boolean isAsyncRollback();

    /**
     * Returns true if one phase commit optimization is to be used.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.commitOnePhase
     *
     * @return true if one phase commit is enabled, false otherwise.
     */
    boolean isCommitOnePhase();

    /**
     * Returns true if heuristic outcomes should be recorded.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.maintainHeuristics
     *
     * @return true if heuristics should be recorded, false otherwise.
     */
    boolean isMaintainHeuristics();

    /**
     * Returns true if write optimisation protocol should be used for PersistenceRecord.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.transactionLog.writeOptimisation
     *
     * @return true if write optimisation is enabled, false otherwise.
     */
    boolean isWriteOptimisation();

    /**
     * Returns true if handling of read only resources should be optimized.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.readonlyOptimisation
     *
     * @return true if read only optimization is enabled, false otherwise.
     */
    boolean isReadonlyOptimisation();

    /**
     * Returns true if the old style of prepare handling should be used for PersistenceRecord.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.classicPrepare
     *
     * @return true is classic prepare handling is enabled, false otherwise.
     */
    boolean isClassicPrepare();

    /**
     * Returns true if transaction statistics should be recorded.
     * Note: Enabling statistics may have a slight performance impact due to locking on the counter variables.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.enableStatistics
     * Effect: immediate.
     *
     * @return true if recording of transaction statistics is enabled, false otherwise.
     */
    boolean isEnableStatistics();

    /**
     * Sets if transaction statistics should be recorded or not.
     *
     * @param enableStatistics true to enable statistics gathering, false to disable.
     */
    void setEnableStatistics(boolean enableStatistics);

    /**
     * Returns if the transaction log should be run in shared mode or not.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.sharedTransactionLog
     *
     * @return true if transaction log sharing is enabled, false otherwise.
     */
    @Deprecated
    boolean isSharedTransactionLog();

    /**
     * Returns if the transaction manager should be created in a disabled state or not.
     *
     * Default: false (i.e. transaction manager is enabled on creation)
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.startDisabled
     *
     * @return true if the transaction manager will start in a disabled state, false otherwise.
     */
    boolean isStartDisabled();

    /**
     * Returns the operating mode of the transaction timeout processing system.
     *
     * Default: "DYNAMIC"
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.txReaperMode
     *
     * @return the operating mode of the transaction reaper.
     */
    String getTxReaperMode();

    /**
     * Returns the timeout (wakeup) interval of the reaper's PERIODIC mode, in milliseconds.
     *
     * Default: 120000ms
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.txReaperTimeout
     *
     * @return the sleep interval of the transaction reaper, in milliseconds.
     */
    long getTxReaperTimeout();

    /**
     * Returns the number of millisecs delay after a cancel is scheduled,
     * before the reaper tries to interrupt the worker thread executing the cancel.
     *
     * Default: 500ms
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.txReaperCancelWaitPeriod
     *
     * @return the reaper cancel wait period, in milliseconds.
     */
    long getTxReaperCancelWaitPeriod();

    /**
     * Returns the number of millisecs delay after a worker thread is interrupted,
     * before the reaper writes the it off as a zombie and starts a new thread.
     *
     * Default: 500ms
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.txReaperCancelFailWaitPeriod
     *
     * @return the reaper cancel wait fail period, in milliseconds.
     */
    long getTxReaperCancelFailWaitPeriod();

    /**
     * Returns the threshold for count of non-exited zombies at which
     * the system starts logging error messages.
     *
     * Default: 8
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.txReaperZombieMax
     *
     * @return the number of zombie threads at which errors will start being reported.
     */
    int getTxReaperZombieMax();

    /**
     * Returns the number of milliseconds delay after a transaction is started,
     * before the reaper will start taking periodic stack traces from it.
     *
     * Default: 180000 (3 minutes)
     *
     * @return the reaper tracing grace period, in milliseconds.
     */
    long getTxReaperTraceGracePeriod();

    /**
     * Returns the number of milliseconds interval between transaction stack trace snapshots.
     *
     * Default: 30000 (30 seconds)
     *
     * @return the reaper tracing interval, in milliseconds.
     */
    long getTxReaperTraceInterval();

    /**
     * Returns the default interval after which a transaction may be considered for timeout, in seconds.
     * Note: depending on the reaper mode and workload, transactions may not be timed out immediately.
     *
     * Default: 60
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.defaultTimeout
     *
     * @return the default transaction lifetime, in seconds.
     */
    int getDefaultTimeout();

    /**
     * Returns if the transaction status manager (TSM) service, needed for out of process recovery, should be provided or not.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.transactionStatusManagerEnable
     *
     * @return true is the transaction status manager is enabled, false otherwise.
     */
    boolean isTransactionStatusManagerEnable();

    /**
     * Returns if beforeCompletion should be called on Synchronizations when completing transactions that are marked rollback only.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.coordinator.beforeCompletionWhenRollbackOnly
     *
     * @return true if beforeCompletion will be called in rollback only cases, false otherwise.
     */
    boolean isBeforeCompletionWhenRollbackOnly();

    /**
     * Returns the class name of an implementation of CheckedActionFactory
     *
     * Default: "com.arjuna.ats.internal.arjuna.coordinator.CheckedActionFactoryImple"
     * Equivalent deprecated property: com.arjuna.ats.coordinator.checkedActionFactory
     *
     * @return the class name of the CheckedActionFactory implementation to use.
     */
    String getCheckedActionFactoryClassName();

    /**
     * Returns the symbolic name for the communication store type.
     *
     * Default: "HashedActionStore"
     *
     * @return the communication store name.
     */
    @Deprecated
    String getCommunicationStore();
}