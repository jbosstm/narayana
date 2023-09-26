/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client;

import jakarta.ws.rs.WebApplicationException;

public class IllegalSRAStateException extends WebApplicationException {
    public IllegalSRAStateException(String sraId, String message, Throwable cause) {
        super(String.format("%s: %s", sraId, message), cause);
    }
}