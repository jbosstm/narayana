package org.jboss.narayana.rts.lra.annotation;

/**
 * The status of a compensator. The status is only valid after the coordinator has told the compensator to
 * complete or compensate. The name value of the enum should be returned by compensator methods marked with
 * the {@link Status} annotation.
 */
public enum CompensatorStatus {
    Compensating, // the Compensator is currently compensating for the LRA.
    Compensated, //  the Compensator has successfully compensated for the LRA.
    FailedToCompensate, //  the Compensator was not able to compensate for the LRA (and must remember
                        // it could not compensate until such time that it receives a forget message).
    Completing, //  the Compensator is tidying up after being told to complete.
    Completed, //  the Compensator has confirmed.
    FailedToComplete, //  the Compensator was unable to tidy-up.
}
