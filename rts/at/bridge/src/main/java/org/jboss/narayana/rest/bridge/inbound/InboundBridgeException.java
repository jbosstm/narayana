/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.rest.bridge.inbound;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class InboundBridgeException extends RuntimeException {

    public InboundBridgeException() {
        super();
    }

    public InboundBridgeException(final String message) {
        super(message);
    }

    public InboundBridgeException(final String message, final Throwable cause) {
        super(message, cause);
    }

}