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
package com.arjuna.ats.internal.jta.transaction.jts;

import com.arjuna.ats.internal.jta.utils.jtaxLogger;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.JTAInterposedSynchronizationImple;



import javax.transaction.*;
import java.io.Serializable;

/**
 * Implementation of the TransactionSynchronizationRegistry interface, in line with the JTA 1.1 specification.
 *
 * @author jonathan.halliday@jboss.com
 */
public class TransactionSynchronizationRegistryImple implements TransactionSynchronizationRegistry, Serializable {

    // This Imple is stateless and just delegates the work down to the transaction manager.
    // It's Serilizable so it can be shoved into the app server JNDI.

    /*
         * http://java.sun.com/javaee/5/docs/api/javax/transaction/TransactionSynchronizationRegistry.html
         * http://jcp.org/aboutJava/communityprocess/maintenance/jsr907/907ChangeLog.html
         */

        // Return an opaque object to represent the transaction bound to the current thread at the time this method is called.

    private static final long serialVersionUID = 1L;

    /**
         * @message com.arjuna.ats.internal.jta.transaction.jts.systemexception
         * [com.arjuna.ats.internal.jta.transaction.jts.systemexception]
         * The transaction implementation threw a SystemException
         */
        public Object getTransactionKey()
        {
                if (jtaxLogger.logger.isDebugEnabled()) {
                    jtaxLogger.logger.debug("TransactionSynchronizationRegistryImple.getTransactionKey");
                }

        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
                TransactionImple transactionImple = null;
                try
                {
                        transactionImple = (TransactionImple)tm.getTransaction();
        }
                catch (SystemException e)
                {
                        throw new RuntimeException(jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.systemexception"), e);
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
                if (jtaxLogger.logger.isDebugEnabled()) {
                    jtaxLogger.logger.debug("TransactionSynchronizationRegistryImple.putResource");
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
                if (jtaxLogger.logger.isDebugEnabled()) {
                    jtaxLogger.logger.debug("TransactionSynchronizationRegistryImple.getResource");
                }

                if(key ==  null)
                {
                        throw new NullPointerException();
                }

                TransactionImple transactionImple = getTransactionImple();
                return transactionImple.getTxLocalResource(key);
        }

        // Register a Synchronization instance with special ordering semantics.
        /**
         * @message com.arjuna.ats.internal.jta.transaction.jts.syncrollbackexception
         * [ccom.arjuna.ats.internal.jta.transaction.jts.syncrollbackexception]
         * The transaction implementation threw a RollbackException
         */
        public void registerInterposedSynchronization(Synchronization synchronization)
        {
                if (jtaxLogger.logger.isDebugEnabled()) {
                    jtaxLogger.logger.debug("TransactionSynchronizationRegistryImple.registerInterposedSynchronization");
                }

                TransactionImple transactionImple = getTransactionImple();

                try
                {
                        transactionImple.registerSynchronizationImple(new JTAInterposedSynchronizationImple(synchronization));
                }
                catch (RollbackException e)
                {
                        throw new com.arjuna.ats.jta.exceptions.RollbackException(jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.syncrollbackexception"), e);
                }
                catch (SystemException e)
                {
                        throw new RuntimeException(jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.systemexception"), e);
                }
        }

        // Return the status of the transaction bound to the current thread at the time this method is called.
        public int getTransactionStatus()
        {
                if (jtaxLogger.logger.isDebugEnabled()) {
                    jtaxLogger.logger.debug("TransactionSynchronizationRegistryImple.getTransactionStatus");
                }

                javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
                try
                {
                        return tm.getStatus();
                }
                catch(SystemException e)
                {
                        throw new RuntimeException(jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.systemexception"), e);
                }

        }

        // Set the rollbackOnly status of the transaction bound to the current thread at the time this method is called.
        public void setRollbackOnly()
        {
                if (jtaxLogger.logger.isDebugEnabled()) {
                    jtaxLogger.logger.debug("TransactionSynchronizationRegistryImple.setRollbackOnly");
                }

                javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
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
                        throw new RuntimeException(jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.systemexception"), e);
                }
        }

        // Get the rollbackOnly status of the transaction bound to the current thread at the time this method is called.
        public boolean getRollbackOnly()
        {
                if (jtaxLogger.logger.isDebugEnabled()) {
                    jtaxLogger.logger.debug("TransactionSynchronizationRegistryImple.getRollbackOnly");
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
                        throw new RuntimeException(jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.systemexception"), e);
                }
        }

        private TransactionImple getTransactionImple() throws IllegalStateException
        {
                javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
                TransactionImple transactionImple = null;
                try
                {
                        transactionImple = (TransactionImple)tm.getTransaction();
                }
                catch (SystemException e)
                {
                        throw new RuntimeException(jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.systemexception"), e);
                }

                if(transactionImple == null)
                {
                        throw new IllegalStateException();
                }

                return transactionImple;
        }
}
