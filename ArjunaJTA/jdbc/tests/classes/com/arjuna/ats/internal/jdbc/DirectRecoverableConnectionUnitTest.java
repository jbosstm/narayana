/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.ats.internal.jdbc;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class DirectRecoverableConnectionUnitTest {

    @Test
    public void shouldGetConnectionWithNullCredentials() throws SQLException {
        DirectRecoverableConnection connection = new DirectRecoverableConnection("testDb", null, null, TestDynamicClass.class.getName(), null);
        XAConnection xaConnection = connection.getConnection();
        assertEquals(TestDynamicClass.lastInstance.getXaConnection(), xaConnection);
        verify(TestDynamicClass.lastInstance.getDataSource("testDb")).getXAConnection();
    }

    @Test
    public void shouldGetConnectionWithEmptyCredentials() throws SQLException {
        DirectRecoverableConnection connection = new DirectRecoverableConnection("testDb", "", "", TestDynamicClass.class.getName(), null);
        XAConnection xaConnection = connection.getConnection();
        assertEquals(TestDynamicClass.lastInstance.getXaConnection(), xaConnection);
        verify(TestDynamicClass.lastInstance.getDataSource("testDb")).getXAConnection();
    }

    @Test
    public void shouldGetConnectionWithCredentials() throws SQLException {
        DirectRecoverableConnection connection = new DirectRecoverableConnection("testDb", "testName", "testPass", TestDynamicClass.class.getName(), null);
        XAConnection xaConnection = connection.getConnection();
        assertEquals(TestDynamicClass.lastInstance.getXaConnection(), xaConnection);
        verify(TestDynamicClass.lastInstance.getDataSource("testDb")).getXAConnection(eq("testName"), eq("testPass"));
    }

    public static class TestDynamicClass implements DynamicClass {

        static TestDynamicClass lastInstance;

        @Mock
        private XADataSource xaDataSource;

        @Mock
        private XAConnection xaConnection;

        public TestDynamicClass() {
            MockitoAnnotations.initMocks(this);
            lastInstance = this;
        }

        @Override
        public XADataSource getDataSource(String dbName) throws SQLException {
            when(xaDataSource.getXAConnection()).thenReturn(xaConnection);
            when(xaDataSource.getXAConnection("testName", "testPass")).thenReturn(xaConnection);
            return xaDataSource;
        }

        public XAConnection getXaConnection() {
            return xaConnection;
        }
    }

}