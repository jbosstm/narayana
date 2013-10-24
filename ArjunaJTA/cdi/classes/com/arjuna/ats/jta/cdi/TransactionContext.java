/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.ats.jta.cdi;


import com.arjuna.ats.jta.logging.jtaLogger;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.*;
import java.lang.annotation.Annotation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author paul.robinson@redhat.com 01/05/2013
 */
public class TransactionContext implements Context {

    private static TransactionManager transactionManager;

    private static TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    private Map<Transaction, TransactionScopeCleanup> transactions = new HashMap<Transaction, TransactionScopeCleanup>();

    @Override
    public Class<? extends Annotation> getScope() {
        return TransactionScoped.class;
    }

    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {

        if (!isActive()) {
            throw new ContextNotActiveException();
        }
        if (contextual == null) {
            throw new RuntimeException(jtaLogger.i18NLogger.get_contextual_is_null());
        }

        PassivationCapable bean = (PassivationCapable) contextual;
        TransactionSynchronizationRegistry tsr = getTransactionSynchronizationRegistry();
        Object resource = tsr.getResource(bean.getId());

        if (resource != null) {
            return (T) resource;
        } else if (creationalContext != null) {
            Transaction currentTransaction = getCurrentTransaction();
            T t = contextual.create(creationalContext);
            tsr.putResource(bean.getId(), t);

            synchronized (transactions) {
                TransactionScopeCleanup synch = transactions.get(currentTransaction);

                if (synch == null) {
                    synch = new TransactionScopeCleanup(this, currentTransaction);
                    transactions.put(currentTransaction, synch);
                }

                synch.registerBean(contextual, creationalContext, t);
            }

            return t;
        } else {
            return null;
        }
    }

    public <T> T get(Contextual<T> contextual) {

        return get(contextual, null);
    }

    public boolean isActive() {

        Transaction transaction = getCurrentTransaction();
        if (transaction == null) {
            return false;
        }

        try {
            int currentStatus = transaction.getStatus();
            return currentStatus == Status.STATUS_ACTIVE ||
                    currentStatus == Status.STATUS_MARKED_ROLLBACK ||
                    currentStatus == Status.STATUS_PREPARED ||
                    currentStatus == Status.STATUS_UNKNOWN ||
                    currentStatus == Status.STATUS_PREPARING ||
                    currentStatus == Status.STATUS_COMMITTING ||
                    currentStatus == Status.STATUS_ROLLING_BACK;
        } catch (SystemException e) {
            throw new RuntimeException(jtaLogger.i18NLogger.get_error_getting_tx_status(), e);
        }
    }

    void cleanupScope(Transaction transaction) {
        synchronized (transactions) {
            transactions.remove(transaction);
        }
    }

    private Transaction getCurrentTransaction() {

        try {
            TransactionManager tm = getTransactionManager();
            return tm.getTransaction();
        } catch (SystemException e) {
            throw new RuntimeException(jtaLogger.i18NLogger.get_error_getting_current_tx(), e);
        }
    }

    private TransactionManager getTransactionManager() {

        if (transactionManager == null) {
            try {
                InitialContext initialContext = new InitialContext();
                transactionManager = (TransactionManager) initialContext.lookup("java:jboss/TransactionManager");
            } catch (NamingException e) {
                throw new ContextNotActiveException(jtaLogger.i18NLogger.get_could_not_lookup_tm(), e);
            }
        }
        return transactionManager;
    }

    private TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {

        if (transactionSynchronizationRegistry == null) {
            try {
                InitialContext initialContext = new InitialContext();
                transactionSynchronizationRegistry = (TransactionSynchronizationRegistry) initialContext.lookup("java:jboss/TransactionSynchronizationRegistry");
            } catch (NamingException e) {
                throw new ContextNotActiveException(jtaLogger.i18NLogger.get_could_not_lookup_tsr(), e);
            }
        }
        return transactionSynchronizationRegistry;
    }
}