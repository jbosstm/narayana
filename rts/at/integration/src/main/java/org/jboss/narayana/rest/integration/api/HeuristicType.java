/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.rest.integration.api;

import org.jboss.jbossts.star.util.TxStatus;

public enum HeuristicType {

    HEURISTIC_ROLLBACK,
    HEURISTIC_COMMIT,
    HEURISTIC_HAZARD,
    HEURISTIC_MIXED;

    public static HeuristicType fromTxStatus(final String status) {
        if (status.equals(TxStatus.TransactionHeuristicCommit.name())) {
            return HeuristicType.HEURISTIC_COMMIT;
        } else if (status.equals(TxStatus.TransactionHeuristicHazard.name())) {
            return HeuristicType.HEURISTIC_HAZARD;
        } else if (status.equals(TxStatus.TransactionHeuristicMixed.name())) {
            return HeuristicType.HEURISTIC_MIXED;
        } else if (status.equals(TxStatus.TransactionHeuristicRollback.name())) {
            return HeuristicType.HEURISTIC_ROLLBACK;
        }

        throw new IllegalArgumentException("TxStatus is not heuristic.");
    }

    public String toTxStatus() {
        switch (this) {
            case HEURISTIC_COMMIT:
                return TxStatus.TransactionHeuristicCommit.name();

            case HEURISTIC_HAZARD:
                return TxStatus.TransactionHeuristicHazard.name();

            case HEURISTIC_MIXED:
                return TxStatus.TransactionHeuristicMixed.name();

            case HEURISTIC_ROLLBACK:
                return TxStatus.TransactionHeuristicRollback.name();
        }

        return null;
    }

}
