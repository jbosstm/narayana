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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionManagerService.java,v 1.5 2005/06/24 15:24:15 kconner Exp $
 */
package com.arjuna.ats.jbossatx.jta;

import org.jboss.tm.*;
import org.jboss.logging.Logger;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.common.Configuration;
import com.arjuna.ats.arjuna.logging.tsLogger;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionSynchronizationRegistry;

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
    protected Logger log = org.jboss.logging.Logger.getLogger(TransactionManagerService.class);
    protected String mode = "JTA";

    private JBossXATerminator jbossXATerminator = null;
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry = null;

    public TransactionManagerService() {}

    public void create() throws Exception
    {
        // Note that we use the arjunacore version of Configuration, as the jta one does not have
        // build properties set when we are running from the jts version of the build.
        String tag = Configuration.getBuildTimeProperty("SOURCEID");

        log.info("JBossTS Transaction Service ("+mode+" version - tag:"+tag+") - JBoss Inc.");

        // Associate transaction reaper with our context classloader.
        TransactionReaper.create();
	}

    public void destroy()
    {
        log.info("Destroying TransactionManagerService");
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
    public void registerXAExceptionFormatter(Class c, XAExceptionFormatter f)
    {
        log.warn("XAExceptionFormatters are not supported by the JBossTS Transaction Service - this warning can safely be ignored");
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
