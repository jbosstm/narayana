/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.osgi.jta.internal;

import jakarta.transaction.Transaction;
import javax.transaction.xa.XAException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.narayana.osgi.jta.internal.OsgiTransactionManager.Listener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 */
public class PlatformTransactionManagerImple extends JtaTransactionManager {

    private final Map<Transaction, SuspendedResourcesHolder> suspendedResources = new ConcurrentHashMap<>();

    public PlatformTransactionManagerImple(OsgiTransactionManager transactionManager) throws XAException {
        super(transactionManager, transactionManager);
        transactionManager.setListener(new Listener() {
            public void resumed(Transaction transaction) {
                SuspendedResourcesHolder holder = suspendedResources.remove(transaction);
                if (holder != null) {
                    TransactionSynchronizationManager.setActualTransactionActive(true);
                    TransactionSynchronizationManager.setCurrentTransactionReadOnly(holder.isReadOnly());
                    TransactionSynchronizationManager.setCurrentTransactionName(holder.getName());
                    TransactionSynchronizationManager.initSynchronization();
                    for (TransactionSynchronization synchronization : holder.getSuspendedSynchronizations()) {
                        synchronization.resume();
                        TransactionSynchronizationManager.registerSynchronization(synchronization);
                    }
                }
            }
            public void suspended(Transaction transaction) {
                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                    List<TransactionSynchronization> suspendedSynchronizations = TransactionSynchronizationManager.getSynchronizations();
                    for (TransactionSynchronization suspendedSynchronization : suspendedSynchronizations) {
                        suspendedSynchronization.suspend();
                    }
                    TransactionSynchronizationManager.clearSynchronization();
                    String name = TransactionSynchronizationManager.getCurrentTransactionName();
                    TransactionSynchronizationManager.setCurrentTransactionName(null);
                    boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
                    TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
                    TransactionSynchronizationManager.setActualTransactionActive(false);
                    SuspendedResourcesHolder holder = new SuspendedResourcesHolder(suspendedSynchronizations, name, readOnly);
                    suspendedResources.put(transaction, holder);
                }
            }
        });
    }

    public static ServiceRegistration register(BundleContext bundleContext, OsgiTransactionManager transactionManager) throws Exception {
        PlatformTransactionManagerImple ptm = new PlatformTransactionManagerImple(transactionManager);
        return bundleContext.registerService(PlatformTransactionManager.class, ptm, null);
    }

    /**
     * Holder for suspended resources.
     * Used internally by <code>suspend</code> and <code>resume</code>.
     */
    private static class SuspendedResourcesHolder {

        private final List<TransactionSynchronization> suspendedSynchronizations;
        private final String name;
        private final boolean readOnly;

        public SuspendedResourcesHolder(List<TransactionSynchronization> suspendedSynchronizations, String name, boolean readOnly) {
            this.suspendedSynchronizations = suspendedSynchronizations;
            this.name = name;
            this.readOnly = readOnly;
        }

        public List<TransactionSynchronization> getSuspendedSynchronizations() {
            return suspendedSynchronizations;
        }

        public String getName() {
            return name;
        }

        public boolean isReadOnly() {
            return readOnly;
        }
    }

}
