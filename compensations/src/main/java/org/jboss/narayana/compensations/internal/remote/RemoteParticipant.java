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

package org.jboss.narayana.compensations.internal.remote;

import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.jbossts.xts.recovery.participant.ba.PersistableBAParticipant;
import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;
import org.jboss.narayana.compensations.internal.BAParticipant;
import org.jboss.narayana.compensations.internal.CurrentTransaction;
import org.jboss.narayana.compensations.internal.ParticipantImpl;
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;
import org.jboss.narayana.compensations.internal.recovery.DeserializerHelper;

/**
 * @author paul.robinson@redhat.com
 * @author gytis@redhat.com
 */
public class RemoteParticipant implements BAParticipant, BusinessAgreementWithParticipantCompletionParticipant,
        ConfirmCompletedParticipant, PersistableBAParticipant {

    private static final Logger LOGGER = Logger.getLogger(RemoteParticipant.class);

    private ParticipantImpl participant;

    // Needed for recovery
    public RemoteParticipant(ParticipantImpl participant) {
        this.participant = participant;
    }

    public RemoteParticipant(CompensationHandler compensationHandler, ConfirmationHandler confirmationHandler,
            TransactionLoggedHandler transactionLoggedHandler, CurrentTransaction currentTransaction, String participantId,
            CompensationContextStateManager compensationContextStateManager, DeserializerHelper deserializerHelper) {

        participant = new ParticipantImpl(compensationHandler, confirmationHandler, transactionLoggedHandler,
                currentTransaction.getId(), participantId, compensationContextStateManager, deserializerHelper);
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

    @Deprecated
    @Override
    public void unknown() throws SystemException {

    }

    @Override
    public void error() throws SystemException {

    }

    @Override
    public byte[] getRecoveryState() throws Exception {
        LOGGER.tracef("Persisting state: '%s'", this);

        OutputObjectState state = new OutputObjectState();
        state.packString(getClass().getSimpleName());

        if (!participant.saveState(state)) {
            throw new Exception("Failed to persist state");
        }

        return state.buffer();
    }
}
