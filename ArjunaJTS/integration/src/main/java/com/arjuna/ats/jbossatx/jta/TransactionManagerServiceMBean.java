/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jbossatx.jta;

import org.jboss.tm.JBossXATerminator;
import org.jboss.tm.XAExceptionFormatter;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.net.InetAddress;

/**
 * The MBean interface for the TransactionManager JBoss service.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: TransactionManagerServiceMBean.java,v 1.3 2005/06/17 10:53:51 kconner Exp $
 */
public interface TransactionManagerServiceMBean
{
    /**
     * Retrieve a reference to the JTA transaction manager.
     *
     * @return A reference to the JTA transaction manager.
     */
    public TransactionManager getTransactionManager();

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
     * Retrieve a reference to the JTA TransactionSynchronizationRegistry.
     *
     * @return a reference to the JTA TransactionSynchronizationRegistry.
     */
    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry();

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
}