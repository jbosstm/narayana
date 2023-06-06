/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package org.jboss.jbossts.star.util.media.txstatusext;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum(String.class)
public enum TransactionStatusElement {
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