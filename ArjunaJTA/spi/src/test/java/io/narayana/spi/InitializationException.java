/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.spi;

/**
 * Exception class for reporting problems starting the transaction service
 */
public class InitializationException extends Exception {
    public InitializationException(String message) {
        super(message);
    }

    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}