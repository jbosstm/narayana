/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.annotation;

/**
 * The status of a compensator. The status is only valid after the coordinator has told the compensator to
 * complete or compensate. The name value of the enum should be returned by compensator methods marked with
 * the {@link Status} annotation.
 */
public enum ParticipantStatus {
    RollbackOnly,
    RollingBack,
    RolledBack,
    Committing,
    Committed,
    HeuristicRollback,
    HeuristicCommit,
    HeuristicHazard,
    HeuristicMixed,
    Preparing,
    Prepared,
    Active,
    CommittedOnePhase,
    ReadOnly,
    StatusNone
}