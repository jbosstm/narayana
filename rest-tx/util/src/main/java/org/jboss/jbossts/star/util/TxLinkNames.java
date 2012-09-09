package org.jboss.jbossts.star.util;

public class TxLinkNames {
    public static final String LOCATION = "location";

    // Transaction links
    public static final String TERMINATOR = "terminator"; // transaction-terminator URI
    public static final String PARTICIPANT = "durable-participant"; // transaction-enlistment URI
    public static final String VOLATILE_PARTICIPANT = "volatile-participant"; // transaction-enlistment URI

    public static final String STATISTICS = "statistics"; // transaction-statistics URI

    // Two phase aware participants
    public static final String PARTICIPANT_RESOURCE = "participant"; // participant-resource URI
    public static final String PARTICIPANT_TERMINATOR = "terminator"; // participant-terminator URI
    // Two phase unaware participants
    public static final String PARTICIPANT_PREPARE = "prepare"; // participant-prepare URI
    public static final String PARTICIPANT_COMMIT = "commit"; // participant-commit URI
    public static final String PARTICIPANT_ROLLBACK = "rollback"; // participant-rollback URI
    public static final String PARTICIPANT_COMMIT_ONE_PHASE = "commit-one-phase"; // participant-commit-one-phase URI

    // made up links
    public static final String TRANSACTION = "transaction";
    public static final String PARTICIPANT_RECOVERY = "recovery";
}
