/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

/**
 * Provides a call-back handler, invoked after the transaction manager logs the confirmation and compensation handlers.
 * <p/>
 * The handler is registered via the org.jboss.narayana.compensations.api.TxLogged annotation.
 * <p/>
 * The 'success' flag indicates whether the logging was successful. 'Success' indicates that the application will later
 * be informed of the outcome of the transaction, via the confirmation and compensation handlers, even if the server crashes
 * and recovery is required.
 * <p/>
 * Critical logic that most be done after transaction logging can be placed in this handler.
 *
 * @author paul.robinson@redhat.com 21/03/2013
 */
public interface TransactionLoggedHandler {

    /**
     * Notification that the transaction has been logged
     *
     * @param success indicates whether the log was succesful or not.
     */
    public void transactionLogged(boolean success);
}