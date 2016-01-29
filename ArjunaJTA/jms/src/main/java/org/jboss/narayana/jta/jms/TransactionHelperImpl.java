/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.narayana.jta.jms;

import javax.jms.JMSException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransactionHelperImpl implements TransactionHelper {

    private final TransactionManager transactionManager;

    public TransactionHelperImpl(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public boolean isTransactionAvailable() throws JMSException {
        try {
            return transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION;
        } catch (SystemException e) {
            throw new JMSException("Failed to get transaction status: " + e.getMessage());
        }
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) throws JMSException {
        try {
            getTransaction().registerSynchronization(synchronization);
        } catch (Throwable t) {
            throw new JMSException("Failed to register synchronization: " + t.getMessage());
        }
    }

    @Override
    public void enlistResource(XAResource xaResource) throws JMSException {
        try {
            getTransaction().enlistResource(xaResource);
        } catch (Throwable t) {
            throw new JMSException("Failed to enlist XA resource: " + t.getMessage());
        }
    }

    @Override
    public void delistResource(XAResource xaResource) throws JMSException {
        try {
            getTransaction().delistResource(xaResource, XAResource.TMSUCCESS);
        } catch (Throwable t) {
            throw new JMSException("Failed to delist XA resource: " + t.getMessage());
        }
    }

    private Transaction getTransaction() throws JMSException {
        try {
            return transactionManager.getTransaction();
        } catch (SystemException e) {
            throw new JMSException("Failed to get transaction: " + e.getMessage());
        }
    }

}
