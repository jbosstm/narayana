/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

/**
 * A piece of work that needs to be executed atomically.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public interface CompensatableWork {

    /**
     * A method implementing work which can be confirmed and/or compensated by ConfirmationHandler and CompensationHandler.
     */
    void execute();

}