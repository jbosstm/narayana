/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.iiop.tm;

import jakarta.transaction.Transaction;

import org.omg.CORBA.Current;

/**
 * Interface to be implemented by a CORBA OTS provider for integration with
 * JBossAS. The CORBA OTS provider must (i) create an object that implements
 * this interface and (ii) register an initial reference for that object
 * with the JBossAS ORB, under name "InboundTransactionCurrent".
 * <p/>
 * Step (ii) above should be done by a call
 * <code>orbInitInfo.register_initial_reference</code> within the
 * <code>pre_init</code> method of an
 * <code>org.omg.PortableInterceptor.ORBInitializer</code>,
 * which will probably be also the initializer that registers a server request
 * interceptor for the OTS provider.
 *
 */
public interface InboundTransactionCurrent extends Current {
    String NAME = "InboundTransactionCurrent";

    /**
     * Gets the Transaction instance associated with the current incoming
     * request. This method should be called only by code that handles incoming
     * requests; its return value is undefined in the case of a call issued
     * outside of a request scope.
     *
     * @return the jakarta.transaction.Transaction instance associated with the
     *         current incoming request, or null if that request was not issued
     *         within the scope of some transaction.
     */
    Transaction getCurrentTransaction();

}