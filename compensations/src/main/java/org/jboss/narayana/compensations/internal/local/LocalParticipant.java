/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal.local;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wscf.exceptions.InvalidParticipantException;
import com.arjuna.mw.wscf.model.sagas.exceptions.CompensateFailedException;
import com.arjuna.mw.wscf.model.sagas.participants.Participant;
import com.arjuna.wst.FaultedException;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;
import org.jboss.narayana.compensations.internal.BAParticipant;
import org.jboss.narayana.compensations.internal.ParticipantImpl;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public class LocalParticipant implements BAParticipant, Participant {

    private ParticipantImpl participant;

    private String participantId;

    public LocalParticipant(CompensationHandler compensationHandler, ConfirmationHandler confirmationHandler,
            TransactionLoggedHandler transactionLoggedHandler, Object currentTX, String participantId) {

        participant = new ParticipantImpl(compensationHandler, confirmationHandler, transactionLoggedHandler, currentTX);
        this.participantId = participantId;
    }

    @Override
    public void confirmCompleted(boolean confirmed) {

        participant.confirmCompleted(confirmed);
    }

    @Override
    public void close() throws InvalidParticipantException {

        try {
            participant.close();
        } catch (com.arjuna.wst.WrongStateException e) {
            throw new InvalidParticipantException("Error closing participant: " + e.getMessage());
        } catch (com.arjuna.wst.SystemException e) {
            throw new InvalidParticipantException("Error closing participant: " + e.getMessage());
        }
    }

    @Override
    public void cancel() throws InvalidParticipantException {

        try {
            participant.cancel();
        } catch (FaultedException e) {
            throw new InvalidParticipantException("Error cancelling participant: " + e.getMessage());
        } catch (com.arjuna.wst.WrongStateException e) {
            throw new InvalidParticipantException("Error cancelling participant: " + e.getMessage());
        } catch (com.arjuna.wst.SystemException e) {
            throw new InvalidParticipantException("Error cancelling participant: " + e.getMessage());
        }
    }

    @Override
    public void compensate() throws CompensateFailedException {

        try {
            participant.compensate();
        } catch (FaultedException e) {
            throw new CompensateFailedException("Error compensating participant: " + e.getMessage());
        } catch (com.arjuna.wst.WrongStateException e) {
            throw new CompensateFailedException("Error compensating participant: " + e.getMessage());
        } catch (com.arjuna.wst.SystemException e) {
            throw new CompensateFailedException("Error compensating participant: " + e.getMessage());
        }
    }

    @Override
    public void forget() throws InvalidParticipantException, WrongStateException, SystemException {
        //TODO: garbage collect if required
    }

    @Override
    public String id() throws SystemException {

        return participantId;
    }

    @Override
    public boolean save_state(OutputObjectState os) {

        //TODO: save state
        return true;
    }

    @Override
    public boolean restore_state(InputObjectState os) {

        //TODO: restore state
        return true;
    }
}