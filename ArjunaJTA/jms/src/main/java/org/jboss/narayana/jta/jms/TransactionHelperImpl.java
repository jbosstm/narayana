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

import org.jboss.logging.Logger;

import javax.jms.JMSException;
import javax.transaction.RollbackException;
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

    private static final Logger LOGGER = Logger.getLogger(TransactionHelperImpl.class);

    private final TransactionManager transactionManager;

    public TransactionHelperImpl(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public boolean isTransactionAvailable() throws JMSException {
        try {
            return transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION;
        } catch (SystemException e) {
            LOGGER.warn("Failed to get transaction status", e);
            throw getJmsException("Failed to get transaction status", e);
        }
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) throws JMSException {
        try {
            getTransaction().registerSynchronization(synchronization);
        } catch (IllegalStateException | RollbackException | SystemException e) {
            LOGGER.warn("Failed to register synchronization", e);
            throw getJmsException("Failed to register synchronization", e);
        }
    }

    @Override
    public void registerXAResource(XAResource xaResource) throws JMSException {
        try {
            if (!getTransaction().enlistResource(xaResource)) {
                LOGGER.warn("Failed to enlist XA resource");
                throw getJmsException("Failed to enlist XA resource", null);
            }
        } catch (RollbackException | IllegalStateException | SystemException e) {
            LOGGER.warn("Failed to enlist XA resource", e);
            throw getJmsException("Failed to enlist XA resource", e);
        }
    }

    @Override
    public void deregisterXAResource(XAResource xaResource) throws JMSException {
        try {
            if (!getTransaction().delistResource(xaResource, XAResource.TMSUCCESS)) {
                LOGGER.warn("Failed to delist XA resource");
                throw getJmsException("Failed to delist XA resource", null);
            }
        } catch (IllegalStateException | SystemException e) {
            LOGGER.warn("Failed to delist XA resource", e);
            throw getJmsException("Failed to delist XA resource", e);
        }
    }

    private Transaction getTransaction() throws JMSException {
        try {
            return transactionManager.getTransaction();
        } catch (SystemException e) {
            LOGGER.warn("Failed to get transaction", e);
            throw getJmsException("Failed to get transaction", e);
        }
    }

    private JMSException getJmsException(String message, Exception cause) {
        JMSException jmsException = new JMSException(message);
        jmsException.setLinkedException(cause);
        return jmsException;
    }

}
