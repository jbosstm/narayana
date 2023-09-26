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
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class JmsXAResourceRecoveryHelperTests {

    @Mock
    private ConnectionManager connectionManager;

    private JmsXAResourceRecoveryHelper recoveryHelper;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        recoveryHelper = new JmsXAResourceRecoveryHelper(connectionManager);
    }

    @Test
    public void shouldCreateConnectionAndGetXAResource() throws XAException {
        when(connectionManager.isConnected()).thenReturn(false);

        XAResource[] xaResources = recoveryHelper.getXAResources();

        assertEquals(1, xaResources.length);
        assertThat(xaResources[0], sameInstance(recoveryHelper));
        verify(connectionManager, times(1)).isConnected();
        verify(connectionManager, times(1)).connect();
    }

    @Test
    public void shouldGetXAResourceWithoutConnecting() throws XAException {
        when(connectionManager.isConnected()).thenReturn(true);

        XAResource[] xaResources = recoveryHelper.getXAResources();

        assertEquals(1, xaResources.length);
        assertThat(xaResources[0], sameInstance(recoveryHelper));
        verify(connectionManager, times(1)).isConnected();
        verify(connectionManager, times(0)).connect();
    }

    @Test
    public void shouldFailToCreateConnectionAndNotGetXAResource() throws XAException{
        when(connectionManager.isConnected()).thenReturn(false);
        doThrow(new XAException("test")).when(connectionManager).connect();

        XAResource[] xaResources = recoveryHelper.getXAResources();

        assertEquals(0, xaResources.length);
        verify(connectionManager, times(1)).isConnected();
        verify(connectionManager, times(1)).connect();
    }

    @Test
    public void shouldDelegateRecoverCall() throws XAException {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        verify(connectionManager, times(1)).connectAndApply(anyObject());
        verify(connectionManager, times(0)).disconnect();
    }

    @Test
    public void shouldDelegateRecoverCallAndCloseConnection() throws XAException, JMSException {
        recoveryHelper.recover(XAResource.TMENDRSCAN);
        verify(connectionManager, times(1)).connectAndApply(anyObject());
        verify(connectionManager, times(1)).disconnect();
    }

    @Test
    public void shouldDelegateStartCall() throws XAException {
        recoveryHelper.start(null, 0);
        verify(connectionManager, times(1)).connectAndAccept(anyObject());
    }

    @Test
    public void shouldDelegateEndCall() throws XAException {
        recoveryHelper.end(null, 0);
        verify(connectionManager, times(1)).connectAndAccept(anyObject());
    }

    @Test
    public void shouldDelegatePrepareCall() throws XAException {
        when(connectionManager.connectAndApply(anyObject())).thenReturn(10);
        assertEquals(10, recoveryHelper.prepare(null));
        verify(connectionManager, times(1)).connectAndApply(anyObject());
    }

    @Test
    public void shouldDelegateCommitCall() throws XAException {
        recoveryHelper.commit(null, true);
        verify(connectionManager, times(1)).connectAndAccept(anyObject());
    }

    @Test
    public void shouldDelegateRollbackCall() throws XAException {
        recoveryHelper.rollback(null);
        verify(connectionManager, times(1)).connectAndAccept(anyObject());
    }

    @Test
    public void shouldDelegateIsSameRMCall() throws XAException {
        when(connectionManager.connectAndApply(anyObject())).thenReturn(true);
        assertTrue(recoveryHelper.isSameRM(null));
        verify(connectionManager, times(1)).connectAndApply(anyObject());
    }

    @Test
    public void shouldDelegateForgetCall() throws XAException {
        recoveryHelper.forget(null);
        verify(connectionManager, times(1)).connectAndAccept(anyObject());
    }

    @Test
    public void shouldDelegateGetTransactionTimeoutCall() throws XAException {
        when(connectionManager.connectAndApply(anyObject())).thenReturn(10);
        assertEquals(10, recoveryHelper.getTransactionTimeout());
        verify(connectionManager, times(1)).connectAndApply(anyObject());
    }

    @Test
    public void shouldDelegateSetTransactionTimeoutCall() throws XAException {
        when(connectionManager.connectAndApply(anyObject())).thenReturn(true);
        assertTrue(recoveryHelper.setTransactionTimeout(0));
        verify(connectionManager, times(1)).connectAndApply(anyObject());
    }

}