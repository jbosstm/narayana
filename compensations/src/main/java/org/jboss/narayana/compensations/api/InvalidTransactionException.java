/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.api;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class InvalidTransactionException extends Exception {

    public InvalidTransactionException() {

        super();
    }

    public InvalidTransactionException(String message) {

        super(message);
    }

}