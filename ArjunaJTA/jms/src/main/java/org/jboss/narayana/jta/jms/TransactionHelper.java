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
import javax.transaction.Synchronization;
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
     * @throws JMSException if error occurred getting transaction status.
     */
    boolean isTransactionAvailable() throws JMSException;

    /**
     * Register synchronization with a current transaction. If error occurs wrap an original exception with
     * {@link JMSException}.
     * 
     * @param synchronization synchronization to be registered.
     * @throws JMSException if error occurred registering synchronization.
     */
    void registerSynchronization(Synchronization synchronization) throws JMSException;

    /**
     * Enlist XA resource to a current transaction. If error occurs wrap an original exception with {@link JMSException}.
     * 
     * @param xaResource resource to be enlisted.
     * @throws JMSException if error occurred enlisting resource.
     */
    void registerXAResource(XAResource xaResource) throws JMSException;

    /**
     * Delist XA resource from a current transaction. If error occurs wrap an original exception with {@link JMSException}.
     * 
     * @param xaResource resource to be delisted.
     * @throws JMSException if error occurred delisting resource.
     */
    void deregisterXAResource(XAResource xaResource) throws JMSException;

}
