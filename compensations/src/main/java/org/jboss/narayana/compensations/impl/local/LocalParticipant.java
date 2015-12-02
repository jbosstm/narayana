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

package org.jboss.narayana.compensations.impl.local;

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
import org.jboss.narayana.compensations.impl.BAParticipant;
import org.jboss.narayana.compensations.impl.ParticipantImpl;

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

    public LocalParticipant(Class<? extends CompensationHandler> compensationHandlerClass,
                            Class<? extends ConfirmationHandler> confirmationHandlerClass,
                            Class<? extends TransactionLoggedHandler> transactionLoggedHandlerClass,
                            Object currentTX, String participantId) {

        participant = new ParticipantImpl(compensationHandlerClass, confirmationHandlerClass, transactionLoggedHandlerClass, currentTX);
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
