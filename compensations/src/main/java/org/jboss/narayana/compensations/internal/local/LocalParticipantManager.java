/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.narayana.compensations.internal.local;

import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import org.jboss.narayana.compensations.internal.CurrentTransaction;
import org.jboss.narayana.compensations.internal.ParticipantManager;
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;
import org.jboss.narayana.compensations.internal.recovery.local.LocalParticipantRecord;

import javax.xml.namespace.QName;

/**
 * Manager used to notify local transaction coordinator about local participant actions.
 * 
 * @author paul.robinson@redhat.com 19/04/2014
 * @author gytis@redhat.com
 */
public class LocalParticipantManager implements ParticipantManager {

    private final LocalParticipant participant;

    private final CurrentTransaction transaction;

    private final CompensationContextStateManager compensationContextStateManager;

    /**
     * @param participant participant to be managed.
     * @param transaction transaction to which participant is enlisted.
     * @param compensationContextStateManager context state manager instance.
     */
    public LocalParticipantManager(LocalParticipant participant, CurrentTransaction transaction,
            CompensationContextStateManager compensationContextStateManager) {
        this.participant = participant;
        this.transaction = transaction;
        this.compensationContextStateManager = compensationContextStateManager;
    }

    /**
     * Detach participant from the compensation context and tell compensation context manager to update its recovery record.
     * Then delist participant from the transaction.
     * 
     * @throws Exception
     */
    @Override
    public void exit() throws Exception {
        compensationContextStateManager.getCurrent().detachParticipant(participant.id());
        compensationContextStateManager.persist(transaction.getId());
        CoordinatorManagerFactory.coordinatorManager().delistParticipant(participant.id());
    }

    /**
     * Tell compensation context manager and local participant to persist their recovery records. And then notify local
     * transaction coordinator that participant has completed.
     *
     * @throws Exception
     */
    @Override
    public void completed() throws Exception {
        compensationContextStateManager.persist(transaction.getId());
        LocalParticipantRecord.getInstance(participant).persist();
        CoordinatorManagerFactory.coordinatorManager().participantCompleted(participant.id());
    }

    @Override
    public void cannotComplete() throws Exception {
        CoordinatorManagerFactory.coordinatorManager().participantCannotComplete(participant.id());
    }

    @Override
    public void fail(QName exceptionIdentifier) throws Exception {
        CoordinatorManagerFactory.coordinatorManager().participantFaulted(participant.id());
    }
}
