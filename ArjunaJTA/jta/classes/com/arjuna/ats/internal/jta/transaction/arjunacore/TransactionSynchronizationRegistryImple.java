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
 * (C) 2006,
 * @author JBoss Inc.
 *
 * $Id$
 */
package com.arjuna.ats.internal.jta.transaction.arjunacore;

import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.internal.jta.resources.arjunacore.SynchronizationImple;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import javax.transaction.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * Implementation of the TransactionSynchronizationRegistry interface, in line with the JTA 1.1 specification.
 *
 * @author jonathan.halliday@jboss.com
 */
public class TransactionSynchronizationRegistryImple implements TransactionSynchronizationRegistry, Serializable, ObjectFactory
{
    // This Imple is stateless and just delegates the work down to the transaction manager.
    // It's Serilizable so it can be shoved into the app server JNDI.

    /*
     * http://java.sun.com/javaee/5/docs/api/javax/transaction/TransactionSynchronizationRegistry.html
     * http://jcp.org/aboutJava/communityprocess/maintenance/jsr907/907ChangeLog.html
     */

    private static final long serialVersionUID = 1L;

    // cached for performance. Note: must set tm config before instantiating a TSRImple instance.
    private transient javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception
    {
        return this;
    }

    // Return an opaque object to represent the transaction bound to the current thread at the time this method is called.
    public Object getTransactionKey()
    {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("TransactionSynchronizationRegistryImple.getTransactionKey");
        }

        TransactionImple transactionImple = null;
        try
        {
            transactionImple = (TransactionImple)tm.getTransaction();
        }
        catch (SystemException e)
        {
            throw new RuntimeException(jtaLogger.i18NLogger.get_transaction_arjunacore_systemexception(), e);
        }

        if (transactionImple == null) {
            return null;
        } else {
            return transactionImple.get_uid();
        }
    }

    // Add or replace an object in the Map of resources being managed for the transaction bound to the current thread at the time this method is called.
    public void putResource(Object key, Object value)
    {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("TransactionSynchronizationRegistryImple.putResource");
        }

        if(key ==  null)
        {
            throw new NullPointerException();
        }

        TransactionImple transactionImple = getTransactionImple();
        transactionImple.putTxLocalResource(key, value);
    }

    // Get an object from the Map of resources being managed for the transaction bound to the current thread at the time this method is called.
    public Object getResource(Object key)
    {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("TransactionSynchronizationRegistryImple.getResource");
        }

        if(key ==  null)
        {
            throw new NullPointerException();
        }

        TransactionImple transactionImple = getTransactionImple();
        return transactionImple.getTxLocalResource(key);
    }

    // Register a Synchronization instance with special ordering semantics.
    public void registerInterposedSynchronization(Synchronization synchronization)
    {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("TransactionSynchronizationRegistryImple.registerInterposedSynchronization");
        }

        TransactionImple transactionImple = getTransactionImple();

        try
        {
            transactionImple.registerSynchronizationImple(new SynchronizationImple(synchronization, true));
        }
        catch (RollbackException e)
        {
            throw new com.arjuna.ats.jta.exceptions.RollbackException(jtaLogger.i18NLogger.get_transaction_arjunacore_syncrollbackexception(), e);
        }
        catch (SystemException e)
        {
            throw new RuntimeException(jtaLogger.i18NLogger.get_transaction_arjunacore_systemexception(), e);
        }
    }

    // Return the status of the transaction bound to the current thread at the time this method is called.
    public int getTransactionStatus()
    {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("TransactionSynchronizationRegistryImple.getTransactionStatus");
        }

        try
        {
            return tm.getStatus();
        }
        catch(SystemException e)
        {
            throw new RuntimeException(jtaLogger.i18NLogger.get_transaction_arjunacore_systemexception(), e);
        }

    }

    // Set the rollbackOnly status of the transaction bound to the current thread at the time this method is called.
    public void setRollbackOnly()
    {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("TransactionSynchronizationRegistryImple.setRollbackOnly");
        }

        try
        {
            Transaction transaction = tm.getTransaction();

            if(transaction == null)
            {
                throw new IllegalStateException();
            }

            tm.setRollbackOnly();
        }
        catch (SystemException e)
        {
            throw new RuntimeException(jtaLogger.i18NLogger.get_transaction_arjunacore_systemexception(), e);
        }
    }

    // Get the rollbackOnly status of the transaction bound to the current thread at the time this method is called.
    public boolean getRollbackOnly()
    {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("TransactionSynchronizationRegistryImple.getRollbackOnly");
        }

        TransactionImple transactionImple = getTransactionImple();

        if(transactionImple == null) {
            throw new IllegalStateException();
        }

        try
        {
            return (transactionImple.getStatus() == Status.STATUS_MARKED_ROLLBACK);
        }
        catch (SystemException e)
        {
            throw new RuntimeException(jtaLogger.i18NLogger.get_transaction_arjunacore_systemexception(), e);
        }
    }

    private TransactionImple getTransactionImple() throws IllegalStateException
    {
        TransactionImple transactionImple = null;
        try
        {
            transactionImple = (TransactionImple)tm.getTransaction();
        }
        catch (SystemException e)
        {
            throw new RuntimeException(jtaLogger.i18NLogger.get_transaction_arjunacore_systemexception(), e);
        }

        if(transactionImple == null)
        {
            throw new IllegalStateException();
        }

        return transactionImple;
    }
}
