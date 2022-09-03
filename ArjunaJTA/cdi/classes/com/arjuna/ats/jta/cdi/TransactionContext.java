/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013-2018, Red Hat, Inc., and individual contributors
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

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.TransactionSynchronizationRegistry;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author paul.robinson@redhat.com 01/05/2013
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public class TransactionContext implements Context {

    private final Supplier<TransactionManager> transactionManagerSupplier;

    private final Supplier<TransactionSynchronizationRegistry> transactionSynchronizationRegistrySupplier;

    private final Map<Transaction, TransactionScopeCleanup<?>> transactions = new HashMap<>();

    /**
     * Creates a new {@link TransactionContext}.
     *
     * @deprecated Please use the {@link #TransactionContext(Supplier,
     * Supplier)} constructor instead.
     */
    @Deprecated
    public TransactionContext() {
        this(() -> jtaPropertyManager.getJTAEnvironmentBean().getTransactionManager(),
             () -> jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistry());
    }

    /**
     * Creates a new {@link TransactionContext}.
     *
     * @param transactionManagerSupplier a {@link Supplier} of a
     * {@link TransactionManager}; must not be {@code null}
     *
     * @param transactionSynchronizationRegistrySupplier a {@link
     * Supplier} of a {@link TransactionSynchronizationRegistry}; must
     * not be {@code null}
     *
     * @exception NullPointerException if either parameter is {@code null}
     */
    public TransactionContext(Supplier<TransactionManager> transactionManagerSupplier,
                              Supplier<TransactionSynchronizationRegistry> transactionSynchronizationRegistrySupplier) {
        super();
        this.transactionManagerSupplier = Objects.requireNonNull(transactionManagerSupplier);
        this.transactionSynchronizationRegistrySupplier = Objects.requireNonNull(transactionSynchronizationRegistrySupplier);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return TransactionScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {

        if (!isActive()) {
            throw new ContextNotActiveException(jtaLogger.i18NLogger.get_contextual_is_not_active());
        }
        if (contextual == null) {
            throw new RuntimeException(jtaLogger.i18NLogger.get_contextual_is_null());
        }

        PassivationCapable bean = (PassivationCapable) contextual;
        TransactionSynchronizationRegistry tsr = this.transactionSynchronizationRegistrySupplier.get();
        @SuppressWarnings("unchecked")
        final T resource = (T) tsr.getResource(bean.getId());

        if (resource != null) {
            return resource;
        } else if (creationalContext != null) {
            Transaction currentTransaction = getCurrentTransaction();
            T t = contextual.create(creationalContext);
            tsr.putResource(bean.getId(), t);

            synchronized (transactions) {
                @SuppressWarnings("unchecked")
                TransactionScopeCleanup<T> synch = (TransactionScopeCleanup<T>) transactions.get(currentTransaction);

                if (synch == null) {
                    synch = new TransactionScopeCleanup<>(this, currentTransaction);
                    transactions.put(currentTransaction, synch);
                }

                synch.registerBean(contextual, creationalContext, t);
            }

            return t;
        } else {
            return null;
        }
    }

    @Override
    public <T> T get(Contextual<T> contextual) {

        return get(contextual, null);
    }

    @Override
    public boolean isActive() {

        // Note that scope initialization and destruction events are
        // fired by NarayanaTransactionManager.  See
        // https://issues.jboss.org/browse/JBTM-3106 for details.

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
            return this.transactionManagerSupplier.get().getTransaction();
        } catch (SystemException e) {
            throw new RuntimeException(jtaLogger.i18NLogger.get_error_getting_current_tx(), e);
        }
    }

}
