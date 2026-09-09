/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.common;

import com.arjuna.ats.arjuna.coordinator.CheckedActionFactory;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.internal.arjuna.coordinator.CheckedActionFactoryImple;
import com.arjuna.ats.internal.arjuna.objectstore.HashedActionStore;
import com.arjuna.common.internal.util.ClassloadingUtility;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

import java.util.concurrent.ExecutorService;

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

    private volatile boolean useVirtualThreadsForTwoPhaseCommitThreads = true;
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
    private volatile long txReaperTraceGracePeriod = TransactionReaper.defaultUntracedPeriod;
    private volatile long txReaperTraceInterval = TransactionReaper.defaultTracePeriod;

    private volatile int defaultTimeout = 60; // seconds
    private volatile boolean transactionStatusManagerEnable = true;

    @FullPropertyName(name = "com.arjuna.ats.coordinator.beforeCompletionWhenRollbackOnly")
    private volatile boolean beforeCompletionWhenRollbackOnly = false;

    @FullPropertyName(name = "com.arjuna.ats.coordinator.checkedActionFactory")
    private volatile String checkedActionFactoryClassName = "com.arjuna.ats.internal.arjuna.coordinator.CheckedActionFactoryImple";
    private volatile CheckedActionFactory checkedActionFactory = null;
    
    private volatile boolean allowCheckedActionFactoryOverride; 

    private volatile boolean alternativeRecordOrdering = false;

    @Deprecated
    private volatile String communicationStore = HashedActionStore.class.getName();

    private volatile boolean finalizeBasicActions = false;

    public boolean isAsyncCommit()
    {
        return asyncCommit;
    }

    /**
     * Sets if asynchronous commit behaviour should be enabled or not.
     * Note: heuristics cannot be reported programatically if asynchronous commit is used.
     *
     * If true then a separate thread will be created to complete the second phase of the action
     * (provided that knowledge about heuristic outcomes is not required).
     *
     * @param asyncCommit true to enable asynchronous commit, false to disable.
     */
    public void setAsyncCommit(boolean asyncCommit)
    {
        this.asyncCommit = asyncCommit;
    }

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
     * <p>
     * Default: 100
     * <p>
     * When running on JRE version 21 and above {@link java.util.concurrent.Executors#newVirtualThreadPerTaskExecutor}
     * is used to create new threads so the size of the pool is unused.
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
     * Returns true if separate virtual threads are to be used to prepare resources when running in asynchronous mode
     * @see CoordinatorEnvironmentBean#asyncPrepare
     * <p>
     * @return true if virtual threads will be used
     */
    public boolean isUseVirtualThreadsForTwoPhaseCommitThreads() {
        return useVirtualThreadsForTwoPhaseCommitThreads;
    }

    /**
     * Enables the use of virtual threads for preparing resources
     * The setting will only take effect when running on JRE 21 and above.
     * <p>
     * @param enableVT true if virtual threads should be used to prepare resources when running in asynchronous mode
     */
    public void setUseVirtualThreadsForTwoPhaseCommitThreads(boolean enableVT) {
        useVirtualThreadsForTwoPhaseCommitThreads = enableVT;
    }

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

    public boolean isEnableStatistics()
    {
        return enableStatistics;
    }

    public void setEnableStatistics(boolean enableStatistics)
    {
        this.enableStatistics = enableStatistics;
    }

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

    public long getTxReaperTraceGracePeriod() {
        return txReaperTraceGracePeriod;
    }

    /**
     * Sets the delay after a transaction is started, before it is eligible for periodic tracing.
     *
     * @param txReaperTraceGracePeriod in milliseconds.
     */
    public void setTxReaperTraceGracePeriod(long txReaperTraceGracePeriod) {
        this.txReaperTraceGracePeriod = txReaperTraceGracePeriod;
    }

    public long getTxReaperTraceInterval() {
        return txReaperTraceInterval;
    }

    /**
     * Sets the interval between stack trace snapshots.
     *
     * @param txReaperTraceInterval in milliseconds.
     */
    public void setTxReaperTraceInterval(long txReaperTraceInterval) {
        this.txReaperTraceInterval = txReaperTraceInterval;
    }

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
        	if (checkedActionFactoryClassName == null || allowCheckedActionFactoryOverride) 
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
                    CheckedActionFactory instance = ClassloadingUtility.loadAndInstantiateClass(CheckedActionFactory.class, checkedActionFactoryClassName, null);
                    checkedActionFactory = instance;
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
        	if (checkedActionFactoryClassName == null || allowCheckedActionFactoryOverride)
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
     * The option is applicable to the volatile phase of the two phase commit protocol
     * which provides a synchronization mechanism that allows an interested party to be notified
     * before and after the transaction completes. If set to true then the beforeCompletion method will be
     * called on all non interposed synchronizations in parallel, after which the beforeCompletion method
     * will be called on all interposed synchronizations in parallel.
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
     * The option is applicable to the volatile phase of the two phase commit protocol
     * which provides a synchronization mechanism that allows an interested party to be notified
     * before and after the transaction completes. If set to true then the afterCompletion method will be
     * called on all interposed synchronizations in parallel, after which the afterCompletion method
     * will be called on all non interposed synchronizations in parallel.
     *
     * Caveat: if an action is committed and the caller wishes to be notified of heuristics outcomes then this
     * behaviour is not available and the afterCompletion callbacks will be invoked synchronously. .
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

    /**
     * Returns true if configured to allow the checked action factory to be overriden
     *
     * Default: true
     *
     * @return true if checked action factory can be overriden, false otherwise.
     */
    public boolean isAllowCheckedActionFactoryOverride() {
		return allowCheckedActionFactoryOverride;
	}

    /**
     * Can be enabled to allow the checked action factory to be overridden at runtime
     *
     * @param allowCheckedActionFactoryOverride Allow the checked action factory to be overriden
     */
    public void setAllowCheckedActionFactoryOverride(
			boolean allowCheckedActionFactoryOverride) {
		this.allowCheckedActionFactoryOverride = allowCheckedActionFactoryOverride;
	}   
    
}