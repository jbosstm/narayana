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

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.mw.wscf.exceptions.InvalidParticipantException;
import com.arjuna.mw.wscf.model.sagas.exceptions.CompensateFailedException;
import com.arjuna.mw.wscf.model.sagas.participants.Participant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.CompensationsRecoveryException;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;
import org.jboss.narayana.compensations.internal.BAParticipant;
import org.jboss.narayana.compensations.internal.CurrentTransaction;
import org.jboss.narayana.compensations.internal.ParticipantImpl;
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;
import org.jboss.narayana.compensations.internal.recovery.DeserializerHelper;
import org.jboss.narayana.compensations.internal.recovery.local.LocalParticipantRecord;

import java.io.IOException;

/**
 * @author paul.robinson@redhat.com
 * @author gytis@redhat.com
 */
public class LocalParticipant implements BAParticipant, Participant {

    private static final Logger LOGGER = Logger.getLogger(LocalParticipant.class);

    private ParticipantImpl participant;

    private String participantId;

    private String coordinatorId;

    // Needed for recovery
    public LocalParticipant() {

    }

    public LocalParticipant(CompensationHandler compensationHandler, ConfirmationHandler confirmationHandler,
            TransactionLoggedHandler transactionLoggedHandler, CurrentTransaction currentTransaction, String participantId,
            String coordinatorId, CompensationContextStateManager compensationContextStateManager,
            DeserializerHelper deserializerHelper) {
        this.participantId = participantId;
        this.coordinatorId = coordinatorId;
        this.participant = new ParticipantImpl(compensationHandler, confirmationHandler, transactionLoggedHandler,
                currentTransaction.getId(), participantId, compensationContextStateManager, deserializerHelper);
    }

    @Override
    public void confirmCompleted(boolean confirmed) {
        participant.confirmCompleted(confirmed);
    }

    @Override
    public void close() throws InvalidParticipantException {
        try {
            participant.close();
            LocalParticipantRecord.getInstance(this).remove();
        } catch (WrongStateException | SystemException | CompensationsRecoveryException e) {
            throw new InvalidParticipantException("Error closing participant: " + e.getMessage());
        }
    }

    @Override
    public void cancel() throws InvalidParticipantException {
        try {
            participant.cancel();
        } catch (FaultedException | WrongStateException | SystemException e) {
            throw new InvalidParticipantException("Error cancelling participant: " + e.getMessage());
        }
    }

    @Override
    public void compensate() throws CompensateFailedException {
        try {
            participant.compensate();
            LocalParticipantRecord.getInstance(this).remove();
        } catch (FaultedException | WrongStateException | SystemException | CompensationsRecoveryException e) {
            throw new CompensateFailedException("Error compensating participant: " + e.getMessage());
        }
    }

    @Override
    public void forget() {
        //TODO: garbage collect if required
    }

    @Override
    public String id() {
        return participantId;
    }

    public String getCoordinatorId() {
        return coordinatorId;
    }

    @Override
    public boolean save_state(OutputObjectState state) {
        LOGGER.tracef("Persisting state: '%s'", this);
        try {
            state.packString(participantId);
            state.packString(coordinatorId);
        } catch (IOException e) {
            LOGGER.warnf(e, "Failed to persist state");
            return false;
        }

        return participant.saveState(state);
    }

    @Override
    public boolean restore_state(InputObjectState state) {
        try {
            participantId = state.unpackString();
            coordinatorId = state.unpackString();
        } catch (IOException e) {
            LOGGER.warnf(e, "Failed to restore state");
            return false;
        }

        participant = new ParticipantImpl(CompensationContextStateManager.getInstance(), new DeserializerHelper());
        if (!participant.restoreState(state)) {
            return false;
        }

        LOGGER.tracef("Restored state: '%s'", this);

        return true;
    }

    @Override
    public String toString() {
        return "LocalParticipant{participantId=" + participantId + ", coordinatorId=" + coordinatorId + ", participant="
                + participant + "}";
    }

}
