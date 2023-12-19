/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.client.internal.proxy;

public class InvalidLRAStateException extends Exception {
    InvalidLRAStateException() {
        this("Invalid state");
    }

    InvalidLRAStateException(String s) {
        super(s);
    }

    InvalidLRAStateException(String message, Exception e) {
        super(String.format("%s (%s)", message, e.getMessage()), e);
    }
}
