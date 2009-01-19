/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionManagerServiceMBean.java,v 1.5 2005/06/17 10:53:51 kconner Exp $
 */
package com.arjuna.ats.jbossatx.jts;

import org.jboss.tm.JBossXATerminator;
import org.jboss.tm.XAExceptionFormatter;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionSynchronizationRegistry;
import java.net.InetAddress;

/**
 * The MBean interface for the TransactionManager JBoss service.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: TransactionManagerServiceMBean.java,v 1.5 2005/06/17 10:53:51 kconner Exp $
 */
public interface TransactionManagerServiceMBean
{
    /**
     * Set the default transaction timeout used by this transaction manager.
     *
     * @param timeout The default timeout in seconds for all transactions created
     * using this transaction manager.
     *
     * @throws IllegalStateException if the MBean has already started.
     */
    public void setTransactionTimeout(int timeout) throws IllegalStateException ;

    /**
     * Get the default transaction timeout used by this transaction manager.
     *
     * @return The default timeout in seconds for all transactions created
     * using this transaction manager.
     */
    public int getTransactionTimeout() ;

    /**
     * Sets whether the transaction service should collate transaction service statistics.
     *
     * @param enabled
     */
    public void setStatisticsEnabled(boolean enabled);

    /**
     * Retrieves whether the statistics are enabled.
     * @return
     */
    public boolean getStatisticsEnabled();

    /**
     * Set whether the transaction propagation context manager should propagate a
     * full PropagationContext (JTS) or just a cut-down version (for JTA).
     *
     * @param propagateFullContext
     */
    public void setPropagateFullContext(boolean propagateFullContext);

    /**
     * Retrieve whether the transaction propagation context manager should propagate a
     * full PropagationContext (JTS) or just a cut-down version (for JTA).
     */
    public boolean getPropagateFullContext();

    /**
     * Retrieve a reference to the JTA transaction manager.
     *
     * @return A reference to the JTA transaction manager.
     */
    public TransactionManager getTransactionManager();

    /**
     * Retrieve a reference to the JTA TransactionSynchronizationRegistry.
     *
     * @return a reference to the JTA TransactionSynchronizationRegistry.
     */
    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry();

    /**
     * Get the XA Terminator
     *
     * @return the XA Terminator
     */
    public JBossXATerminator getXATerminator() ;

    /**
     * Retrieve a reference to the JTA user transaction manager.
     *
     * @return A reference to the JTA user transaction manager.
     */
    public UserTransaction getUserTransaction();

    /**
     * This method has been put in here so that it is compatible with the JBoss standard Transaction Manager.
     * As we do not support exception formatters just display a warning for the moment.
     */
    public void registerXAExceptionFormatter(Class c, XAExceptionFormatter f);

    /**
     * This method has been put in here so that it is compatible with the JBoss standard Transaction Manager.
     * As we do not support exception formatters just display a warning for the moment.
     */
    public void unregisterXAExceptionFormatter(Class c);

    /**
     * Returns the total number of transactions started over the lifetime of the server
     * @return the total number of transactions started over the lifetime of the server
     */
    public long getTransactionCount();

    /**
     * Returns the total number of nested transactions started over the lifetime of the server.
     * This will usually be 0 even when stats are enabled, since JTA disallows nested tx by default.
     * @return the total number of nested transactions started over the lifetime of the server.
     */
    public long getNestedTransactonCount();

    /**
     * Returns the number of heuristics that have occurred.
     * @return the number of heuristics that have occurred.
     */
    public long getHeuristicCount();

    /**
     * Returns the number of committed transactions
     * @return the number of committed transactions
     */
    public long getCommitCount();

    /**
     * Returns the number of rolledback transactions.
     * This is approximatly the same as applicationRollbackCount + ResourceRollbackCount.
     * @return
     */
    public long getRollbackCount();

    /**
     * Returns the number of transactions that have begun but not yet terminated.
     * This count is approximate, particularly in recovery situations.
     * @return the number of transactions that have begun but not yet terminated.
     */
    public long getRunningTransactionCount();

    /**
     * Returns the number of transactions that have been timed out by the transaction reaper.
     * @return the number of transactions that have been timed out by the transaction reaper.
     */
    public long getTimedoutCount();

    /**
     * Returns the number of transactions that have been rolled back by application request.
     * This includes those that timeout, since the timeout behaviour is considered an
     * attribute of the application configuration.
     * @return the number of transactions that have been rolled back by application request.
     */
    public long getApplicationRollbackCount();

    /**
     * Returns the number of transactions that rolled back due to resource failure.
     * @return the number of transactions that rolled back due to resource failure.
     */
    public long getResourceRollbackCount();

    /**
     * Set whether the recovery manager should be ran in the same VM as
     * JBoss.  If this is false the Recovery Manager is already expected to
     * be running when JBoss starts.
     * @param runRM
     *
     * @throws IllegalStateException If the MBean has already started.
     */
    public void setRunInVMRecoveryManager(boolean runRM) throws IllegalStateException ;

    /**
     * Get whether the recovery manager should be ran in the same VM as
     * JBoss.  If this is false the Recovery Manager is already expected to
     * be running when JBoss starts.
     *
     * @return true if the recover manager is running in the same VM, false otherwise.
     */
    public boolean getRunInVMRecoveryManager() ;

    /**
     * Set the object store directory.
     * @param objectStoreDir The object store directory.
     *
     * @throws IllegalStateException if the MBean has already started.
     */
    public void setObjectStoreDir(final String objectStoreDir) throws IllegalStateException ;

    /**
     * Get the object store directory.
     * @return objectStoreDir The object store directory.
     */
    public String getObjectStoreDir() ;

    /**
     * Set the flag indicating whether the propagation context should always be propagated.
     * @param alwaysPropagateContext true if the context should always be propagated, false if only propagated to OTS transactional objects.
     *
     * @throws IllegalStateException If the MBean has already started.
     */
    public void setAlwaysPropagateContext(final boolean alwaysPropagateContext) throws IllegalStateException ;

    /**
     * Get the flag indicating whether the propagation context should always be propagated.
     * @return true if the context should always be propagated, false if only propagated to OTS transactional objects.
     */
    public boolean getAlwaysPropagateContext() ;

    /**
     * Gets the address to which the TransactionStatusManager listener will bind.
     * @return InetAddress the address.
     */
    public InetAddress getTransactionStatusManagerInetAddress();

    /**
     * Sets the address to which the TransactionStatusManager will bind.
     * @param tsmInetAddress the address to set.
     * @throws IllegalStateException if the MBean has already started.
     */
    public void setTransactionStatusManagerInetAddress(InetAddress tsmInetAddress) throws IllegalStateException;

    /**
     * Gets the port number to which the TransactionStatusManager will bind.
     * @return int the port number.
     */
    public int getTransactionStatusManagerPort();

    /**
     * Sets the port number to which the TransactionStatusManager will bind.
     * @param port the port number to set.
     * @throws IllegalStateException if the MBean has already started.
     */
    public void setTransactionStatusManagerPort(int port) throws IllegalStateException;

    /**
     * Gets the address to which the RecoveryManager will bind, if it runs in-process with the application server.
     * @return InetAddress the address.
     */
    public InetAddress getRecoveryInetAddress();

    /**
     * Sets the address to which the RecvoeryManager will bind, if it runs in-process with the application server.
     * @param recoveryInetAddress the address to set
     * @throws IllegalStateException if the MBean has already started.
     */
    public void setRecoveryInetAddress(InetAddress recoveryInetAddress) throws IllegalStateException;

    /**
     * Gets the port number to which the RecoveryManager will bind, if it runs in-process with the application server.
     * @return int the port number.
     */
    public int getRecoveryPort();

    /**
     * Sets the port number to which the RecoveryManager will bind, if it runs in-process with the application server.
     * @param port
     * @throws IllegalStateException if the MBean has already started.
     */
    public void setRecoveryPort(int port) throws IllegalStateException;

    /**
     * Gets the port number to which the SocketProcessId service will bind.
     * @return
     */
    public int getSocketProcessIdPort();

    /**
     * Sets the port number to which the SocketProcessId service will bind.
     * @param port
     * @throws IllegalStateException if the MBean has already started.
     */
    public void setSocketProcessIdPort(int port) throws IllegalStateException;
}

