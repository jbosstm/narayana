/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package com.hp.mwtests.ts.jdbc.basic;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.jdbc.ConnectionImple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
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
        verify(connection, times(0)).isClosed();
    }

    @Test
    public void checkIfConnectionIsClosedWithoutTransaction() throws SQLException {
        ConnectionImple connectionToTest = getConnectionToTest();
        connectionToTest.clearWarnings(); // Initialises the connection
        connectionToTest.isClosed();
        verify(connection, times(1)).isClosed();
    }

    @Test
    public void checkIfConnectionIsClosedWithTransaction() throws Exception {
        transactionManager.begin();
        ConnectionImple connectionToTest = getConnectionToTest();
        connectionToTest.clearWarnings(); // Initialises the connection
        connectionToTest.isClosed();
        verify(connection, times(1)).isClosed();
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
        verify(connection, times(1)).isClosed();
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
