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

import com.arjuna.ats.arjuna.coordinator.CheckedActionFactory;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.internal.arjuna.common.ClassloadingUtility;
import com.arjuna.ats.internal.arjuna.coordinator.CheckedActionFactoryImple;
import com.arjuna.ats.internal.arjuna.objectstore.HashedActionStore;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

/**
 * A JavaBean containing configuration properties for the core transaction coordinator.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.arjuna.coordinator.")
public class CoordinatorEnvironmentBean implements CoordinatorEnvironmentBeanMBean
{
    private volatile boolean asyncCommit = false;
    private volatile boolean asyncPrepare = false;
    private volatile boolean asyncRollback = false;

    private volatile boolean asyncBeforeSynchronization;
    private volatile boolean asyncAfterSynchronization;

    private volatile boolean commitOnePhase = true;
    private volatile boolean maintainHeuristics = true;
    @Deprecated
    private volatile boolean transactionLog = false; // rename to useTransactionLog ?

    private volatile int maxTwoPhaseCommitThreads = 100;

    // public static final String TRANSACTION_LOG_REMOVAL_MARKER = "com.arjuna.ats.arjuna.coordinator.transactionLog.removalMarker";
    //private String removalMarker;

    @FullPropertyName(name = "com.arjuna.ats.arjuna.coordinator.transactionLog.writeOptimisation")
    private volatile boolean writeOptimisation = false;

    private volatile boolean dynamic1PC = true;
    private volatile boolean readonlyOptimisation = true;
    private volatile boolean classicPrepare = false;
    private volatile boolean enableStatistics = false;
    @Deprecated
    private volatile boolean sharedTransactionLog = false;
    private volatile boolean startDisabled = false; // rename/repurpose to 'enable'?
    private volatile String txReaperMode = "DYNAMIC"; // rename bool txReaperModeDynamic?

    private volatile long txReaperTimeout = TransactionReaper.defaultCheckPeriod;
    private volatile long txReaperCancelWaitPeriod = TransactionReaper.defaultCancelWaitPeriod;
    private volatile long txReaperCancelFailWaitPeriod = TransactionReaper.defaultCancelFailWaitPeriod;
    private volatile int txReaperZombieMax = TransactionReaper.defaultZombieMax;

    private volatile int defaultTimeout = 60; // seconds
    private volatile boolean transactionStatusManagerEnable = true;

    @FullPropertyName(name = "com.arjuna.ats.coordinator.beforeCompletionWhenRollbackOnly")
    private volatile boolean beforeCompletionWhenRollbackOnly = false;

    @FullPropertyName(name = "com.arjuna.ats.coordinator.checkedActionFactory")
    private volatile String checkedActionFactoryClassName = "com.arjuna.ats.internal.arjuna.coordinator.CheckedActionFactoryImple";
    private volatile CheckedActionFactory checkedActionFactory = null;

    private volatile boolean alternativeRecordOrdering = false;

    @Deprecated
    private volatile String communicationStore = HashedActionStore.class.getName();

    private volatile boolean finalizeBasicActions = false;

    /**
     * Returns true if anynchronous commit behaviour is enabled.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.asyncCommit
     *
     * @return true if asynchronous commit is enabled, value otherwise.
     */
    public boolean isAsyncCommit()
    {
        return asyncCommit;
    }

    /**
     * Sets if asynchronous commit behaviour should be enabled or not.
     * Note: heuristics cannot be reported programatically if asynchronous commit is used.
     *
     * @param asyncCommit true to enable asynchronous commit, false to disable.
     */
    public void setAsyncCommit(boolean asyncCommit)
    {
        this.asyncCommit = asyncCommit;
    }

    /**
     * Returns true if asynchronous prepare behaviour is enabled.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.asyncPrepare
     *
     * @return true if asynchronous prepare is enabled, false otherwise.
     */
    public boolean isAsyncPrepare()
    {
        return asyncPrepare;
    }

    /**
     * Sets if asynchronous prepare behaviour should be enabled or not.
     *
     * @param asyncPrepare true to enable asynchronous prepare, false to disable.
     */
    public void setAsyncPrepare(boolean asyncPrepare)
    {
        this.asyncPrepare = asyncPrepare;
    }

    /**
     * Returns true if asynchronous rollback behaviour is enabled.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.asyncRollback
     *
     * @return true if asynchronous rollback is enabled, false otherwise.
     */
    public boolean isAsyncRollback()
    {
        return asyncRollback;
    }

    /**
     * Sets if asynchronous rollback behaviour should be enabled or not.
     *
     * @param asyncRollback true to enable asynchronous rollback, false to disable.
     */
    public void setAsyncRollback(boolean asyncRollback)
    {
        this.asyncRollback = asyncRollback;
    }

    /**
     * Returns maximum thread pool size allowed for two phase commits.
     *
     * Default: 100
     *
     * @return maximum number of threads in a thread pool
     */
    public int getMaxTwoPhaseCommitThreads() {
        return maxTwoPhaseCommitThreads;
    }

    /**
     * Sets maximum thread pool size for two phase commits.
     *
     * @param maxTwoPhaseCommitThreads maximum number of threads in a thread pool
     */
    public void setMaxTwoPhaseCommitThreads(int maxTwoPhaseCommitThreads) {
        this.maxTwoPhaseCommitThreads = maxTwoPhaseCommitThreads;
    }

    /**
     * Returns true if one phase commit optimization is to be used.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.commitOnePhase
     *
     * @return true if one phase commit is enabled, false otherwise.
     */
    public boolean isCommitOnePhase()
    {
        return commitOnePhase;
    }

    /**
     * Sets if one phase commit behaviour is enabled or not.
     *
     * @param commitOnePhase true to enable, false to disable.
     */
    public void setCommitOnePhase(boolean commitOnePhase)
    {
        this.commitOnePhase = commitOnePhase;
    }
    
    /**
     * Returns true if dynamic one phase commit optimization is to be used. This means that
     * if the first N-1 participants in the intentions list return read-only then commit_one_phase
     * will be called on the last participant.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.dynamic1PC
     *
     * @return true if one phase commit is enabled, false otherwise.
     */
    public boolean getDynamic1PC()
    {
        return dynamic1PC;
    }

    /**
     * Sets if dynamic one phase commit behaviour is enabled or not.
     *
     * @param dynamic1PC true to enable, false to disable.
     */
    public void setDynamic1PC(boolean dynamic1PC)
    {
        this.dynamic1PC = dynamic1PC;
    }

    /**
     * Returns true if heuristic outcomes should be recorded.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.maintainHeuristics
     *
     * @return true if heuristics should be recorded, false otherwise.
     */
    public boolean isMaintainHeuristics()
    {
        return maintainHeuristics;
    }

    /**
     * Sets if heuristics should be recorded or not.
     *
     * @param maintainHeuristics true to enable recording of heuristics, false to disable.
     */
    public void setMaintainHeuristics(boolean maintainHeuristics)
    {
        this.maintainHeuristics = maintainHeuristics;
    }

    /**
     * Returns true if write optimisation protocol should be used for PersistenceRecord.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.transactionLog.writeOptimisation
     *
     * @return true if write optimisation is enabled, false otherwise.
     */
    public boolean isWriteOptimisation()
    {
        return writeOptimisation;
    }

    /**
     * Sets if write optimization protocol should be used for PersistenceRecord.
     *
     * @param writeOptimisation true to enable write optimization, false to disable.
     */
    public void setWriteOptimisation(boolean writeOptimisation)
    {
        this.writeOptimisation = writeOptimisation;
    }

    /**
     * Returns true if handling of read only resources should be optimized.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.readonlyOptimisation
     *
     * @return true if read only optimization is enabled, false otherwise.
     */
    public boolean isReadonlyOptimisation()
    {
        return readonlyOptimisation;
    }

    /**
     * Sets if handling of read only resources should be optimized.
     *
     * @param readonlyOptimisation true to enable read only optimization, false to disable.
     */
    public void setReadonlyOptimisation(boolean readonlyOptimisation)
    {
        this.readonlyOptimisation = readonlyOptimisation;
    }

    /**
     * Returns true if the old style of prepare handling should be used for PersistenceRecord.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.classicPrepare
     *
     * @return true is classic prepare handling is enabled, false otherwise.
     */
    public boolean isClassicPrepare()
    {
        return classicPrepare;
    }

    /**
     * Sets if old style prepare handling should be used for PersistenceRecord.
     *
     * @param classicPrepare true to enable classic prepare handling, false to disable.
     */
    public void setClassicPrepare(boolean classicPrepare)
    {
        this.classicPrepare = classicPrepare;
    }

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
    public boolean isEnableStatistics()
    {
        return enableStatistics;
    }

    /**
     * Sets if transaction statistics should be recorded or not.
     *
     * @param enableStatistics true to enable statistics gathering, false to disable.
     */
    public void setEnableStatistics(boolean enableStatistics)
    {
        this.enableStatistics = enableStatistics;
    }

    /**
     * Returns if the transaction log should be run in shared mode or not.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.sharedTransactionLog
     *
     * @return true if transaction log sharing is enabled, false otherwise.
     */
    @Deprecated
    public boolean isSharedTransactionLog()
    {
        return sharedTransactionLog;
    }

    /**
     * Sets if the transaction log should be run in shared mode or not.
     *
     * @param sharedTransactionLog true to enable transaction log sharing, false to disable.
     */
    @Deprecated
    public void setSharedTransactionLog(boolean sharedTransactionLog)
    {
        this.sharedTransactionLog = sharedTransactionLog;
    }

    /**
     * Returns if the transaction manager should be created in a disabled state or not.
     *
     * Default: false (i.e. transaction manager is enabled on creation)
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.startDisabled
     *
     * @return true if the transaction manager will start in a disabled state, false otherwise.
     */
    public boolean isStartDisabled()
    {
        return startDisabled;
    }

    /**
     * Sets if the transaction manager should be created in a disabled state or not.
     *
     * @param startDisabled true to start in a diabled state, false to start enabled.
     */
    public void setStartDisabled(boolean startDisabled)
    {
        this.startDisabled = startDisabled;
    }

    /**
     * Returns the operating mode of the transaction timeout processing system.
     *
     * Default: "DYNAMIC"
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.txReaperMode
     *
     * @return the operating mode of the transaction reaper.
     */
    public String getTxReaperMode()
    {
        return txReaperMode;
    }

    /**
     * Sets the operating mode of the transaction timeout processing system.
     *
     * @param txReaperMode the name of the required operating mode.
     */
    public void setTxReaperMode(String txReaperMode)
    {
        this.txReaperMode = txReaperMode;
    }

    /**
     * Returns the timeout (wakeup) interval of the reaper's PERIODIC mode, in milliseconds.
     *
     * Default: 120000ms
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.txReaperTimeout
     *
     * @return the sleep interval of the transaction reaper, in milliseconds.
     */
    public long getTxReaperTimeout()
    {
        return txReaperTimeout;
    }

    /**
     * Sets the timeout interval of the transaction reaper.
     *
     * @param txReaperTimeout the reaper sleep interval, in milliseconds.
     */
    public void setTxReaperTimeout(long txReaperTimeout)
    {
        this.txReaperTimeout = txReaperTimeout;
    }

    /**
     * Returns the number of millisecs delay after a cancel is scheduled,
     * before the reaper tries to interrupt the worker thread executing the cancel.
     *
     * Default: 500ms
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.txReaperCancelWaitPeriod
     *
     * @return the reaper cancel wait period, in milliseconds.
     */
    public long getTxReaperCancelWaitPeriod()
    {
        return txReaperCancelWaitPeriod;
    }

    /**
     * Sets the delay to allow a cancel to be processed before interrupting it.
     *
     * @param txReaperCancelWaitPeriod in milliseconds.
     */
    public void setTxReaperCancelWaitPeriod(long txReaperCancelWaitPeriod)
    {
        this.txReaperCancelWaitPeriod = txReaperCancelWaitPeriod;
    }

    /**
     * Returns the number of millisecs delay after a worker thread is interrupted,
     * before the reaper writes the it off as a zombie and starts a new thread.
     *
     * Default: 500ms
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.txReaperCancelFailWaitPeriod
     *
     * @return the reaper cancel wait fail period, in milliseconds.
     */
    public long getTxReaperCancelFailWaitPeriod()
    {
        return txReaperCancelFailWaitPeriod;
    }

    /**
     * Sets the delay to allow an interrupted cancel to be cleaned up.
     *
     * @param txReaperCancelFailWaitPeriod in milliseconds.
     */
    public void setTxReaperCancelFailWaitPeriod(long txReaperCancelFailWaitPeriod)
    {
        this.txReaperCancelFailWaitPeriod = txReaperCancelFailWaitPeriod;
    }

    /**
     * Returns the threshold for count of non-exited zombies at which
     * the system starts logging error messages.
     *
     * Default: 8
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.txReaperZombieMax
     *
     * @return the number of zombie threads at which errors will start being reported.
     */
    public int getTxReaperZombieMax()
    {
        return txReaperZombieMax;
    }

    /**
     * Sets the threshold number of zombie threads at which errors will start to be reported.
     *
     * @param txReaperZombieMax the number of threads.
     */
    public void setTxReaperZombieMax(int txReaperZombieMax)
    {
        this.txReaperZombieMax = txReaperZombieMax;
    }

    /**
     * Returns the default interval after which a transaction may be considered for timeout, in seconds.
     * Note: depending on the reaper mode and workload, transactions may not be timed out immediately.
     *
     * Default: 60
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.defaultTimeout
     *
     * @return the default transaction lifetime, in seconds.
     */
    public int getDefaultTimeout()
    {
        return defaultTimeout;
    }

    /**
     * Sets the default lifetime after which a transaction may be considered for timeout, in seconds.
     *
     * @param defaultTimeout the default transaction lifetime, in seconds.
     */
    public void setDefaultTimeout(int defaultTimeout)
    {
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Returns if the transaction status manager (TSM) service, needed for out of process recovery, should be provided or not.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.transactionStatusManagerEnable
     *
     * @return true is the transaction status manager is enabled, false otherwise.
     */
    public boolean isTransactionStatusManagerEnable()
    {
        return transactionStatusManagerEnable;
    }

    /**
     * Sets if the transaction status manager service should be provided or not.
     *
     * @param transactionStatusManagerEnable true to enable the TSM, false to disable.
     */
    public void setTransactionStatusManagerEnable(boolean transactionStatusManagerEnable)
    {
        this.transactionStatusManagerEnable = transactionStatusManagerEnable;
    }

    /**
     * Returns if beforeCompletion should be called on Synchronizations when completing transactions that are marked rollback only.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.coordinator.beforeCompletionWhenRollbackOnly
     *
     * @return true if beforeCompletion will be called in rollback only cases, false otherwise.
     */
    public boolean isBeforeCompletionWhenRollbackOnly()
    {
        return beforeCompletionWhenRollbackOnly;
    }

    /**
     * Sets if beforeCompletion should be called on transactions that are set rollback only.
     *
     * @param beforeCompletionWhenRollbackOnly true to call beforeCompletions on rollback only tx, false to skip them.
     */
    public void setBeforeCompletionWhenRollbackOnly(boolean beforeCompletionWhenRollbackOnly)
    {
        this.beforeCompletionWhenRollbackOnly = beforeCompletionWhenRollbackOnly;
    }

    /**
     * Returns the class name of an implementation of CheckedActionFactory
     *
     * Default: "com.arjuna.ats.internal.arjuna.coordinator.CheckedActionFactoryImple"
     * Equivalent deprecated property: com.arjuna.ats.coordinator.checkedActionFactory
     *
     * @return the class name of the CheckedActionFactory implementation to use.
     */
    public String getCheckedActionFactoryClassName()
    {
        return checkedActionFactoryClassName;
    }

    /**
     * Sets the class name of the CheckedActionFactory implementation.
     *
     * @param checkedActionFactoryClassName the name of a class that implements CheckedActionFactory.
     */
    public void setCheckedActionFactoryClassName(String checkedActionFactoryClassName)
    {
        synchronized(this)
        {
            if(checkedActionFactoryClassName == null)
            {
                this.checkedActionFactory = null;
            }
            else if(!checkedActionFactoryClassName.equals(this.checkedActionFactoryClassName))
            {
                this.checkedActionFactory = null;
            }
            this.checkedActionFactoryClassName = checkedActionFactoryClassName;
        }
    }

    /**
     * Returns an instance of a class implementing CheckedActionFactory.
     *
     * If there is no pre-instantiated instance set and classloading or instantiation fails,
     * this method will log appropriate warning and return null, not throw an exception.
     *
     * @return a CheckedActionFactory implementation instance, or null.
     */
    public CheckedActionFactory getCheckedActionFactory()
    {
        if(checkedActionFactory == null && checkedActionFactoryClassName != null)
        {
            synchronized (this) {
                if(checkedActionFactory == null && checkedActionFactoryClassName != null) {
                    try
                    {
                        CheckedActionFactory instance = ClassloadingUtility.loadAndInstantiateClass(CheckedActionFactory.class, checkedActionFactoryClassName, null);
                        checkedActionFactory = instance;
                    }
                    catch (final java.lang.RuntimeException ex) // todo android
                    {
                        if (Utility.isAndroid())
                            checkedActionFactory = new CheckedActionFactoryImple();
                        else
                            throw ex;
                    }
                }
            }
        }

        return checkedActionFactory;
    }

    /**
     * Sets the instance of CheckedActionFactory.
     *
     * @param instance an Object that implements CheckedActionFactory, or null.
     */
    public void setCheckedActionFactory(CheckedActionFactory instance)
    {
        synchronized(this)
        {
            CheckedActionFactory oldInstance = this.checkedActionFactory;
            checkedActionFactory = instance;

            if(instance == null)
            {
                this.checkedActionFactoryClassName = null;
            }
            else if(instance != oldInstance)
            {
                String name = ClassloadingUtility.getNameForClass(instance);
                this.checkedActionFactoryClassName = name;
            }
        }
    }


    /**
     * Whether to use the alternative abstract record ordering.
     * At present this is not fully documented, so stay away!
     *
     * Default: false
     *
     * @return <code>true</code> if order abstract records on type first, or
     * <code>false</code> if order on Uid first.
     */
    public boolean isAlternativeRecordOrdering()
    {
        return alternativeRecordOrdering;
    }

    /**
     * Set whether or not to use the alternative abstract record
     * ordering. Don't try this whilst the system is running!
     *
     * @param alternativeRecordOrdering true for alternative (i.e. type) ordering, false for normal (i.e. Uid) ordering.
     */
    public void setAlternativeRecordOrdering(boolean alternativeRecordOrdering)
    {
        this.alternativeRecordOrdering = alternativeRecordOrdering;
    }

    /**
     * Returns the symbolic name for the communication store type.
     *
     * Default: "HashedActionStore"
     *
     * @return the communication store name.
     */
    @Deprecated
    public String getCommunicationStore()
    {
        return communicationStore;
    }

    /**
     * Sets the symbolic name of the communication store.
     *
     * @param communicationStore the communication store name.
     */
    @Deprecated
    public void setCommunicationStore(String communicationStore)
    {
        this.communicationStore = communicationStore;
    }

    /**
     * Sets whether or not to use finalizers for BasicActions (i.e. transactions).
     * This can provide a useful safety net to ensure cleanup of locks and other
     * resources, but does not perform well.  In most cases it's preferable to
     * set a transaction timeout and rely on the reaper for cleanup.
     *
     * Default: false
     *
     * @return true if a finalize method should be registered for BasicActions instances, false otherwise.
     */
    public boolean isFinalizeBasicActions()
    {
        return finalizeBasicActions;
    }

    /**
     * Sets whether or not to use finalizers for BasicActions (i.e. transactions).
     *
     * @param finalizeBasicActions true to enable finalization, false to disable.
     */
    public void setFinalizeBasicActions(boolean finalizeBasicActions)
    {
        this.finalizeBasicActions = finalizeBasicActions;
    }

    /**
     * Returns true if asynchronous before completion behaviour is enabled.
     *
     * Default: false
     *
     * @return true if asynchronous before completion is enabled, value otherwise.
     */
    public boolean isAsyncBeforeSynchronization() {
        return asyncBeforeSynchronization;
    }

    /**
     * Sets if asynchronous before completion behaviour should be enabled or not.
     *
     * @param asyncBeforeSynchronization true to enable asynchronous before completion, false to disable.
     */
    public void setAsyncBeforeSynchronization(boolean asyncBeforeSynchronization) {
        this.asyncBeforeSynchronization = asyncBeforeSynchronization;
    }

    /**
     * Returns true if asynchronous after completion behaviour is enabled.
     *
     * Default: false
     *
     * @return true if asynchronous after completion is enabled, value otherwise.
     */
    public boolean isAsyncAfterSynchronization() {
        return asyncAfterSynchronization;
    }

    /**
     * Sets if asynchronous after completion behaviour should be enabled or not.
     *
     * @param asyncAfterSynchronization true to enable asynchronous after completion, false to disable.
     */
    public void setAsyncAfterSynchronization(boolean asyncAfterSynchronization) {
        this.asyncAfterSynchronization = asyncAfterSynchronization;
    }
}
