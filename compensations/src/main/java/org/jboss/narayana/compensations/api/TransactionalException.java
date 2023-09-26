/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.api;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransactionalException extends RuntimeException {

    public TransactionalException(String message, Throwable cause) {

        super(message, cause);
    }

}