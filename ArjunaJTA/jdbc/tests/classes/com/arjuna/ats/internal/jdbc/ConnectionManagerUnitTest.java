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

package com.arjuna.ats.internal.jdbc;

import com.arjuna.ats.jdbc.TransactionalDriver;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.XADataSource;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ConnectionManagerUnitTest {

    @Mock
    private Transaction transaction;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private XADataSource xaDataSource;

    @Mock
    private XADataSource otherXaDataSource;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCreateTwoConnectionsWithProvidedDataSourceAndEmptyUrl() throws SQLException, SystemException {
        // Assume that transaction is available
        when(transactionManager.getTransaction()).thenReturn(transaction);
        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManager(transactionManager);

        Properties properties = new Properties();
        properties.put(TransactionalDriver.XADataSource, xaDataSource);

        ConnectionImple connection = (ConnectionImple) ConnectionManager.create(null, properties);
        // Make connection part of the transaction
        ((TransactionalDriverXAConnection) connection.connectionControl()).setTransaction(transaction);
        ConnectionImple otherConnection = (ConnectionImple) ConnectionManager.create(null, properties);

        assertEquals(connection, otherConnection);
    }

    @Test
    public void shouldCreateTwoDifferentConnectionsWithProvidedDataSourceAndEmptyUrl() throws SQLException, SystemException {
        // Assume that transaction is available
        when(transactionManager.getTransaction()).thenReturn(transaction);
        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManager(transactionManager);

        Properties properties = new Properties();
        Properties otherProperties = new Properties();
        properties.put(TransactionalDriver.XADataSource, xaDataSource);
        otherProperties.put(TransactionalDriver.XADataSource, otherXaDataSource);

        ConnectionImple connection = (ConnectionImple) ConnectionManager.create(null, properties);
        // Make connection part of the transaction
        ((TransactionalDriverXAConnection) connection.connectionControl()).setTransaction(transaction);
        ConnectionImple otherConnection = (ConnectionImple) ConnectionManager.create(null, otherProperties);

        assertNotEquals(connection, otherConnection);
    }

}
