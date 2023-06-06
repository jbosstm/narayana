/*
 * SPDX short identifier: Apache-2.0
 */

 package org.jboss.jbossts.star.provider;

/**
 * transaction status exception
 */
public class TransactionStatusException extends RuntimeException {
    public TransactionStatusException(String message) {
        super(message);
    }
}