/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jdbc.basic;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.jdbc.ConnectionImple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ConnectionImpleTest {

    @Mock
    private XADataSource xaDataSource;

    @Mock
    private XAConnection xaConnection;

    @Mock
    private ConnectionImple connection;

    @Mock
    private XAResource xaResource;

    @Mock
    private DatabaseMetaData databaseMetaData; // Mocking this in order to get rid of NPE from ConnectionImple.getModifier

    private TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(xaDataSource.getXAConnection()).thenReturn(xaConnection);
        when(xaConnection.getConnection()).thenReturn(connection);
        when(xaConnection.getXAResource()).thenReturn(xaResource);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDriverName()).thenReturn("test");
        when(databaseMetaData.getDriverMajorVersion()).thenReturn(1);
        when(databaseMetaData.getDriverMinorVersion()).thenReturn(1);
        arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("1");
    }

    @After
    public void after() throws SystemException {
        if (transactionManager.getTransaction() != null) {
            transactionManager.rollback();
        }
    }

    @Test
    public void checkIfConnectionIsClosedWithoutTheConnection() throws SQLException {
        assertFalse(getConnectionToTest().isClosed());
        verify(connection, times(1)).isClosed(); // We changed that now when you create a ConnectionImple it creates a JDBC connection so isClosed ends up being called
    }

    @Test
    public void checkIfConnectionIsClosedWithoutTransaction() throws SQLException {
        ConnectionImple connectionToTest = getConnectionToTest();
        connectionToTest.clearWarnings(); // Initialises the connection
        connectionToTest.isClosed();
        verify(connection, times(2)).isClosed();
    }

    @Test
    public void checkIfConnectionIsClosedWithTransaction() throws Exception {
        transactionManager.begin();
        ConnectionImple connectionToTest = getConnectionToTest();
        connectionToTest.clearWarnings(); // Initialises the connection
        connectionToTest.isClosed();
        verify(connection, times(2)).isClosed();
    }

    @Test
    public void checkIfConnectionIsClosedInAfterCompletion() throws Exception {
        transactionManager.begin();
        ConnectionImple connectionToTest = getConnectionToTest();
        connectionToTest.clearWarnings(); // Initialises the connection
        transactionManager.getTransaction().registerSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() { }

            @Override
            public void afterCompletion(int status) {
                try {
                    connectionToTest.isClosed();
                } catch (SQLException e) {
                    fail("Could not check isClosed on connection: " + e.getMessage());
                }
            }
        });
        transactionManager.commit();
        verify(connection, times(2)).isClosed();
    }

    @Test
    public void closeConnectionWithoutTransaction() throws SQLException {
        ConnectionImple connectionToTest = getConnectionToTest();
        connectionToTest.clearWarnings(); // Initialises the connection
        connectionToTest.close();
        verify(connection, times(1)).close();
    }

    @Test
    public void closeConnectionWithTransaction() throws Exception {
        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(xaResource); // Normally driver does this
        ConnectionImple connectionToTest = getConnectionToTest();
        connectionToTest.clearWarnings(); // Initialises the connection
        connectionToTest.close();
        verify(connection, times(1)).close();
        // check that XAResource was enlisted and delisted to transaction
        verify(xaResource, atLeast(1)).start(Mockito.any(), Mockito.anyInt());
        verify(xaResource, atLeast(1)).end(Mockito.any(), Mockito.anyInt());
    }

    @Test
    public void noEnlistNoDelist() throws Exception {
        transactionManager.begin();
        ConnectionImple connectionToTest = getConnectionToTest();
        connectionToTest.clearWarnings(); // Initialises the connection
        connectionToTest.close();
        verify(connection, times(1)).close();
        // check that XAResource is not delisted when not enlisted
        verify(xaResource, times(0)).start(Mockito.any(), Mockito.anyInt());
        verify(xaResource, times(0)).end(Mockito.any(), Mockito.anyInt());
    }

    @Test
    public void closeConnectionInAfterCompletion() throws Exception {
        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(xaResource); // Normally driver does this
        ConnectionImple connectionToTest = getConnectionToTest();
        connectionToTest.clearWarnings(); // Initialises the connection
        transactionManager.getTransaction().registerSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() { }

            @Override
            public void afterCompletion(int status) {
                try {
                    connectionToTest.close();
                } catch (SQLException e) {
                    fail("Could not close the connection: " + e);
                }
            }
        });
        transactionManager.commit();
        verify(connection, times(1)).close();
    }

    private ConnectionImple getConnectionToTest() throws SQLException {
        return new ConnectionImple(null, null, null, null, xaDataSource);
    }

}