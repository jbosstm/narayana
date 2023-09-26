/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.ats.jbossatx.jta;

import org.jboss.tm.*;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.jbossatx.logging.jbossatxLogger;
import com.arjuna.common.util.ConfigurationInfo;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import jakarta.transaction.TransactionSynchronizationRegistry;

/**
 * JBoss Transaction Manager Service.
 *
 * Should be configured via deploy/transaction-jboss-beans.xml
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: TransactionManagerService.java,v 1.5 2005/06/24 15:24:15 kconner Exp $
 */
public class TransactionManagerService implements TransactionManagerServiceMBean
{
    protected String mode = "JTA";

    private JBossXATerminator jbossXATerminator = null;
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry = null;

    public TransactionManagerService() {}

    public void create()
    {
        String tag = ConfigurationInfo.getSourceId();

        jbossatxLogger.i18NLogger.info_jta_TransactionManagerService_create(mode, tag);

        // Associate transaction reaper with our context classloader.
        TransactionReaper.transactionReaper();
	}

    public void destroy()
    {
        jbossatxLogger.i18NLogger.info_jta_TransactionManagerService_destroy();
    }

    public void start()
    {
    }

    public void stop()
    {
    }


    /**
     * Retrieve a reference to the JTA transaction manager.
     *
     * @return A reference to the JTA transaction manager.
     */
    public TransactionManager getTransactionManager()
    {
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    /**
     * Retrieve a reference ot the JTA TransactionSynchronizationRegistry.
     *
     * @return a reference to the JTA TransactionSynchronizationRegistry.
     */
    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
    {
        return transactionSynchronizationRegistry;
    }

    public void setTransactionSynchronizationRegistry(TransactionSynchronizationRegistry transactionSynchronizationRegistry)
    {
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
    }

    /**
     * Get the XA Terminator
     *
     * @deprecated use getJbossXATerminator instead
     * @return the XA Terminator
     */
    public JBossXATerminator getXATerminator()
    {
       return getJbossXATerminator();
    }

    public JBossXATerminator getJbossXATerminator()
    {
        return jbossXATerminator;
    }

    public void setJbossXATerminator(JBossXATerminator jbossXATerminator)
    {
        this.jbossXATerminator = jbossXATerminator;
    }

    /**
     * Retrieve a reference to the JTA user transaction manager.
     *
     * @return A reference to the JTA user transaction manager.
     */
    public UserTransaction getUserTransaction()
    {
        return com.arjuna.ats.jta.UserTransaction.userTransaction();
    }

    /**
     * This method has been put in here so that it is compatible with the JBoss standard Transaction Manager.
     * As we do not support exception formatters just display a warning for the moment.
     */
    @Deprecated // the JBoss standard Transaction Manager no longer uses exception formatters
    public void registerXAExceptionFormatter(Class c, XAExceptionFormatter f)
    {
        jbossatxLogger.i18NLogger.warn_jta_TransactionManagerService_noformatter();
    }

    /**
     * This method has been put in here so that it is compatible with the JBoss standard Transaction Manager.
     * As we do not support exception formatters just display a warning for the moment.
     */
    public void unregisterXAExceptionFormatter(Class c)
    {
        // Ignore
    }
}