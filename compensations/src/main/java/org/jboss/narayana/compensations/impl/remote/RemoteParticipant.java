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

package org.jboss.narayana.compensations.impl.remote;

import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;
import org.jboss.narayana.compensations.impl.BAParticipant;
import org.jboss.narayana.compensations.impl.ParticipantImpl;

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
