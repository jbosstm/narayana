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

/**
 * A JMX MBean interface containing configuration for the core transaction coordinator.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface CoordinatorEnvironmentBeanMBean
{
    String getActionStore();

    void setActionStore(String actionStore);

    boolean isAsyncCommit();

    void setAsyncCommit(boolean asyncCommit);

    boolean isAsyncPrepare();

    void setAsyncPrepare(boolean asyncPrepare);

    boolean isAsyncRollback();

    void setAsyncRollback(boolean asyncRollback);

    boolean isCommitOnePhase();

    void setCommitOnePhase(boolean commitOnePhase);

    boolean isMaintainHeuristics();

    void setMaintainHeuristics(boolean maintainHeuristics);

    boolean isTransactionLog();

    void setTransactionLog(boolean transactionLog);

    boolean isWriteOptimisation();

    void setWriteOptimisation(boolean writeOptimisation);

    boolean isReadonlyOptimisation();

    void setReadonlyOptimisation(boolean readonlyOptimisation);

    boolean isClassicPrepare();

    void setClassicPrepare(boolean classicPrepare);

    boolean isEnableStatistics();

    void setEnableStatistics(boolean enableStatistics);

    boolean isSharedTransactionLog();

    void setSharedTransactionLog(boolean sharedTransactionLog);

    boolean isStartDisabled();

    void setStartDisabled(boolean startDisabled);

    String getTxReaperMode();

    void setTxReaperMode(String txReaperMode);

    long getTxReaperTimeout();

    void setTxReaperTimeout(long txReaperTimeout);

    long getTxReaperCancelWaitPeriod();

    void setTxReaperCancelWaitPeriod(long txReaperCancelWaitPeriod);

    long getTxReaperCancelFailWaitPeriod();

    void setTxReaperCancelFailWaitPeriod(long txReaperCancelFailWaitPeriod);

    int getTxReaperZombieMax();

    void setTxReaperZombieMax(int txReaperZombieMax);

    int getDefaultTimeout();

    void setDefaultTimeout(int defaultTimeout);

    boolean isTransactionStatusManagerEnable();

    void setTransactionStatusManagerEnable(boolean transactionStatusManagerEnable);

    boolean isBeforeCompletionWhenRollbackOnly();

    void setBeforeCompletionWhenRollbackOnly(boolean beforeCompletionWhenRollbackOnly);

    String getCheckedActionFactory();

    void setCheckedActionFactory(String checkedActionFactory);
}
