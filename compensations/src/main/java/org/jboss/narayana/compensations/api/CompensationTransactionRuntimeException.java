/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

/**
 * General RuntimeException used when something unexpected goes wrong.
 *
 * @author paul.robinson@redhat.com 22/04/2013
 */
public class CompensationTransactionRuntimeException extends RuntimeException {

    public CompensationTransactionRuntimeException(String message) {

        super(message);
    }

    public CompensationTransactionRuntimeException(String message, Throwable cause) {

        super(message, cause);
    }
}