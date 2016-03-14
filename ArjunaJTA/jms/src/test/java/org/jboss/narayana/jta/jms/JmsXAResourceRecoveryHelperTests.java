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
    public void shouldGetXAResource() throws Exception {
        XAResource[] xaResources = recoveryHelper.getXAResources();

        assertEquals(1, xaResources.length);
        assertThat(xaResources[0], sameInstance(recoveryHelper));
    }

    @Test
    public void shouldCreateConnectionOnScanStart() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        verifyCreatedConnection();
    }

    @Test
    public void shouldCreateConnectionWithCredentialsOnScanStart() throws Exception {
        when(xaConnectionFactoryMock.createXAConnection(anyString(), anyString())).thenReturn(xaConnectionMock);
        recoveryHelper = new JmsXAResourceRecoveryHelper(xaConnectionFactoryMock, "userName", "password");

        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        verifyCreatedConnection("userName", "password");
    }

    @Test
    public void shouldCloseConnectionOnScanEnd() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        recoveryHelper.recover(XAResource.TMENDRSCAN);
        verifyCreatedConnection();
        verify(xaResourceMock, times(1)).recover(XAResource.TMENDRSCAN);
        verify(xaConnectionMock, times(1)).close();
    }

    @Test
    public void shouldRecover() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        recoveryHelper.recover(XAResource.TMRESUME);
        verifyCreatedConnection();
        verify(xaResourceMock, times(1)).recover(XAResource.TMRESUME);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailRecover() throws XAException {
        recoveryHelper.recover(XAResource.TMRESUME);
    }

    @Test
    public void shouldStart() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        recoveryHelper.start(null, 0);
        verifyCreatedConnection();
        verify(xaResourceMock, times(1)).start(null, 0);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailStart() throws Exception {
        recoveryHelper.start(null, 0);
    }

    @Test
    public void shouldEnd() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        recoveryHelper.end(null, 0);
        verifyCreatedConnection();
        verify(xaResourceMock, times(1)).end(null, 0);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailEnd() throws Exception {
        recoveryHelper.end(null, 0);
    }

    @Test
    public void shouldPrepare() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        recoveryHelper.prepare(null);
        verifyCreatedConnection();
        verify(xaResourceMock, times(1)).prepare(null);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailPrepare() throws Exception {
        recoveryHelper.prepare(null);
    }

    @Test
    public void shouldCommit() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        recoveryHelper.commit(null, true);
        verifyCreatedConnection();
        verify(xaResourceMock, times(1)).commit(null, true);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailCommit() throws Exception {
        recoveryHelper.commit(null, true);
    }

    @Test
    public void shouldRollback() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        recoveryHelper.rollback(null);
        verifyCreatedConnection();
        verify(xaResourceMock, times(1)).rollback(null);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailRollback() throws Exception {
        recoveryHelper.rollback(null);
    }

    @Test
    public void shouldCheckSameRM() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        recoveryHelper.isSameRM(null);
        verifyCreatedConnection();
        verify(xaResourceMock, times(1)).isSameRM(null);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailSameRMCheck() throws Exception {
        recoveryHelper.isSameRM(null);
    }

    @Test
    public void shouldForget() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        recoveryHelper.forget(null);
        verifyCreatedConnection();
        verify(xaResourceMock, times(1)).forget(null);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailForget() throws Exception {
        recoveryHelper.forget(null);
    }

    @Test
    public void shouldGetTransactionTimeout() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        recoveryHelper.getTransactionTimeout();
        verifyCreatedConnection();
        verify(xaResourceMock, times(1)).getTransactionTimeout();
    }

    @Test(expected = AssertionError.class)
    public void shouldFailGetTransactionTimeout() throws Exception {
        recoveryHelper.getTransactionTimeout();
    }

    @Test
    public void shouldSetTransactionTimeout() throws Exception {
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        recoveryHelper.setTransactionTimeout(0);
        verifyCreatedConnection();
        verify(xaResourceMock, times(1)).setTransactionTimeout(0);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailSetTransactionTimeout() throws Exception {
        recoveryHelper.setTransactionTimeout(0);
    }

    private void verifyCreatedConnection() throws Exception {
        verifyCreatedConnection(null, null);
    }

    private void verifyCreatedConnection(String user, String pass) throws Exception {
        if (user == null && pass == null) {
            verify(xaConnectionFactoryMock, times(1)).createXAConnection();
        } else {
            verify(xaConnectionFactoryMock, times(1)).createXAConnection(user, pass);
        }

        verify(xaConnectionMock, times(1)).createXASession();
        verify(xaSessionMock, times(1)).getXAResource();
        verify(xaResourceMock, times(1)).recover(XAResource.TMSTARTRSCAN);
    }

}
