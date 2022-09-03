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
package org.jboss.narayana.osgi.jta.internal;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.UserTransaction;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;

public class OsgiTransactionManager extends TransactionManagerImple implements UserTransaction {

    public interface Listener {
        void resumed(Transaction transaction);
        void suspended(Transaction transaction);
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Transaction suspend() throws SystemException {
        Transaction tx = super.suspend();
        if (listener != null) {
            listener.suspended(tx);
        }
        return tx;
    }

    @Override
    public void resume(Transaction which) throws InvalidTransactionException, IllegalStateException, SystemException {
        super.resume(which);
        if (listener != null) {
            listener.resumed(which);
        }
    }
}
