/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

/**
 * Implementation of compensation handler logic, to be used in the case where work annotated with
 * org.jboss.narayana.compensations.api.TxCompensate requires compensation.
 *
 * @author paul.robinson@redhat.com 21/03/2013
 */
public interface CompensationHandler {

    public void compensate();
}