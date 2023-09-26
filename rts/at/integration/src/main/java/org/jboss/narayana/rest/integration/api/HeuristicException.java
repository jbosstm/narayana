/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.rest.integration.api;


/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public class HeuristicException extends Exception {

    private static final long serialVersionUID = 5814038435168985598L;

    private final HeuristicType heuristicType;

    public HeuristicException(final HeuristicType heuristicType) {
        super(getMessageText(heuristicType));
        this.heuristicType = heuristicType;
    }

    public HeuristicType getHeuristicType() {
        return heuristicType;
    }

    private static String getMessageText(final HeuristicType heuristicType) {
        final String message = "Heuristic exception was thrown with heuristic type: ";

        switch (heuristicType) {
            case HEURISTIC_COMMIT:
                return message + "heuristic commit";

            case HEURISTIC_HAZARD:
                return message + "heuristic hazard";

            case HEURISTIC_MIXED:
                return message + "heuristic mixed";

            case HEURISTIC_ROLLBACK:
                return message + "heuristic rollback";
        }

        return message + "unknown";
    }

}
