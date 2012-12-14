/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.txframework.impl.handlers.restat.client;

import org.jboss.jbossts.star.util.TxSupport;

/**
 * @author paul.robinson@redhat.com, 2012-04-12
 */
//todo: this is a quick and dirty naive implementation. Belongs in REST-TX
public class UserTransaction {

    private static final String coordinatorUrl = "http://localhost:8080/rest-tx/tx/transaction-manager";

    private static ThreadLocal<TxSupport> threadTX = new ThreadLocal<TxSupport>();

    public void begin() throws IllegalStateException {

        if (threadTX.get() != null) {
            throw new IllegalStateException("Transaction already running");
        }
        TxSupport txSupport = new TxSupport();
        txSupport.startTx();
        threadTX.set(txSupport);
    }

    public void commit() throws TransactionRolledBackException {

        if (threadTX.get() == null) {
            throw new IllegalStateException("Transaction not running");
        }
        TxSupport txSupport = threadTX.get();
        txSupport.commitTx();

        //todo: check if rolled back and throw TransactionRolledBackException

        threadTX.remove();
    }

    public void rollback() {

        if (threadTX.get() == null) {
            throw new IllegalStateException("Transaction not running");
        }
        TxSupport txSupport = threadTX.get();
        txSupport.rollbackTx();
        threadTX.remove();
    }

    public static TxSupport getTXSupport() {

        return threadTX.get();
    }
}
