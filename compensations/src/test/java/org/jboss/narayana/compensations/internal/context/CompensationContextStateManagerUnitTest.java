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

package org.jboss.narayana.compensations.internal.context;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import org.jboss.narayana.compensations.internal.recovery.DeserializerHelper;
import org.jboss.narayana.compensations.internal.utils.RecoveryHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CompensationContextStateManagerUnitTest {

    @Mock
    private RecoveryHelper recoveryHelper;

    @Mock
    private DeserializerHelper deserializerHelper;

    private String transactionId = new Uid().stringForm();

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullRecoveryHelper() {
        new CompensationContextStateManager(null, deserializerHelper);
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullDeserializerHelper() {
        new CompensationContextStateManager(recoveryHelper, null);
    }

    @Test(expected = NullPointerException.class)
    public void activateShouldNotAllowNullTransactionId() {
        getCompensationContextStateManager().activate(null);
    }

    @Test
    public void shouldActivateAndDeactivate() {
        CompensationContextStateManager manager = getCompensationContextStateManager();
        assertFalse(manager.isActive());
        manager.activate(transactionId);
        assertTrue(manager.isActive());
        manager.deactivate();
        assertFalse(manager.isActive());
    }

    @Test(expected = NullPointerException.class)
    public void getShouldNotAllowNullTransactionId() {
        getCompensationContextStateManager().get(null);
    }

    @Test
    public void shouldNotGetNonExistentState() {
        assertFalse(getCompensationContextStateManager().get(transactionId).isPresent());
    }

    @Test
    public void shouldGetSpecificState() {
        CompensationContextStateManager manager = getCompensationContextStateManager();
        manager.activate(transactionId);
        manager.deactivate();
        Optional<CompensationContextState> optionalState = manager.get(transactionId);
        assertTrue(optionalState.isPresent());
        assertEquals(transactionId, optionalState.get().getTransactionId());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotGetCurrentStateWhenInactive() {
        getCompensationContextStateManager().getCurrent();
    }

    @Test
    public void shouldGetCurrentState() {
        CompensationContextStateManager manager = getCompensationContextStateManager();
        manager.activate(transactionId);
        assertEquals(transactionId, manager.getCurrent().getTransactionId());
        manager.deactivate();
    }

    @Test(expected = NullPointerException.class)
    public void persistShouldNotAllowNullTransactionId() {
        getCompensationContextStateManager().persist(null);
    }

    @Test
    public void shouldNotPersistNonExistentState() throws ObjectStoreException {
        getCompensationContextStateManager().persist(transactionId);
        verify(recoveryHelper, times(0)).writeRecord(anyObject(), anyObject());
    }

    @Test
    public void shouldPersist() throws ObjectStoreException {
        CompensationContextStateManager manager = getCompensationContextStateManager();
        manager.activate(transactionId);
        manager.deactivate();
        manager.persist(transactionId);
        verify(recoveryHelper, times(1)).writeRecord(anyObject(), anyObject());
    }

    @Test(expected = NullPointerException.class)
    public void removeShouldNotAllowNullTransactionId() {
        getCompensationContextStateManager().remove(null);
    }

    @Test
    public void shouldNotRemoveNonExistentState() throws ObjectStoreException {
        getCompensationContextStateManager().remove(transactionId);
        verify(recoveryHelper, times(0)).removeRecord(anyObject(), anyObject());
    }

    @Test
    public void shouldRemove() throws ObjectStoreException {
        CompensationContextStateManager manager = getCompensationContextStateManager();
        manager.activate(transactionId);
        CompensationContextState state = manager.getCurrent();
        manager.deactivate();
        manager.remove(transactionId);
        assertFalse(manager.get(transactionId).isPresent());
        verify(recoveryHelper, times(1)).removeRecord(eq(state.getId()), anyObject());
    }

    @Test
    public void shouldRestore() throws IOException, ObjectStoreException {
        CompensationContextState state = new CompensationContextState(new Uid(), transactionId, deserializerHelper);
        OutputObjectState output = new OutputObjectState(state.getId(), CompensationContextState.getRecordType());
        assertTrue(state.persist(output));
        when(recoveryHelper.getAllRecords(anyObject())).thenReturn(Collections.singleton(new InputObjectState(output)));

        CompensationContextStateManager manager = getCompensationContextStateManager();
        manager.restore();
        Optional<CompensationContextState> restoredState = manager.get(transactionId);
        assertTrue(restoredState.isPresent());
        assertEquals(state, restoredState.get());
        verify(recoveryHelper, times(1)).getAllRecords(anyObject());
    }

    private CompensationContextStateManager getCompensationContextStateManager() {
        return new CompensationContextStateManager(recoveryHelper, deserializerHelper);
    }

}
