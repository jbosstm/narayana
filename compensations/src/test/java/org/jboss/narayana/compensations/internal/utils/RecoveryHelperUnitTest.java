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

package org.jboss.narayana.compensations.internal.utils;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RecoveryHelperUnitTest {

    @Mock
    private RecoveryStore recoveryStore;

    @Mock
    private InputObjectState inputObjectState;

    @Mock
    private Consumer<Throwable> exceptionConsumer;

    @Mock
    private Function<Throwable, Throwable> exceptionFunction;

    private String recordType = "test-record-type";

    private Uid uid = new Uid();

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetStates() throws ObjectStoreException, IOException {
        when(recoveryStore.allObjUids(anyObject(), anyObject())).then(i -> {
            i.getArgumentAt(1, InputObjectState.class).copy(getInputObjectState());
            return true;
        });
        assertEquals(uid, UidHelper.unpackFrom(getRecoveryHelper().getStates(exceptionConsumer)));
        verify(exceptionConsumer, times(0)).accept(anyObject());
    }

    @Test
    public void getStatesShouldCallExceptionConsumer() throws ObjectStoreException {
        when(recoveryStore.allObjUids(anyObject(), anyObject())).thenThrow(new ObjectStoreException("test"));
        assertFalse(getRecoveryHelper().getStates(exceptionConsumer).notempty());
        verify(exceptionConsumer, times(1)).accept(anyObject());
    }

    @Test
    public void testGetStatesWithException() throws Throwable {
        when(recoveryStore.allObjUids(anyObject(), anyObject())).then(i -> {
            i.getArgumentAt(1, InputObjectState.class).copy(getInputObjectState());
            return true;
        });
        assertEquals(uid, UidHelper.unpackFrom(getRecoveryHelper().getStatesWithException(exceptionFunction)));
        verify(exceptionFunction, times(0)).apply(anyObject());
    }

    @Test(expected = RuntimeException.class)
    public void getStatesWithExceptionShouldCallExceptionFunction() throws Throwable {
        when(exceptionFunction.apply(anyObject())).thenReturn(new RuntimeException());
        when(recoveryStore.allObjUids(anyObject(), anyObject())).thenThrow(new ObjectStoreException("test"));
        getRecoveryHelper().getStatesWithException(exceptionFunction);
    }

    @Test
    public void shouldGetAllUids() throws ObjectStoreException, IOException {
        when(recoveryStore.allObjUids(anyObject(), anyObject())).then(i -> {
            i.getArgumentAt(1, InputObjectState.class).copy(getInputObjectState());
            return true;
        });
        assertEquals(Collections.singleton(uid), getRecoveryHelper().getAllUids(exceptionConsumer));
        verify(exceptionConsumer, times(0)).accept(anyObject());
    }

    @Test
    public void getAllUidsShouldCallExceptionConsumer() throws ObjectStoreException {
        when(recoveryStore.allObjUids(anyObject(), anyObject())).thenThrow(new ObjectStoreException("test"));
        assertTrue(getRecoveryHelper().getAllUids(exceptionConsumer).isEmpty());
        verify(exceptionConsumer, times(1)).accept(anyObject());
    }

    @Test
    public void testGetAllUidsWithException() throws Throwable {
        when(recoveryStore.allObjUids(anyObject(), anyObject())).then(i -> {
            i.getArgumentAt(1, InputObjectState.class).copy(getInputObjectState());
            return true;
        });
        assertEquals(Collections.singleton(uid), getRecoveryHelper().getAllUidsWithException(exceptionFunction));
        verify(exceptionFunction, times(0)).apply(anyObject());
    }

    @Test(expected = RuntimeException.class)
    public void getAllUidsWithExceptionShouldCallExceptionFunction() throws Throwable {
        when(exceptionFunction.apply(anyObject())).thenReturn(new RuntimeException());
        when(recoveryStore.allObjUids(anyObject(), anyObject())).thenThrow(new ObjectStoreException("test"));
        getRecoveryHelper().getAllUidsWithException(exceptionFunction);
    }

    @Test
    public void shouldGetAllRecords() throws IOException, ObjectStoreException {
        when(recoveryStore.allObjUids(anyObject(), anyObject())).then(i -> {
            i.getArgumentAt(1, InputObjectState.class).copy(getInputObjectState());
            return true;
        });
        when(recoveryStore.read_committed(anyObject(), anyObject())).thenReturn(getInputObjectState());
        Set<InputObjectState> records = getRecoveryHelper().getAllRecords(exceptionConsumer);
        assertEquals(1, records.size());
        assertEquals(uid, UidHelper.unpackFrom(records.iterator().next()));
        verify(exceptionConsumer, times(0)).accept(anyObject());
    }

    @Test
    public void getAllRecordsShouldCallExceptionConsumer() throws ObjectStoreException {
        when(recoveryStore.allObjUids(anyObject(), anyObject())).then(i -> {
            i.getArgumentAt(1, InputObjectState.class).copy(getInputObjectState());
            return true;
        });
        when(recoveryStore.read_committed(anyObject(), anyObject())).thenThrow(new ObjectStoreException("test"));
        assertTrue(getRecoveryHelper().getAllRecords(exceptionConsumer).isEmpty());
        verify(exceptionConsumer, times(1)).accept(anyObject());
    }

    @Test
    public void shouldGetRecord() throws ObjectStoreException, IOException {
        when(recoveryStore.read_committed(anyObject(), anyObject())).thenReturn(getInputObjectState());
        assertEquals(uid, UidHelper.unpackFrom(getRecoveryHelper().getRecord(uid, exceptionConsumer).get()));
        verify(exceptionConsumer, times(0)).accept(anyObject());
    }

    @Test
    public void getRecordShouldCallExceptionConsumer() throws ObjectStoreException {
        when(recoveryStore.read_committed(anyObject(), anyObject())).thenThrow(new ObjectStoreException("test"));
        assertFalse(getRecoveryHelper().getRecord(uid, exceptionConsumer).isPresent());
        verify(exceptionConsumer, times(1)).accept(anyObject());
    }

    @Test
    public void shouldWriteRecord() throws ObjectStoreException {
        when(recoveryStore.write_committed(anyObject(), anyString(), anyObject())).thenReturn(true);
        OutputObjectState outputObjectState = new OutputObjectState(uid, recordType);
        assertTrue(getRecoveryHelper().writeRecord(outputObjectState, exceptionConsumer));
        verify(recoveryStore, times(1)).write_committed(eq(uid), eq(recordType), eq(outputObjectState));
        verify(exceptionConsumer, times(0)).accept(anyObject());
    }

    @Test
    public void writeRecordShouldCallExceptionConsumer() throws ObjectStoreException {
        when(recoveryStore.write_committed(anyObject(), anyString(), anyObject())).thenThrow(new ObjectStoreException("test"));
        assertFalse(getRecoveryHelper().writeRecord(new OutputObjectState(), exceptionConsumer));
        verify(exceptionConsumer, times(1)).accept(anyObject());
    }

    @Test
    public void shouldRemoveRecord() throws ObjectStoreException {
        when(recoveryStore.remove_committed(anyObject(), anyString())).thenReturn(true);
        assertTrue(getRecoveryHelper().removeRecord(uid, exceptionConsumer));
        verify(exceptionConsumer, times(0)).accept(anyObject());
    }

    @Test
    public void removeRecordShouldCallExceptionConsumer() throws ObjectStoreException {
        when(recoveryStore.remove_committed(anyObject(), anyString())).thenThrow(new ObjectStoreException("test"));
        assertFalse(getRecoveryHelper().removeRecord(uid, exceptionConsumer));
        verify(exceptionConsumer, times(1)).accept(anyObject());
    }

    @Test
    public void shouldRemoveRecordWithException() throws Throwable {
        when(recoveryStore.remove_committed(anyObject(), anyString())).thenReturn(true);
        assertTrue(getRecoveryHelper().removeRecordWithException(uid, exceptionFunction));
        verify(exceptionFunction, times(0)).apply(anyObject());
    }

    @Test(expected = RuntimeException.class)
    public void removeRecordWithExceptionShouldCallExceptionFunction() throws Throwable {
        when(recoveryStore.remove_committed(anyObject(), anyString())).thenThrow(new ObjectStoreException("test"));
        getRecoveryHelper().removeRecordWithException(uid, exceptionFunction);
    }

    private RecoveryHelper getRecoveryHelper() {
        return new RecoveryHelper(recoveryStore, recordType);
    }

    private InputObjectState getInputObjectState() throws IOException {
        OutputObjectState outputObjectState = new OutputObjectState();
        UidHelper.packInto(uid, outputObjectState);
        return new InputObjectState(outputObjectState);
    }

}
