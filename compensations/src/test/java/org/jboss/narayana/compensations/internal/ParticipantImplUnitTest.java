/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;
import org.jboss.narayana.compensations.internal.context.CompensationContextState;
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;
import org.jboss.narayana.compensations.internal.recovery.DeserializerHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TODO restore state with/without handlers
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ParticipantImplUnitTest {

    @Mock
    private CompensationHandler compensationHandler;

    @Mock
    private SerializableCompensationHandler serializableCompensationHandler;

    @Mock
    private ConfirmationHandler confirmationHandler;

    @Mock
    private SerializableConfirmationHandler serializableConfirmationHandler;

    @Mock
    private TransactionLoggedHandler transactionLoggedHandler;

    @Mock
    private CompensationContextStateManager compensationContextStateManager;

    @Mock
    private CompensationContextState compensationContextState;

    @Mock
    private DeserializerHelper deserializerHelper;

    @Mock
    private OutputObjectState outputObjectState; // This doesn't work as expected, but it's good to test IOException

    @Mock
    private InputObjectState inputObjectState;  // This doesn't work as expected, but it's good to test IOException

    private String testTransactionId = new Uid().toString();

    private String testParticipantId = new Uid().toString();

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(compensationContextStateManager.getCurrent()).thenReturn(compensationContextState);
        when(compensationContextStateManager.get(testTransactionId)).thenReturn(Optional.of(compensationContextState));
    }

    @Test
    public void shouldAttachParticipantOnCreation() {
        new ParticipantImpl(null, null, null, testTransactionId, testParticipantId, compensationContextStateManager,
                deserializerHelper);
        verify(compensationContextState, times(1)).attachParticipant(testParticipantId);
    }

    @Test
    public void confirmCompletedShouldLogSuccess() {
        getParticipantImpl(null, null, transactionLoggedHandler).confirmCompleted(true);
        verify(transactionLoggedHandler, times(1)).transactionLogged(true);
    }

    @Test
    public void confirmCompletedShouldLogFailure() {
        getParticipantImpl(null, null, transactionLoggedHandler).confirmCompleted(false);
        verify(transactionLoggedHandler, times(1)).transactionLogged(false);
    }

    @Test
    public void closeWithoutHandlerShouldOnlyRemoveDependant() throws WrongStateException, SystemException {
        getParticipantImpl(null, null, null).close();
        verify(compensationContextState, times(1)).detachParticipant(testParticipantId);
    }

    @Test
    public void closeShouldCallHandler() throws WrongStateException, SystemException {
        getParticipantImpl(null, confirmationHandler, null).close();
        verify(confirmationHandler, times(1)).confirm();
        verify(compensationContextStateManager, times(1)).activate(testTransactionId);
        verify(compensationContextStateManager, times(1)).deactivate();
        verify(compensationContextStateManager, times(1)).persist(testTransactionId);
        verify(compensationContextState, times(1)).detachParticipant(testParticipantId);
    }

    @Test
    public void compensateWithoutHandlerShouldOnlyRemoveDependant() throws WrongStateException, SystemException {
        getParticipantImpl(null, null, null).close();
        verify(compensationContextState, times(1)).detachParticipant(testParticipantId);
    }

    @Test
    public void compensateShouldCallHandler() throws SystemException, FaultedException, WrongStateException {
        getParticipantImpl(compensationHandler, null, null).compensate();
        verify(compensationHandler, times(1)).compensate();
        verify(compensationContextStateManager, times(1)).activate(testTransactionId);
        verify(compensationContextStateManager, times(1)).deactivate();
        verify(compensationContextStateManager, times(1)).persist(testTransactionId);
        verify(compensationContextState, times(1)).detachParticipant(testParticipantId);
    }

    @Test
    public void shouldSaveState() throws IOException, ClassNotFoundException {
        OutputObjectState outputObjectState = new OutputObjectState();
        assertTrue(getParticipantImpl(serializableCompensationHandler, serializableConfirmationHandler, null)
                .saveState(outputObjectState));

        InputObjectState inputObjectState = new InputObjectState(outputObjectState);
        assertEquals(testTransactionId, inputObjectState.unpackString());
        assertEquals(testParticipantId, inputObjectState.unpackString());
        assertTrue(inputObjectState.unpackBoolean());
        assertEquals(serializableCompensationHandler.getClass().getName(), inputObjectState.unpackString());
        assertThat(serializableCompensationHandler, new ReflectionEquals(
                new ObjectInputStream(new ByteArrayInputStream(inputObjectState.unpackBytes())).readObject()));
        assertTrue(inputObjectState.unpackBoolean());
        assertEquals(serializableConfirmationHandler.getClass().getName(), inputObjectState.unpackString());
        assertThat(serializableConfirmationHandler, new ReflectionEquals(
                new ObjectInputStream(new ByteArrayInputStream(inputObjectState.unpackBytes())).readObject()));
    }

    @Test
    public void shouldSaveStateWithNonSerializableHandlers() throws IOException {
        OutputObjectState outputObjectState = new OutputObjectState();
        assertTrue(getParticipantImpl(compensationHandler, confirmationHandler, null).saveState(outputObjectState));

        InputObjectState inputObjectState = new InputObjectState(outputObjectState);
        assertEquals(testTransactionId, inputObjectState.unpackString());
        assertEquals(testParticipantId, inputObjectState.unpackString());
        assertFalse(inputObjectState.unpackBoolean());
        assertFalse(inputObjectState.unpackBoolean());
    }

    @Test
    public void shouldSaveStateWithHandlers() throws IOException {
        OutputObjectState outputObjectState = new OutputObjectState();
        assertTrue(getParticipantImpl(null, null, null).saveState(outputObjectState));

        InputObjectState inputObjectState = new InputObjectState(outputObjectState);
        assertEquals(testTransactionId, inputObjectState.unpackString());
        assertEquals(testParticipantId, inputObjectState.unpackString());
        assertFalse(inputObjectState.unpackBoolean());
        assertFalse(inputObjectState.unpackBoolean());
    }

    @Test
    public void saveStateShouldCatchIOException() {
        assertFalse(getParticipantImpl(null, null, null).saveState(outputObjectState));
    }

    @Test
    public void restoreStateShouldCatchIOException() {
        ParticipantImpl participant = new ParticipantImpl(compensationContextStateManager, deserializerHelper);
        assertFalse(participant.restoreState(inputObjectState));
    }
    
    private ParticipantImpl getParticipantImpl(CompensationHandler compensationHandler, ConfirmationHandler confirmationHandler,
            TransactionLoggedHandler transactionLoggedHandler) {
        return new ParticipantImpl(compensationHandler, confirmationHandler, transactionLoggedHandler, testTransactionId,
                testParticipantId, compensationContextStateManager, deserializerHelper);
    }

    private interface SerializableCompensationHandler extends CompensationHandler, Serializable {
    }

    private interface SerializableConfirmationHandler extends ConfirmationHandler, Serializable {
    }

}
