/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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