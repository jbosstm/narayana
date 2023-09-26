/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms;

import jakarta.jms.JMSException;
import jakarta.transaction.Synchronization;
import javax.transaction.xa.XAResource;

/**
 * Utility class to make transaction status checking and resources registration easier.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public interface TransactionHelper {

    /**
     * Check if transaction is active. If error occurs wrap an original exception with {@link JMSException}.
     *
     * @return whether transaction is active or not.
     * @throws JMSException if transaction service has failed in unexpected way to obtain transaction status
     */
    boolean isTransactionAvailable() throws JMSException;

    /**
     * Register synchronization with a current transaction. If error occurs wrap an original exception with
     * {@link JMSException}.
     * 
     * @param synchronization synchronization to be registered.
     * @throws JMSException if error occurred registering synchronization
     *   that occurs when transaction service fails in an unexpected way
     *   or when the transaction is marked for rollback only
     *   or when transaction is in a state where {@link Synchronization} callbacks cannot be registered
     */
    void registerSynchronization(Synchronization synchronization) throws JMSException;

    /**
     * Enlist XA resource to a current transaction. If error occurs wrap an original exception with {@link JMSException}.
     * 
     * @param xaResource resource to be enlisted.
     * @throws JMSException if error occurred enlisting resource
     *   that occurs when transaction service fails in an unexpected way
     *   or when the transaction is marked for rollback only
     *   or when transaction is in a state where resources cannot be enlisted.
     */
    void registerXAResource(XAResource xaResource) throws JMSException;

    /**
     * Delist XA resource from a current transaction. If error occurs wrap an original exception with {@link JMSException}.
     * 
     * @param xaResource resource to be delisted.
     * @throws JMSException if error occurred delisting resource.
     *   that occurs when transaction service fails in an unexpected way
     *   or when transaction is in a state where resources cannot be delisted.
     */
    void deregisterXAResource(XAResource xaResource) throws JMSException;

}