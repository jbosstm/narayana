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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CompensationContextStateUnitTest {

    @Mock
    private DeserializerHelper deserializerHelper;

    private Uid contextId = new Uid();

    private String transactionId = new Uid().stringForm();

    private String anyId = "test-resource-id";

    private String resource = "test-resource";

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullId() {
        new CompensationContextState(null, transactionId, deserializerHelper);
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullTransactionId() {
        new CompensationContextState(contextId, null, deserializerHelper);
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullDeserializerHelper() {
        new CompensationContextState(contextId, transactionId, null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullDeserializerHelperInSingleParameterConstructor() {
        new CompensationContextState(null);
    }

    @Test
    public void shouldGetId() {
        assertEquals(contextId, getTestCompensationContextState().getId());
    }

    @Test
    public void shouldGetTransactionId() {
        assertEquals(transactionId, getTestCompensationContextState().getTransactionId());
    }

    @Test(expected = NullPointerException.class)
    public void addResourceShouldNotAllowNullId() {
        getTestCompensationContextState().addResource(null, resource);
    }

    @Test(expected = NullPointerException.class)
    public void addResourceShouldNotAllowNullResource() {
        getTestCompensationContextState().addResource(anyId, null);
    }

    @Test(expected = NullPointerException.class)
    public void getResourceShouldNotAllowNullId() {
        getTestCompensationContextState().getResource(null);
    }

    @Test(expected = NullPointerException.class)
    public void remoteResourceShouldNotAllowNullId() {
        getTestCompensationContextState().removeResource(null);
    }

    @Test
    public void shouldAddGetAndRemoveResource() {
        CompensationContextState state = getTestCompensationContextState();
        assertNull(state.getResource(anyId));

        state.addResource(anyId, resource);
        assertEquals(resource, state.getResource(anyId));

        state.removeResource(anyId);
        assertNull(state.getResource(anyId));
    }

    @Test(expected = NullPointerException.class)
    public void attachParticipantShouldNotAllowNullId() {
        getTestCompensationContextState().attachParticipant(null);
    }

    @Test(expected = NullPointerException.class)
    public void detachParticipantShouldNotAllowNullId() {
        getTestCompensationContextState().detachParticipant(null);
    }

    @Test
    public void shouldAttachAndDetachParticipant() {
        CompensationContextState state = getTestCompensationContextState();
        assertFalse(state.hasAttachedParticipants());

        state.attachParticipant(anyId);
        assertTrue(state.hasAttachedParticipants());

        state.detachParticipant(anyId);
        assertFalse(state.hasAttachedParticipants());
    }

    @Test(expected = NullPointerException.class)
    public void persistShouldNotAllowNullState() {
        getTestCompensationContextState().persist(null);
    }

    @Test(expected = NullPointerException.class)
    public void restoreShouldNotAllowNullState() {
        getTestCompensationContextState().restore(null);
    }

    /**
     * Restore action should fail because there are no deserializers registered.
     */
    @Test
    public void shouldPersistAndFailToRestore() {
        CompensationContextState state = getTestCompensationContextState();
        state.addResource(anyId, resource);
        state.attachParticipant(anyId);

        OutputObjectState outputObjectState = new OutputObjectState();
        assertTrue(state.persist(outputObjectState));

        when(deserializerHelper.deserialize(anyObject(), anyString(), anyObject())).thenReturn(Optional.empty());
        InputObjectState inputObjectState = new InputObjectState(outputObjectState);
        CompensationContextState restoredState = new CompensationContextState(deserializerHelper);
        assertFalse(restoredState.restore(inputObjectState));
    }

    /**
     * Restore action should succeed because deserializer helper will simulate an existing deserializer.
     */
    @Test
    public void shouldPersistAndRestore() throws IOException, ObjectStoreException {
        when(deserializerHelper.deserialize(any(ObjectInputStream.class), eq(String.class.getName()), eq(Object.class)))
                .thenReturn(Optional.of(resource));
        CompensationContextState state = getTestCompensationContextState();
        state.addResource(anyId, resource);
        state.attachParticipant(anyId);

        OutputObjectState outputObjectState = new OutputObjectState();
        assertTrue(state.persist(outputObjectState));

        InputObjectState inputObjectState = new InputObjectState(outputObjectState);
        CompensationContextState restoredState = new CompensationContextState(deserializerHelper);
        assertTrue(restoredState.restore(inputObjectState));
        assertEquals(state, restoredState);
    }

    private CompensationContextState getTestCompensationContextState() {
        return new CompensationContextState(contextId, transactionId, deserializerHelper);
    }

}