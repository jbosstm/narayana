/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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