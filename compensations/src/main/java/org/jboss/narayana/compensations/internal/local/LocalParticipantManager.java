/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.compensations.internal.local;

import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import org.jboss.narayana.compensations.internal.ParticipantManager;

import javax.xml.namespace.QName;

/**
 * @author paul.robinson@redhat.com 19/04/2014
 */
public class LocalParticipantManager implements ParticipantManager {

    String participantId;

    public LocalParticipantManager(String participantId) {

        this.participantId = participantId;
    }

    @Override
    public void exit() throws Exception {

        CoordinatorManagerFactory.coordinatorManager().delistParticipant(participantId);
    }

    @Override
    public void completed() throws Exception {

        CoordinatorManagerFactory.coordinatorManager().participantCompleted(participantId);
    }

    @Override
    public void cannotComplete() throws Exception {

        CoordinatorManagerFactory.coordinatorManager().participantCannotComplete(participantId);
    }

    @Override
    public void fail(QName exceptionIdentifier) throws Exception {

        CoordinatorManagerFactory.coordinatorManager().participantFaulted(participantId);
    }
}
