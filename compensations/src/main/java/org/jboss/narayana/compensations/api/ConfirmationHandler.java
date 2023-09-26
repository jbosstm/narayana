/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

/**
 * Implementation of confirmation handler logic, to be used to notify work annotated with
 * org.jboss.narayana.compensations.api.TxConfirm that the transaction completed successfully.
 *
 * @author paul.robinson@redhat.com 21/03/2013
 */
public interface ConfirmationHandler {

    public void confirm();
}