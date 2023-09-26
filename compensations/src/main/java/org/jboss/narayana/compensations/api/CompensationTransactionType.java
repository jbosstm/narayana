/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

/**
 * Indicates whether a bean method is to be
 * executed within a transaction context where the values provide the following
 * corresponding behavior.
 *
 * @author paul.robinson@redhat.com 21/03/2013
 */
public enum CompensationTransactionType {

    /**
     * <p>If called outside a transaction context, the interceptor must begin a new
     * compensation-based transaction, the managed bean method execution must then continue
     * inside this transaction context, and the transaction must be completed by
     * the interceptor.</p>
     * <p>If called inside a compensation-based transaction context, the managed bean
     * method execution must then continue inside this transaction context.</p>
     */
    REQUIRED,

    /**
     * <p>If called outside a transaction context, the interceptor must begin a new
     * compensation-based transaction, the managed bean method execution must then continue
     * inside this transaction context, and the transaction must be completed by
     * the interceptor.</p>
     * <p>If called inside a compensation-based transaction context, the current transaction context must
     * be suspended, a new compensation-based transaction will begin, the managed bean method
     * execution must then continue inside this transaction context, the transaction
     * must be completed, and the previously suspended transaction must be resumed.</p>
     */
    REQUIRES_NEW,

    /**
     * <p>If called outside a transaction context, a TransactionalException with a
     * nested TransactionRequiredException must be thrown.</p>
     * <p>If called inside a compensation-based transaction context, managed bean method execution will
     * then continue under that context.</p>
     */
    MANDATORY,

    /**
     * <p>If called outside a transaction context, managed bean method execution
     * must then continue outside a transaction context.</p>
     * <p>If called inside a transaction context, the managed bean method execution
     * must then continue inside this transaction context.</p>
     */
    SUPPORTS,

    /**
     * <p>If called outside a transaction context, managed bean method execution
     * must then continue outside a transaction context.</p>
     * <p>If called inside a compensation-based transaction context, the current transaction context must
     * be suspended, the managed bean method execution must then continue
     * outside a transaction context, and the previously suspended transaction
     * must be resumed by the interceptor that suspended it after the method
     * execution has completed.</p>
     */
    NOT_SUPPORTED,

    /**
     * <p>If called outside a transaction context, managed bean method execution
     * must then continue outside a transaction context.</p>
     * <p>If called inside a compensation-based transaction context, a TransactionalException with
     * a nested InvalidTransactionException must be thrown.</p>
     */
    NEVER
}