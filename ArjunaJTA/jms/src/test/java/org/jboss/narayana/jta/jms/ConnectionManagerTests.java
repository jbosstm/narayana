/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.narayana.jta.jms;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.jms.JMSException;
import jakarta.jms.XAConnection;
import jakarta.jms.XAConnectionFactory;
import jakarta.jms.XASession;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ConnectionManagerTests {

    @Mock
    private XAConnectionFactory xaConnectionFactory;

    @Mock
    private XAConnection xaConnection;

    @Mock
    private XASession xaSession;

    @Mock
    private XAResource xaResource;

    private String user = "testUser";

    private String pass = "testPass";

    private ConnectionManager connectionManager;

    @Before
    public void before() throws JMSException {
        MockitoAnnotations.initMocks(this);
        when(xaConnectionFactory.createXAConnection()).thenReturn(xaConnection);
        when(xaConnectionFactory.createXAConnection(anyString(), anyString())).thenReturn(xaConnection);
        when(xaConnection.createXASession()).thenReturn(xaSession);
        when(xaSession.getXAResource()).thenReturn(xaResource);

        connectionManager = new ConnectionManager(xaConnectionFactory, user, pass);
    }

    @Test
    public void shouldConnectWithoutCredentials() throws XAException, JMSException {
        connectionManager = new ConnectionManager(xaConnectionFactory, null, null);
        connectionManager.connect();
        verify(xaConnectionFactory, times(1)).createXAConnection();
        verify(xaConnectionFactory, times(0)).createXAConnection(anyString(), anyString());
        assertTrue(connectionManager.isConnected());
    }

    @Test
    public void shouldConnectWithCredentials() throws XAException, JMSException {
        connectionManager.connect();
        verify(xaConnectionFactory, times(0)).createXAConnection();
        verify(xaConnectionFactory, times(1)).createXAConnection(anyString(), anyString());
        assertTrue(connectionManager.isConnected());
    }

    @Test
    public void shouldNotConnectWithExistingConnection() throws XAException, JMSException {
        connectionManager.connect();
        connectionManager.connect();
        verify(xaConnectionFactory, times(1)).createXAConnection(anyString(), anyString());
        assertTrue(connectionManager.isConnected());
    }

    @Test(expected = XAException.class)
    public void shouldFailToConnect() throws XAException, JMSException {
        when(xaConnectionFactory.createXAConnection(anyString(), anyString())).thenThrow(new JMSException("test"));
        connectionManager.connect();
    }

    @Test
    public void shouldFailToCreateSession() throws XAException, JMSException {
        when(xaConnection.createXASession()).thenThrow(new JMSException("test"));
        try {
            connectionManager.connect();
            fail("Exception was expected");
        } catch (XAException e) {
            verify(xaConnection, times(1)).close();
            assertFalse(connectionManager.isConnected());
        }
    }

    @Test
    public void shouldDisconnect() throws XAException, JMSException {
        connectionManager.connect();
        connectionManager.disconnect();
        verify(xaConnection, times(1)).close();
        assertFalse(connectionManager.isConnected());
    }

    @Test
    public void shouldFailToDisconnect() throws XAException, JMSException {
        doThrow(new JMSException("test")).when(xaConnection).close();
        connectionManager.connect();
        connectionManager.disconnect();
        verify(xaConnection, times(1)).close();
        assertFalse(connectionManager.isConnected());
    }

    @Test
    public void shouldNotDisconnectWithoutConnection() throws XAException, JMSException {
        connectionManager.disconnect();
        verify(xaConnection, times(0)).close();
        assertFalse(connectionManager.isConnected());
    }

    @Test
    public void shouldAcceptWithoutConnecting() throws XAException, JMSException {
        connectionManager.connect();
        connectionManager.connectAndAccept(XAResource::getTransactionTimeout);
        verify(xaConnectionFactory, times(1)).createXAConnection(anyString(), anyString());
        verify(xaConnection, times(1)).createXASession();
        verify(xaSession, times(1)).getXAResource();
        verify(xaResource, times(1)).getTransactionTimeout();
        assertTrue(connectionManager.isConnected());
    }

    @Test
    public void shouldConnectAndAccept() throws XAException, JMSException {
        connectionManager.connectAndAccept(XAResource::getTransactionTimeout);
        verify(xaConnectionFactory, times(1)).createXAConnection(anyString(), anyString());
        verify(xaConnection, times(1)).createXASession();
        verify(xaConnection, times(1)).close();
        verify(xaSession, times(1)).getXAResource();
        verify(xaResource, times(1)).getTransactionTimeout();
        assertFalse(connectionManager.isConnected());
    }

    @Test
    public void shouldFailToConnectAndNotAccept() throws JMSException, XAException {
        when(xaConnectionFactory.createXAConnection(anyString(), anyString())).thenThrow(new JMSException("test"));
        try {
            connectionManager.connectAndAccept(XAResource::getTransactionTimeout);
            fail("Exception expected");
        } catch (XAException ignored) {
        }
        verify(xaConnectionFactory, times(1)).createXAConnection(anyString(), anyString());
        verify(xaConnection, times(0)).createXASession();
        verify(xaConnection, times(0)).close();
        verify(xaSession, times(0)).getXAResource();
        verify(xaResource, times(0)).getTransactionTimeout();
        assertFalse(connectionManager.isConnected());
    }

    @Test
    public void shouldApplyWithoutConnecting() throws XAException, JMSException {
        connectionManager.connect();
        connectionManager.connectAndApply(XAResource::getTransactionTimeout);
        verify(xaConnectionFactory, times(1)).createXAConnection(anyString(), anyString());
        verify(xaConnection, times(1)).createXASession();
        verify(xaSession, times(1)).getXAResource();
        verify(xaResource, times(1)).getTransactionTimeout();
        assertTrue(connectionManager.isConnected());
    }

    @Test
    public void shouldConnectAndApply() throws XAException, JMSException {
        connectionManager.connectAndApply(XAResource::getTransactionTimeout);
        verify(xaConnectionFactory, times(1)).createXAConnection(anyString(), anyString());
        verify(xaConnection, times(1)).createXASession();
        verify(xaConnection, times(1)).close();
        verify(xaSession, times(1)).getXAResource();
        verify(xaResource, times(1)).getTransactionTimeout();
        assertFalse(connectionManager.isConnected());
    }

    @Test
    public void shouldFailToConnectAndNotApply() throws JMSException, XAException {
        when(xaConnectionFactory.createXAConnection(anyString(), anyString())).thenThrow(new JMSException("test"));
        try {
            connectionManager.connectAndApply(XAResource::getTransactionTimeout);
            fail("Exception expected");
        } catch (XAException ignored) {
        }
        verify(xaConnectionFactory, times(1)).createXAConnection(anyString(), anyString());
        verify(xaConnection, times(0)).createXASession();
        verify(xaConnection, times(0)).close();
        verify(xaSession, times(0)).getXAResource();
        verify(xaResource, times(0)).getTransactionTimeout();
        assertFalse(connectionManager.isConnected());
    }

}
