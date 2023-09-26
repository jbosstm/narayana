/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

/**
 * Management interface for interacting with the compensation-based transaction.
 *
 * @author paul.robinson@redhat.com 21/03/2013
 */
public interface CompensationManager {

    /**
     * Mark the transaction as "compensate only". This ensures that the compensation-based transaction
     * will be cancelled and any completed work compensated.
     */
    public void setCompensateOnly();

}