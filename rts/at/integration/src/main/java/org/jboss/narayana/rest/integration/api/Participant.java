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
public interface Participant {

    Vote prepare() throws ParticipantException;

    void commit() throws ParticipantException, HeuristicException;

    void commitOnePhase() throws ParticipantException;

    void rollback() throws ParticipantException, HeuristicException;

}
