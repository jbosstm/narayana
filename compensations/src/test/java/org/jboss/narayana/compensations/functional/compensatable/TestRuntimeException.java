/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.functional.compensatable;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestRuntimeException extends RuntimeException {

    public TestRuntimeException() {

        super();
    }

    public TestRuntimeException(String message) {

        super(message);
    }

}