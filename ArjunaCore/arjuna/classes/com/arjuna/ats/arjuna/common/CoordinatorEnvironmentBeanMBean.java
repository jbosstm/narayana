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

    boolean isSharedTransactionLog();

    boolean isStartDisabled();

    String getTxReaperMode();

    long getTxReaperTimeout();

    long getTxReaperCancelWaitPeriod();

    long getTxReaperCancelFailWaitPeriod();

    int getTxReaperZombieMax();

    int getDefaultTimeout();

    boolean isTransactionStatusManagerEnable();

    boolean isBeforeCompletionWhenRollbackOnly();

    String getCheckedActionFactoryClassName();

    String getCommunicationStore();
}
