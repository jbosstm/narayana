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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
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
