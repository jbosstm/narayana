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

import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;

/**
 * A JavaBean containing configuration properties for the core transaction coordinator.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.arjuna.coordinator.")
public class CoordinatorEnvironmentBean implements CoordinatorEnvironmentBeanMBean
{
    private String actionStore = ArjunaNames.Implementation_ObjectStore_defaultActionStore().stringForm();
    private boolean asyncCommit = false;
    private boolean asyncPrepare = false;
    private boolean asyncRollback = false;
    private boolean commitOnePhase = true;
    private boolean maintainHeuristics = true;
    private boolean transactionLog = false; // rename to useTransactionLog ?

    // public static final String TRANSACTION_LOG_REMOVAL_MARKER = "com.arjuna.ats.arjuna.coordinator.transactionLog.removalMarker";
    //private String removalMarker;

    @FullPropertyName(name = "com.arjuna.ats.arjuna.coordinator.transactionLog.writeOptimisation")
    private boolean writeOptimisation = false;

    private boolean readonlyOptimisation = true;
    private boolean classicPrepare = false;
    private boolean enableStatistics = false;
    private boolean sharedTransactionLog = false;
    private boolean startDisabled = false; // rename/repurpose to 'enable'?
    private String txReaperMode = "DYNAMIC"; // rename bool txReaperModeDynamic?

    private long txReaperTimeout = TransactionReaper.defaultCheckPeriod;
    private long txReaperCancelWaitPeriod = TransactionReaper.defaultCancelWaitPeriod;
    private long txReaperCancelFailWaitPeriod = TransactionReaper.defaultCancelFailWaitPeriod;
    private int txReaperZombieMax = TransactionReaper.defaultZombieMax;
    
    private int defaultTimeout = 60; // seconds
    private boolean transactionStatusManagerEnable = true;

    @FullPropertyName(name = "com.arjuna.ats.coordinator.beforeCompletionWhenRollbackOnly")
    private boolean beforeCompletionWhenRollbackOnly = false;
    @FullPropertyName(name = "com.arjuna.ats.coordinator.checkedActionFactory")
    private String checkedActionFactory = null;


    //    public static final String ACTION_STORE= "com.arjuna.ats.arjuna.coordinator.actionStore";
    public String getActionStore()
    {
        return actionStore;
    }

    public void setActionStore(String actionStore)
    {
        this.actionStore = actionStore;
    }

//    public static final String ASYNC_COMMIT = "com.arjuna.ats.arjuna.coordinator.asyncCommit";
    public boolean isAsyncCommit()
    {
        return asyncCommit;
    }

    public void setAsyncCommit(boolean asyncCommit)
    {
        this.asyncCommit = asyncCommit;
    }

//    public static final String ASYNC_PREPARE = "com.arjuna.ats.arjuna.coordinator.asyncPrepare";
    public boolean isAsyncPrepare()
    {
        return asyncPrepare;
    }

    public void setAsyncPrepare(boolean asyncPrepare)
    {
        this.asyncPrepare = asyncPrepare;
    }

//    public static final String ASYNC_ROLLBACK = "com.arjuna.ats.arjuna.coordinator.asyncRollback";
    public boolean isAsyncRollback()
    {
        return asyncRollback;
    }

    public void setAsyncRollback(boolean asyncRollback)
    {
        this.asyncRollback = asyncRollback;
    }

//    public static final String COMMIT_ONE_PHASE = "com.arjuna.ats.arjuna.coordinator.commitOnePhase";
    public boolean isCommitOnePhase()
    {
        return commitOnePhase;
    }

    public void setCommitOnePhase(boolean commitOnePhase)
    {
        this.commitOnePhase = commitOnePhase;
    }

//    public static final String MAINTAIN_HEURISTICS = "com.arjuna.ats.arjuna.coordinator.maintainHeuristics";
    public boolean isMaintainHeuristics()
    {
        return maintainHeuristics;
    }

    public void setMaintainHeuristics(boolean maintainHeuristics)
    {
        this.maintainHeuristics = maintainHeuristics;
    }

//    public static final String TRANSACTION_LOG = "com.arjuna.ats.arjuna.coordinator.transactionLog";
    public boolean isTransactionLog()
    {
        return transactionLog;
    }

    public void setTransactionLog(boolean transactionLog)
    {
        this.transactionLog = transactionLog;
    }



//    public static final String TRANSACTION_LOG_WRITE_OPTIMISATION = "com.arjuna.ats.arjuna.coordinator.transactionLog.writeOptimisation";
    public boolean isWriteOptimisation()
    {
        return writeOptimisation;
    }

    public void setWriteOptimisation(boolean writeOptimisation)
    {
        this.writeOptimisation = writeOptimisation;
    }

//    public static final String READONLY_OPTIMISATION = "com.arjuna.ats.arjuna.coordinator.readonlyOptimisation";
    public boolean isReadonlyOptimisation()
    {
        return readonlyOptimisation;
    }

    public void setReadonlyOptimisation(boolean readonlyOptimisation)
    {
        this.readonlyOptimisation = readonlyOptimisation;
    }

//    public static final String CLASSIC_PREPARE = "com.arjuna.ats.arjuna.coordinator.classicPrepare";
    public boolean isClassicPrepare()
    {
        return classicPrepare;
    }

    public void setClassicPrepare(boolean classicPrepare)
    {
        this.classicPrepare = classicPrepare;
    }

//    public static final String ENABLE_STATISTICS = "com.arjuna.ats.arjuna.coordinator.enableStatistics";
    public boolean isEnableStatistics()
    {
        return enableStatistics;
    }

    public void setEnableStatistics(boolean enableStatistics)
    {
        this.enableStatistics = enableStatistics;
    }

//    public static final String SHARED_TRANSACTION_LOG = "com.arjuna.ats.arjuna.coordinator.sharedTransactionLog";
    public boolean isSharedTransactionLog()
    {
        return sharedTransactionLog;
    }

    public void setSharedTransactionLog(boolean sharedTransactionLog)
    {
        this.sharedTransactionLog = sharedTransactionLog;
    }

//    public static final String START_DISABLED = "com.arjuna.ats.arjuna.coordinator.startDisabled";
    public boolean isStartDisabled()
    {
        return startDisabled;
    }

    public void setStartDisabled(boolean startDisabled)
    {
        this.startDisabled = startDisabled;
    }

//    public static final String TX_REAPER_MODE = "com.arjuna.ats.arjuna.coordinator.txReaperMode";
    public String getTxReaperMode()
    {
        return txReaperMode;
    }

    public void setTxReaperMode(String txReaperMode)
    {
        this.txReaperMode = txReaperMode;
    }

//    public static final String TX_REAPER_TIMEOUT = "com.arjuna.ats.arjuna.coordinator.txReaperTimeout";
    public long getTxReaperTimeout()
    {
        return txReaperTimeout;
    }

    public void setTxReaperTimeout(long txReaperTimeout)
    {
        this.txReaperTimeout = txReaperTimeout;
    }

//    public static final String TX_REAPER_CANCEL_WAIT_PERIOD = "com.arjuna.ats.arjuna.coordinator.txReaperCancelWaitPeriod";
    public long getTxReaperCancelWaitPeriod()
    {
        return txReaperCancelWaitPeriod;
    }

    public void setTxReaperCancelWaitPeriod(long txReaperCancelWaitPeriod)
    {
        this.txReaperCancelWaitPeriod = txReaperCancelWaitPeriod;
    }

//    public static final String TX_REAPER_CANCEL_FAIL_WAIT_PERIOD = "com.arjuna.ats.arjuna.coordinator.txReaperCancelFailWaitPeriod";
    public long getTxReaperCancelFailWaitPeriod()
    {
        return txReaperCancelFailWaitPeriod;
    }

    public void setTxReaperCancelFailWaitPeriod(long txReaperCancelFailWaitPeriod)
    {
        this.txReaperCancelFailWaitPeriod = txReaperCancelFailWaitPeriod;
    }

//    public static final String TX_REAPER_ZOMBIE_MAX = "com.arjuna.ats.arjuna.coordinator.txReaperZombieMax";
    public int getTxReaperZombieMax()
    {
        return txReaperZombieMax;
    }

    public void setTxReaperZombieMax(int txReaperZombieMax)
    {
        this.txReaperZombieMax = txReaperZombieMax;
    }

//    public static final String DEFAULT_TIMEOUT = "com.arjuna.ats.arjuna.coordinator.defaultTimeout";
    public int getDefaultTimeout()
    {
        return defaultTimeout;
    }

    public void setDefaultTimeout(int defaultTimeout)
    {
        this.defaultTimeout = defaultTimeout;
    }

//    public static final String TRANSACTION_STATUS_MANAGER_ENABLE = "com.arjuna.ats.arjuna.coordinator.transactionStatusManagerEnable";
    public boolean isTransactionStatusManagerEnable()
    {
        return transactionStatusManagerEnable;
    }

    public void setTransactionStatusManagerEnable(boolean transactionStatusManagerEnable)
    {
        this.transactionStatusManagerEnable = transactionStatusManagerEnable;
    }

//    public static final String BEFORECOMPLETION_WHEN_ROLLBACKONLY = "com.arjuna.ats.coordinator.beforeCompletionWhenRollbackOnly";
    public boolean isBeforeCompletionWhenRollbackOnly()
    {
        return beforeCompletionWhenRollbackOnly;
    }

    public void setBeforeCompletionWhenRollbackOnly(boolean beforeCompletionWhenRollbackOnly)
    {
        this.beforeCompletionWhenRollbackOnly = beforeCompletionWhenRollbackOnly;
    }

//    public static final String CHECKEDACTION_FACTORY = "com.arjuna.ats.coordinator.checkedActionFactory";
    public String getCheckedActionFactory()
    {
        return checkedActionFactory;
    }

    public void setCheckedActionFactory(String checkedActionFactory)
    {
        this.checkedActionFactory = checkedActionFactory;
    }
}
