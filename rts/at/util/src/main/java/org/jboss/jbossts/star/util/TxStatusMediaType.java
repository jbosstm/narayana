/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package org.jboss.jbossts.star.util;

public class TxStatusMediaType extends TxMediaType {
    public static final String STATUS_PROPERTY = "txstatus";

    public static final String TX_ACTIVE = toMediaType(TxStatus.TransactionActive);
    public static final String TX_PREPARED = toMediaType(TxStatus.TransactionPrepared);
    public static final String TX_COMMITTED = toMediaType(TxStatus.TransactionCommitted);
    public static final String TX_ROLLEDBACK = toMediaType(TxStatus.TransactionRolledBack);
    public static final String TX_ROLLBACK_ONLY = toMediaType(TxStatus.TransactionRollbackOnly);
    public static final String TX_COMMITTED_ONE_PHASE = toMediaType(TxStatus.TransactionCommittedOnePhase);
    public static final String TX_H_MIXED = toMediaType(TxStatus.TransactionHeuristicMixed);
    public static final String TX_H_ROLLBACK = toMediaType(TxStatus.TransactionHeuristicRollback);

    public static String toMediaType(TxStatus txStatus) {
        return new StringBuilder(TxStatusMediaType.STATUS_PROPERTY).append('=').append(txStatus.name()).toString();
    }

}