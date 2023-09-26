/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

/**
 * Indicates that a compensation-based transaction was not active when the operation was attempted, despite being required.
 *
 * @author paul.robinson@redhat.com 25/04/2013
 */
public class NoTransactionException extends CompensationTransactionRuntimeException {

    public NoTransactionException(String message) {

        super(message);
    }

    public NoTransactionException(String message, Throwable cause) {

        super(message, cause);
    }
}