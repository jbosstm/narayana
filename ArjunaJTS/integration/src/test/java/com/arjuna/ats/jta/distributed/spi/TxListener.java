/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package com.arjuna.ats.jta.distributed.spi;

import org.jboss.tm.listener.*;

import javax.transaction.Synchronization;
import java.util.EnumSet;

public class TxListener implements Synchronization, TransactionListener {
    volatile int assoc = 0; // we don't get a listener callback for the initial association
    volatile boolean closed = false;
    volatile int acCalled = 0;
    TransactionListenerRegistry registry;
    volatile int registrationCount = 2;
    boolean hasEvents = false;

    public TxListener(TransactionListenerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void beforeCompletion() {
    }

    @Override
    public void afterCompletion(int status) {
        acCalled++;
        if (assoc == 0) {
            closed = true; // safe to close because the app thread has finished with the txn
        }
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent) {
        hasEvents = true;

        if (transactionEvent.getTypes().contains(EventType.ASSOCIATED)) {
            if (registry != null && registrationCount > 0) {
                // test that callbacks can register listeners
                L2 listener = new L2();

                registrationCount -= 1;

                try {
                    registry.addListener(transactionEvent.getTransaction(), listener, EnumSet.allOf(EventType.class));
                } catch (TransactionTypeNotSupported e) {
                    throw new RuntimeException(e);
                }
            }
            assoc++;
        }

        if (transactionEvent.getTypes().contains(EventType.DISASSOCIATING)) {
            assoc--;
            assert (assoc == 0);
            if (acCalled == 1) {
                closed = true; // safe to close now that the AC has ran
            }
        }
    }

    public void reset() {
        assoc = 1;
        closed = false;
        acCalled = 0;
    }
    public boolean isClosed() {
        return closed;
    }

    /**
     * @return true if the txn has been disassociated and the AC has been called just once
     */
    public boolean shouldDisassoc() {
        return assoc == 0 && singleCallAC();

    }

    public boolean singleCallAC() {
        return acCalled == 1;

    }

    public boolean hasEvents() {
        return hasEvents;
    }

    public void clearEvents() {
        hasEvents = false;
    }

    private static class L2 implements TransactionListener {
        @Override
        public void onEvent(TransactionEvent transactionEvent) {
        }
    }
}
