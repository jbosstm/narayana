/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

/**
 * Indicates that the compensation-based transaction needed to be compensated, despite being requested to close.
 *
 * @author paul.robinson@redhat.com 25/04/2013
 */
public class TransactionCompensatedException extends CompensationTransactionRuntimeException {

    public TransactionCompensatedException(String message) {

        super(message);
    }

    public TransactionCompensatedException(String message, Throwable cause) {

        super(message, cause);
    }
}