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
package com.arjuna.ats.jts.common;

import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

import java.net.InetAddress;

/**
 * A JavaBean containing configuration properties for the JTS system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.jts.")
public class JTSEnvironmentBean implements JTSEnvironmentBeanMBean
{
    private volatile boolean transactionManager = false;
    private volatile boolean needTranContext = false;
    private volatile boolean alwaysPropagateContext = false;
    private volatile String interposition = null;
    private volatile boolean checkedTransactions = false;
    private volatile boolean supportSubtransactions = true;
    private volatile boolean supportRollbackSync = true;
    private volatile boolean supportInterposedSynchronization = false;
    private volatile boolean propagateTerminator = false;
    private volatile String contextPropMode = null;
    private volatile int recoveryManagerPort = 4711;
    private volatile String recoveryManagerAddress = "";

    @FullPropertyName(name = "com.arjuna.ats.jts.ots_1_0.timeoutPropagation")
    private volatile boolean timeoutPropagation = true;

    @FullPropertyName(name = "com.arjuna.ats.jts.recovery.issueRecoveryRollback")
    private volatile boolean issueRecoveryRollback = true;

    @FullPropertyName(name = "com.arjuna.ats.jts.recovery.commitTransactionRetryLimit")
    private volatile int commitedTransactionRetryLimit = 3;


    /**
     * Returns if an extenal transaction manager process should be used.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.jts.transactionManager
     *
     * @return true for separate transaction manaager process, false for in-process.
     */
    public boolean isTransactionManager()
    {
        return transactionManager;
    }

    /**
     * Sets if an external transaction manager process should be used.
     *
     * @param transactionManager true to enable use of a separate transaction manager, false to disable.
     */
    public void setTransactionManager(boolean transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    /**
     * Returns if transaction context interceptors will require a context to be present.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.jts.needTranContext
     *
     * @return true if context is required, false for optional context.
     */
    public boolean isNeedTranContext()
    {
        return needTranContext;
    }

    /**
     * Sets if transaction context interceptors will require a context to be present.
     *
     * @param needTranContext true to require a transaction context, false if it is optional.
     */
    public void setNeedTranContext(boolean needTranContext)
    {
        this.needTranContext = needTranContext;
    }

    /**
     * Returns if a transaction context should always be propagated on remote calls.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.jts.alwaysPropagateContext
     *
     * @return true to always propagate, false to propagate selectively.
     */
    public boolean isAlwaysPropagateContext()
    {
        return alwaysPropagateContext;
    }

    /**
     * Sets if a transaction context should always be propagated on remote calls.
     *
     * @param alwaysPropagateContext true to always propagate, false to propagate selectively.
     */
    public void setAlwaysPropagateContext(boolean alwaysPropagateContext)
    {
        this.alwaysPropagateContext = alwaysPropagateContext;
    }

    /**
     * Returns the Xid format interposition strategy.
     *
     * Default: null
     * Equivalent deprecated property: com.arjuna.ats.jts.interposition
     *
     * @return the name of the interposition implementation.
     */
    public String getInterposition()
    {
        return interposition;
    }

    /**
     * Sets the Xid format interposition strategy.
     *
     * @param interposition the name of the interposition implementation.
     */
    public void setInterposition(String interposition)
    {
        this.interposition = interposition;
    }

    /**
     * Returns if checked transactions should be used.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.jts.checkedTransactions
     *
     * @return true if checked transactions are enabled, false otherwise.
     */
    public boolean isCheckedTransactions()
    {
        return checkedTransactions;
    }

    /**
     * Sets if checked transactions should be used.
     *
     * @param checkedTransactions true to enable checked transactions, false to disable.
     */
    public void setCheckedTransactions(boolean checkedTransactions)
    {
        this.checkedTransactions = checkedTransactions;
    }

    /**
     * Returns if subtransactions should be used in the JTS.
     * Note that this is distinct from the JTA module subtransaction option.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.jts.supportSubtransactions
     *
     * @return true if subtransactions are allowed, false otherwise.
     */
    public boolean isSupportSubtransactions()
    {
        return supportSubtransactions;
    }

    /**
     * Sets if subtransactions are allowed in the JTS.
     *
     * @param supportSubtransactions true to enable subtransactions, false to disable.
     */
    public void setSupportSubtransactions(boolean supportSubtransactions)
    {
        this.supportSubtransactions = supportSubtransactions;
    }

    /**
     * Returns if Synchronizations should be fired on transaction rollback.
     * Note: this is distinct from the coordinator's beforeCompletionWhenRollbackOnly option.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.jts.supportRollbackSync
     *
     * @return true if Synchronizations will run on transaction rollback, false if they will be skipped. 
     */
    public boolean isSupportRollbackSync()
    {
        return supportRollbackSync;
    }

    /**
     * Sets if Synchronizations will be fired on transaction rollback.
     *
     * @param supportRollbackSync true to enable Synchronizations on rollback transactions, false to disable.
     */
    public void setSupportRollbackSync(boolean supportRollbackSync)
    {
        this.supportRollbackSync = supportRollbackSync;
    }

    /**
     * Returns if Synchronizations can be interposed i.e. registered direct with the parent coordinator.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.jts.supportInterposedSynchronization
     *
     * @return true for interposed (remote) Synchronization handling, false for local handling.
     */
    public boolean isSupportInterposedSynchronization()
    {
        return supportInterposedSynchronization;
    }

    /**
     * Sets if Synchronizations can be interposed.
     *
     * @param supportInterposedSynchronization true to enable interposition of synchronizations, false to disable.
     */
    public void setSupportInterposedSynchronization(boolean supportInterposedSynchronization)
    {
        this.supportInterposedSynchronization = supportInterposedSynchronization;
    }

    /**
     * Returns if a reference to the terminator should be included in the propagation context.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.jts.propagateTerminator
     *
     * @return true if the terminator should be propagated, false otherwise.
     */
    public boolean isPropagateTerminator()
    {
        return propagateTerminator;
    }

    /**
     * Sets if a reference to the terminator should be included in the propagation context.
     *
     * @param propagateTerminator true to enable propagation of the terminator, false to disable.
     */
    public void setPropagateTerminator(boolean propagateTerminator)
    {
        this.propagateTerminator = propagateTerminator;
    }

    /**
     * Returns the name of the context propagation mode.
     *  "CONTEXT" or "NONE"
     *
     * Default: null
     * Equivalent deprecated property: com.arjuna.ats.jts.contextPropMode
     *
     * @return the name of the context propagation mode.
     */
    public String getContextPropMode()
    {
        return contextPropMode;
    }

    /**
     * Sets the name of the context propagation mode.
     *
     * @param contextPropMode the name of the context propagation mode.
     */
    public void setContextPropMode(String contextPropMode)
    {
        this.contextPropMode = contextPropMode;
    }

    /**
     * Returns the port number on which the recovery manager will listen.
     *
     * Default: 4711
     * Equivalent deprecated property: com.arjuna.ats.jts.recoveryManagerPort
     *
     * @return the port number used by the recovery manager.
     */
    public int getRecoveryManagerPort()
    {
        return recoveryManagerPort;
    }

    /**
     * Sets the port number on which the recovery manager will listen.
     *
     * @param recoveryManagerPort the port number to use for the recovery manager.
     */
    public void setRecoveryManagerPort(int recoveryManagerPort)
    {
        this.recoveryManagerPort = recoveryManagerPort;
    }

    /**
     * Returns the hostname on which the recovery manager will bind.
     *
     * Default: ""
     * Equivalent deprecated property: com.arjuna.ats.jts.recoveryManagerAddress
     *
     * @return the hostname used by the recovery manager.
     */
    public String getRecoveryManagerAddress()
    {
        return recoveryManagerAddress;
    }

    /**
     * Sets the hostname on which the recovery manager will bind.
     *
     * @param recoveryManagerAddress the hostname to use for the recovery manager.
     */
    public void setRecoveryManagerAddress(String recoveryManagerAddress)
    {
        this.recoveryManagerAddress = recoveryManagerAddress;
    }

    /**
     * Sets the InetAddress on which the recovery manager should bind.
     * Mainly intended for use by strongly typed bean injection systems,
     * this is a wrapper around the String form of the method.
     *
     * @param inetAddress
     */
    public void setRecoveryManagerInetAddress(InetAddress inetAddress) {
        setRecoveryManagerAddress(inetAddress.getHostAddress());
    }

    /**
     * Returns if the timeout value sent should be the time remaining or not.
     * true for OTS 1.2 (time remaining), false for backwards compatible (total time)
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.jts.ots_1_0.timeoutPropagation
     *
     * @return true for remaining time propagation, false for total time propagation. 
     */
    public boolean isTimeoutPropagation()
    {
        return timeoutPropagation;
    }

    /**
     * Sets if the timeout value propagated should be time remaining or not.
     *
     * @param timeoutPropagation true for OTS 1.2 behaviour, false for backwards compatible.
     */
    public void setTimeoutPropagation(boolean timeoutPropagation)
    {
        this.timeoutPropagation = timeoutPropagation;
    }

    /**
     * Returns if resources will have rollback invoked explicitly on them by recovery.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.jts.recovery.issueRecoveryRollback
     *
     * @return true for explicit rollback by recovery, false to skip this.
     */
    public boolean isIssueRecoveryRollback()
    {
        return issueRecoveryRollback;
    }

    /**
     * Sets if resources will have rollback invoked explicitly on them by recovery.
     *
     * @param issueRecoveryRollback true to enable explicit rollback, false to disable.
     */
    public void setIssueRecoveryRollback(boolean issueRecoveryRollback)
    {
        this.issueRecoveryRollback = issueRecoveryRollback;
    }

    /**
     * Returns the number of attempts to make to notify resources of a transaction commit during recovery.
     *
     * Default: 3
     * Equivalent deprecated property: com.arjuna.ats.jts.recovery.commitTransactionRetryLimit
     *
     * @return the number of communication attempts to make.
     */
    public int getCommitedTransactionRetryLimit()
    {
        return commitedTransactionRetryLimit;
    }

    /**
     * Sets the number of attempts to make to notify resource of a transaction commit during recovery.
     *
     * @param commitedTransactionRetryLimit the number of communication attempts to make.
     */
    public void setCommitedTransactionRetryLimit(int commitedTransactionRetryLimit)
    {
        this.commitedTransactionRetryLimit = commitedTransactionRetryLimit;
    }
}
