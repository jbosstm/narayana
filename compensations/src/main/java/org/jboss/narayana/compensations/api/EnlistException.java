/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

/**
 * Exception thrown if participant enlistment fails.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class EnlistException extends Exception {

    public EnlistException(String message, Throwable cause) {
        super(message, cause);
    }

}