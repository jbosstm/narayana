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

import com.arjuna.ats.jta.logging.jtaLogger;

import jakarta.jms.JMSException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
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
            jtaLogger.i18NLogger.warn_failed_to_get_transaction_status(e);
            throw getJmsException(jtaLogger.i18NLogger.get_failed_to_get_transaction_status(), e);
        }
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) throws JMSException {
        try {
            getTransaction().registerSynchronization(synchronization);
        } catch (IllegalStateException | RollbackException | SystemException e) {
            jtaLogger.i18NLogger.warn_failed_to_register_synchronization(e);
            throw getJmsException(jtaLogger.i18NLogger.get_failed_to_register_synchronization(), e);
        }
    }

    @Override
    public void registerXAResource(XAResource xaResource) throws JMSException {
        try {
            if (!getTransaction().enlistResource(xaResource)) {
                jtaLogger.i18NLogger.warn_failed_to_enlist_xa_resource(null);
                throw getJmsException(jtaLogger.i18NLogger.get_failed_to_enlist_xa_resource(), null);
            }
        } catch (RollbackException | IllegalStateException | SystemException e) {
            jtaLogger.i18NLogger.warn_failed_to_enlist_xa_resource(e);
            throw getJmsException(jtaLogger.i18NLogger.get_failed_to_enlist_xa_resource(), e);
        }
    }

    @Override
    public void deregisterXAResource(XAResource xaResource) throws JMSException {
        try {
            if (!getTransaction().delistResource(xaResource, XAResource.TMSUCCESS)) {
                jtaLogger.i18NLogger.warn_failed_to_delist_xa_resource(null);
                throw getJmsException(jtaLogger.i18NLogger.get_failed_to_delist_xa_resource(), null);
            }
        } catch (IllegalStateException | SystemException e) {
            jtaLogger.i18NLogger.warn_failed_to_delist_xa_resource(e);
            throw getJmsException(jtaLogger.i18NLogger.get_failed_to_delist_xa_resource(), e);
        }
    }

    private Transaction getTransaction() throws JMSException {
        try {
            return transactionManager.getTransaction();
        } catch (SystemException e) {
            jtaLogger.i18NLogger.warn_failed_to_get_transaction(e);
            throw getJmsException(jtaLogger.i18NLogger.get_failed_to_get_transaction(), e);
        }
    }

    private JMSException getJmsException(String message, Exception cause) {
        JMSException jmsException = new JMSException(message);
        jmsException.setLinkedException(cause);
        return jmsException;
    }

}
