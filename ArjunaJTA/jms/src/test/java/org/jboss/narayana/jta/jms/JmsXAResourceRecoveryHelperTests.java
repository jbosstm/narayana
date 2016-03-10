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
package org.jboss.narayana.jta.jms;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class JmsXAResourceRecoveryHelperTests {

    @Mock
    private XAConnectionFactory xaConnectionFactoryMock;

    @Mock
    private XAConnection xaConnectionMock;

    @Mock
    private XASession xaSessionMock;

    @Mock
    private XAResource xaResourceMock;

    private JmsXAResourceRecoveryHelper recoveryHelper;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(xaConnectionFactoryMock.createXAConnection()).thenReturn(xaConnectionMock);
        when(xaConnectionMock.createXASession()).thenReturn(xaSessionMock);
        when(xaSessionMock.getXAResource()).thenReturn(xaResourceMock);

        recoveryHelper = new JmsXAResourceRecoveryHelper(xaConnectionFactoryMock);
    }

    @Test
    public void shouldCreateConnectionAndGetXAResource() throws JMSException {
        when(xaConnectionFactoryMock.createXAConnection(anyString(), anyString())).thenReturn(xaConnectionMock);
        recoveryHelper = new JmsXAResourceRecoveryHelper(xaConnectionFactoryMock, "username", "password");

        XAResource[] xaResources = recoveryHelper.getXAResources();

        assertEquals(1, xaResources.length);
        assertThat(xaResources[0], sameInstance(recoveryHelper));
        verify(xaConnectionFactoryMock, times(1)).createXAConnection("username", "password");
        verify(xaConnectionMock, times(1)).createXASession();
        verify(xaSessionMock, times(1)).getXAResource();
    }

    @Test
    public void shouldCreateConnectionWithCredentialsAndGetXAResource() throws JMSException {
        XAResource[] xaResources = recoveryHelper.getXAResources();

        assertEquals(1, xaResources.length);
        assertThat(xaResources[0], sameInstance(recoveryHelper));
        verify(xaConnectionFactoryMock, times(1)).createXAConnection();
        verify(xaConnectionMock, times(1)).createXASession();
        verify(xaSessionMock, times(1)).getXAResource();
    }

    @Test
    public void shouldFailToCreateConnectionAndNotGetXAResource() throws JMSException {
        when(xaConnectionMock.createXASession()).thenThrow(new JMSException("Test exception"));

        XAResource[] xaResources = recoveryHelper.getXAResources();

        assertEquals(0, xaResources.length);
        verify(xaConnectionFactoryMock, times(1)).createXAConnection();
        verify(xaConnectionMock, times(1)).createXASession();
        verify(xaSessionMock, times(0)).getXAResource();
    }

    @Test
    public void shouldDelegateRecoverCall() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);

        verify(xaResourceMock, times(1)).recover(XAResource.TMSTARTRSCAN);
    }

    @Test
    public void shouldDelegateRecoverCallAndCloseConnection() throws XAException, JMSException {
        recoveryHelper.getXAResources();
        recoveryHelper.recover(XAResource.TMENDRSCAN);

        verify(xaResourceMock, times(1)).recover(XAResource.TMENDRSCAN);
        verify(xaConnectionMock, times(1)).close();
    }

    @Test
    public void shouldDelegateStartCall() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.start(null, 0);

        verify(xaResourceMock, times(1)).start(null, 0);
    }

    @Test
    public void shouldDelegateEndCall() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.end(null, 0);

        verify(xaResourceMock, times(1)).end(null, 0);
    }

    @Test
    public void shouldDelegatePrepareCall() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.prepare(null);

        verify(xaResourceMock, times(1)).prepare(null);
    }

    @Test
    public void shouldDelegateCommitCall() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.commit(null, true);

        verify(xaResourceMock, times(1)).commit(null, true);
    }

    @Test
    public void shouldDelegateRollbackCall() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.rollback(null);

        verify(xaResourceMock, times(1)).rollback(null);
    }

    @Test
    public void shouldDelegateIsSameRMCall() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.isSameRM(null);

        verify(xaResourceMock, times(1)).isSameRM(null);
    }

    @Test
    public void shouldDelegateForgetCall() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.forget(null);

        verify(xaResourceMock, times(1)).forget(null);
    }

    @Test
    public void shouldDelegateGetTransactionTimeoutCall() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.getTransactionTimeout();

        verify(xaResourceMock, times(1)).getTransactionTimeout();
    }

    @Test
    public void shouldDelegateSetTransactionTimeoutCall() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.setTransactionTimeout(0);

        verify(xaResourceMock, times(1)).setTransactionTimeout(0);
    }

}
