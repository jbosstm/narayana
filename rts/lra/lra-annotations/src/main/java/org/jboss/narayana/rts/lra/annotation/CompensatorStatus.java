package org.jboss.narayana.rts.lra.annotation;

/**
 * The status of a compensator. The status is only valid after the coordinator has told the compensator to
 * complete or compensate. The name value of the enum should be returned by compensator methods marked with
 * the {@link Status} annotation.
 */
public enum CompensatorStatus {
    Compensating, // the Compensator is currently compensating for the jfdi.
    Compensated, //  the Compensator has successfully compensated for the jfdi.
    FailedToCompensate, //  the Compensator was not able to compensate for the jfdi. It must maintain information about the work it was to compensate until the org.jboss.narayana.rts.lra.coordinator sends it a forget message.
    Completing, //  the Compensator is tidying up after being told to complete.
    Completed, //  the org.jboss.narayana.rts.lra.coordinator/participant has confirmed.
    FailedToComplete, //  the Compensator was unable to tidy-up.
}
