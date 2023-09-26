/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal.remote;

import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;
import org.jboss.narayana.compensations.internal.BAParticipant;
import org.jboss.narayana.compensations.internal.ParticipantImpl;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public class RemoteParticipant implements BAParticipant, BusinessAgreementWithParticipantCompletionParticipant, ConfirmCompletedParticipant {

    private ParticipantImpl participant;

    public RemoteParticipant(CompensationHandler compensationHandler, ConfirmationHandler confirmationHandler,
            TransactionLoggedHandler transactionLoggedHandler, Object currentTX) {

        participant = new ParticipantImpl(compensationHandler, confirmationHandler, transactionLoggedHandler, currentTX);
    }


    @Override
    public void confirmCompleted(boolean confirmed) {

        participant.confirmCompleted(confirmed);
    }

    @Override
    public void close() throws WrongStateException, SystemException {

        participant.close();
    }

    @Override
    public void cancel() throws FaultedException, WrongStateException, SystemException {
        //TODO: Do nothing?
    }

    @Override
    public void compensate() throws FaultedException, WrongStateException, SystemException {

        participant.compensate();
    }

    @Override
    public String status() throws SystemException {
        //TODO: what to do here?
        return null;
    }

    @Override
    public void unknown() throws SystemException {

    }

    @Override
    public void error() throws SystemException {

    }
}