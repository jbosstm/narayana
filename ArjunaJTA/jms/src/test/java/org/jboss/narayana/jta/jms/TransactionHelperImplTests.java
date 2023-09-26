/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.jms.JMSException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransactionHelperImplTests {

    @Mock
    private TransactionManager transactionManagerMock;

    @Mock
    private Transaction transactionMock;

    @Mock
    private Synchronization synchronizationMock;

    @Mock
    private XAResource xaResourceMock;

    private TransactionHelper transactionHelper;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        transactionHelper = new TransactionHelperImpl(transactionManagerMock);
    }

    @Test
    public void transactionShouldBeAvailable() throws Exception {
        when(transactionManagerMock.getStatus()).thenReturn(Status.STATUS_ACTIVE);

        assertTrue(transactionHelper.isTransactionAvailable());
        verify(transactionManagerMock, times(1)).getStatus();
    }

    @Test
    public void transactionShouldNotBeAvailable() throws Exception {
        when(transactionManagerMock.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);

        assertFalse(transactionHelper.isTransactionAvailable());
        verify(transactionManagerMock, times(1)).getStatus();
    }

    @Test(expected = JMSException.class)
    public void shouldFailToCheckTransactionAvailability() throws Exception {
        when(transactionManagerMock.getStatus()).thenThrow(new SystemException());

        transactionHelper.isTransactionAvailable();
    }

    @Test
    public void shouldRegisterSynchronization() throws Exception {
        when(transactionManagerMock.getTransaction()).thenReturn(transactionMock);

        transactionHelper.registerSynchronization(synchronizationMock);
        verify(transactionManagerMock, times(1)).getTransaction();
        verify(transactionMock, times(1)).registerSynchronization(synchronizationMock);
    }

    @Test(expected = JMSException.class)
    public void shouldFailToRegisterSynchronization() throws Exception {
        when(transactionManagerMock.getTransaction()).thenReturn(transactionMock);
        doThrow(new IllegalStateException()).when(transactionMock).registerSynchronization(any(Synchronization.class));

        transactionHelper.registerSynchronization(synchronizationMock);
    }

    @Test
    public void shouldRegisterXAResource() throws Exception {
        when(transactionManagerMock.getTransaction()).thenReturn(transactionMock);
        when(transactionMock.enlistResource(any(XAResource.class))).thenReturn(true);

        transactionHelper.registerXAResource(xaResourceMock);
        verify(transactionManagerMock, times(1)).getTransaction();
        verify(transactionMock, times(1)).enlistResource(xaResourceMock);
    }

    @Test(expected = JMSException.class)
    public void shouldFailToRegisterXAResource() throws Exception {
        when(transactionManagerMock.getTransaction()).thenReturn(transactionMock);
        when(transactionMock.enlistResource(any(XAResource.class))).thenReturn(false);

        transactionHelper.registerXAResource(xaResourceMock);
    }

    @Test(expected = JMSException.class)
    public void shouldFailToRegisterXAResourceWithException() throws Exception {
        when(transactionManagerMock.getTransaction()).thenReturn(transactionMock);
        when(transactionMock.enlistResource(any(XAResource.class))).thenThrow(new IllegalStateException());

        transactionHelper.registerXAResource(xaResourceMock);
    }

    @Test
    public void shouldDeregisterXAResource() throws Exception {
        when(transactionManagerMock.getTransaction()).thenReturn(transactionMock);
        when(transactionMock.delistResource(any(XAResource.class), anyInt())).thenReturn(true);

        transactionHelper.deregisterXAResource(xaResourceMock);
        verify(transactionManagerMock, times(1)).getTransaction();
        verify(transactionMock, times(1)).delistResource(xaResourceMock, XAResource.TMSUCCESS);
    }

    @Test(expected = JMSException.class)
    public void shouldFailToDeregisterXAResource() throws Exception {
        when(transactionManagerMock.getTransaction()).thenReturn(transactionMock);
        when(transactionMock.delistResource(any(XAResource.class), anyInt())).thenReturn(false);

        transactionHelper.deregisterXAResource(xaResourceMock);
    }

    @Test(expected = JMSException.class)
    public void shouldFailToDeregisterXAResourceWithException() throws Exception {
        when(transactionManagerMock.getTransaction()).thenReturn(transactionMock);
        when(transactionMock.delistResource(any(XAResource.class), anyInt())).thenThrow(new IllegalStateException());

        transactionHelper.deregisterXAResource(xaResourceMock);
    }

}