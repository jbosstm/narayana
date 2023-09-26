/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.api;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransactionRequiredException extends Exception {

    public TransactionRequiredException() {

        super();
    }

    public TransactionRequiredException(String message) {

        super(message);
    }

}