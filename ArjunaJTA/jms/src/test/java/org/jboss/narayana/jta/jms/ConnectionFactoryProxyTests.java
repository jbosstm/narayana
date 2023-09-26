/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.XAConnection;
import jakarta.jms.XAConnectionFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ConnectionFactoryProxyTests {

    @Mock
    private XAConnectionFactory xaConnectionFactoryMock;

    @Mock
    private XAConnection xaConnectionMock;

    @Mock
    private TransactionHelper transactionHelperMock;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetConnection() throws JMSException {
        when(xaConnectionFactoryMock.createXAConnection()).thenReturn(xaConnectionMock);

        ConnectionFactory factory = new ConnectionFactoryProxy(xaConnectionFactoryMock, transactionHelperMock);
        Connection connection = factory.createConnection();

        assertThat(connection, instanceOf(ConnectionProxy.class));
        verify(xaConnectionFactoryMock, times(1)).createXAConnection();
    }

    @Test
    public void shouldGetConnectionWithCredentials() throws JMSException {
        String username = "testUsername";
        String password = "testPassword";
        when(xaConnectionFactoryMock.createXAConnection(username, password)).thenReturn(xaConnectionMock);

        ConnectionFactory factory = new ConnectionFactoryProxy(xaConnectionFactoryMock, transactionHelperMock);
        Connection connection = factory.createConnection(username, password);

        assertThat(connection, instanceOf(ConnectionProxy.class));
        verify(xaConnectionFactoryMock, times(1)).createXAConnection(username, password);
    }

}