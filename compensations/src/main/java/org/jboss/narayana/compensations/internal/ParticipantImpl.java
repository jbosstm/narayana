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

package org.jboss.narayana.compensations.internal;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.PersistableParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;
import org.jboss.narayana.compensations.internal.recovery.DeserializerHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public class ParticipantImpl implements BusinessAgreementWithParticipantCompletionParticipant, ConfirmCompletedParticipant,
        PersistableParticipant {

    private static final Logger LOGGER = Logger.getLogger(ParticipantImpl.class);

    private final ClassLoader applicationClassloader; // TODO do we need to persist it for recovery?

    private final CompensationContextStateManager compensationContextStateManager;

    private final DeserializerHelper deserializerHelper;

    private CompensationHandler compensationHandler;

    private ConfirmationHandler confirmationHandler;

    private TransactionLoggedHandler transactionLoggedHandler;

    private String currentTransactionId;

    private String participantId;

    public ParticipantImpl(CompensationContextStateManager compensationContextStateManager,
            DeserializerHelper deserializerHelper) {
        this.compensationContextStateManager = compensationContextStateManager;
        this.deserializerHelper = deserializerHelper;
        this.applicationClassloader = Thread.currentThread().getContextClassLoader(); // TODO is this ok?
    }

    public ParticipantImpl(CompensationHandler compensationHandler, ConfirmationHandler confirmationHandler,
            TransactionLoggedHandler transactionLoggedHandler, String currentTransactionId, String participantId,
            CompensationContextStateManager compensationContextStateManager, DeserializerHelper deserializerHelper) {

        this.compensationHandler = compensationHandler;
        this.confirmationHandler = confirmationHandler;
        this.transactionLoggedHandler = transactionLoggedHandler;
        this.currentTransactionId = currentTransactionId;
        this.participantId = participantId;
        this.compensationContextStateManager = compensationContextStateManager;
        this.deserializerHelper = deserializerHelper;
        this.applicationClassloader = Thread.currentThread().getContextClassLoader();

        compensationContextStateManager.getCurrent().attachParticipant(participantId);
    }

    @Override
    public void confirmCompleted(boolean confirmed) {
        if (transactionLoggedHandler != null) {
            ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(applicationClassloader);

            transactionLoggedHandler.transactionLogged(confirmed);

            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }

    @Override
    public void close() throws WrongStateException, SystemException {
        if (confirmationHandler != null) {
            ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(applicationClassloader);
            compensationContextStateManager.activate(currentTransactionId);
            confirmationHandler.confirm();
            compensationContextStateManager.deactivate();
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
        compensationContextStateManager.get(currentTransactionId).ifPresent(state -> state.detachParticipant(participantId));
        compensationContextStateManager.persist(currentTransactionId);
    }

    @Override
    public void cancel() throws FaultedException, WrongStateException, SystemException {
        // TODO: Do nothing?
    }

    @Override
    public void compensate() throws FaultedException, WrongStateException, SystemException {
        if (compensationHandler != null) {
            ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(applicationClassloader);
            compensationContextStateManager.activate(currentTransactionId);
            compensationHandler.compensate();
            compensationContextStateManager.deactivate();
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
        compensationContextStateManager.get(currentTransactionId).ifPresent(state -> state.detachParticipant(participantId));
        compensationContextStateManager.persist(currentTransactionId);
    }

    @Override
    public String status() throws SystemException {
        // TODO: what to do here?
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
    public boolean saveState(OutputObjectState state) {
        LOGGER.tracef("Persisting state: '%s'", this);

        try {
            state.packString(currentTransactionId);
            state.packString(participantId);
            packHandler(state, compensationHandler);
            packHandler(state, confirmationHandler);
        } catch (IOException e) {
            LOGGER.warnf(e, "Failed to persist state");
            return false;
        }

        return true;
    }

    @Override
    public boolean restoreState(InputObjectState state) {
        try {
            currentTransactionId = state.unpackString();
            participantId = state.unpackString();
            compensationHandler = unpackHandler(state, CompensationHandler.class);
            confirmationHandler = unpackHandler(state, ConfirmationHandler.class);
        } catch (IOException e) {
            LOGGER.warnf(e, "Failed to restore state");
            return false;
        }

        LOGGER.tracef("Restored state: '%s'", this);

        return true;
    }

    @Override
    public String toString() {
        return "ParticipantImpl{currentTransactionId='" + currentTransactionId + "', compensationHandler=" + compensationHandler
                + ", confirmationHandler=" + confirmationHandler + ", transactionLoggedHandler=" + transactionLoggedHandler
                + ", applicationClassloader=" + applicationClassloader + "}";
    }

    private <T> boolean isHandlerPersistable(T handler) {
        return handler != null && handler instanceof Serializable;
    }

    private <T> void packHandler(OutputObjectState state, T handler) throws IOException {
        if (!isHandlerPersistable(handler)) {
            LOGGER.warnf("Ignoring a non-serializable handler %s", handler);
            state.packBoolean(false);
            return;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectStream = new ObjectOutputStream(outputStream)) {
            objectStream.writeObject(handler);
            state.packBoolean(true);
            state.packString(handler.getClass().getName());
            state.packBytes(outputStream.toByteArray());
        }

        LOGGER.tracef("Persisted handler: '%s'", handler);
    }

    private <T> T unpackHandler(InputObjectState state, Class<T> clazz) throws IOException {
        if (!state.unpackBoolean()) {
            return null;
        }

        String className = state.unpackString();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(state.unpackBytes());
                ObjectInputStream objectStream = new ObjectInputStream(inputStream)) {
            return deserializerHelper.deserialize(objectStream, className, clazz)
                    .orElseThrow(() -> new IOException("Handler could not be deserialized by any deserializer"));
        }
    }

}
