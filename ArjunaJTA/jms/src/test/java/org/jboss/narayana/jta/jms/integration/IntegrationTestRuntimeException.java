/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms.integration;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class IntegrationTestRuntimeException extends RuntimeException {

    public IntegrationTestRuntimeException(String message) {
        super(message);
    }

}