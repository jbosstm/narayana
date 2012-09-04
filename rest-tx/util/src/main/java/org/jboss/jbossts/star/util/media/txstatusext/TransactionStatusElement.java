package org.jboss.jbossts.star.util.media.txstatusext;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

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
