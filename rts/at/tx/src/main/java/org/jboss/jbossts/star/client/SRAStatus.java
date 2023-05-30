/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */
package org.jboss.jbossts.star.client;

public enum SRAStatus {
    TransactionRollbackOnly,
    TransactionRollingBack,
    TransactionRolledBack,
    TransactionCommitting,
    TransactionCommitted,
    TransactionHeuristicRollback,
    TransactionHeuristicCommit,
    TransactionHeuristicHazard,
    TransactionHeuristicMixed,
    TransactionPreparing,
    TransactionPrepared,
    TransactionActive,
    TransactionCommittedOnePhase,
    TransactionReadOnly,
    TransactionStatusNone
}
