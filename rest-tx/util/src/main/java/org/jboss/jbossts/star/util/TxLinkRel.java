package org.jboss.jbossts.star.util;

import java.util.HashMap;
import java.util.Map;

public enum TxLinkRel {

    LOCATION("location"),

    // Transaction links
    TERMINATOR("terminator"), // transaction-terminator URI
    PARTICIPANT("durable-participant"), // transaction-enlistment URI
    VOLATILE_PARTICIPANT("volatile-participant"), // transaction-enlistment URI

    STATISTICS("statistics"), // transaction-statistics URI

    // Two phase aware participants
    PARTICIPANT_RESOURCE("participant"), // participant-resource URI
    PARTICIPANT_TERMINATOR("terminator"), // participant-terminator URI
    // Two phase unaware participants
    PARTICIPANT_PREPARE("prepare"), // participant-prepare URI
    PARTICIPANT_COMMIT("commit"), // participant-commit URI
    PARTICIPANT_ROLLBACK("rollback"), // participant-rollback URI
    PARTICIPANT_COMMIT_ONE_PHASE("commit-one-phase"), // participant-commit-one-phase URI

    // made up links
    TRANSACTION("transaction"),
    PARTICIPANT_RECOVERY("recovery");

    private static Map<String, TxLinkRel> linkRelMap = new HashMap<String, TxLinkRel>();

    static {
        linkRelMap.put("terminator", TERMINATOR);
        linkRelMap.put("durable-participant", PARTICIPANT);
        linkRelMap.put("volatile-participant", VOLATILE_PARTICIPANT);
        linkRelMap.put("statistics", STATISTICS);
        linkRelMap.put("participant", PARTICIPANT_RESOURCE);
        linkRelMap.put("terminator", PARTICIPANT_TERMINATOR); // two instances of terminator
        linkRelMap.put("prepare", PARTICIPANT_PREPARE);
        linkRelMap.put("commit", PARTICIPANT_COMMIT);
        linkRelMap.put("rollback", PARTICIPANT_ROLLBACK);
        linkRelMap.put("commit-one-phase", PARTICIPANT_COMMIT_ONE_PHASE);
        linkRelMap.put("transaction", TRANSACTION);
        linkRelMap.put("recovery", PARTICIPANT_RECOVERY);

    }

    private String linkName;

    TxLinkRel(String linkName) {
        this.linkName = linkName;
    }

    public String linkName() {
        return linkName;
    }

    public static TxLinkRel fromLinkName(String linkName) {
       return linkRelMap.get(linkName.toLowerCase());
    }
}
