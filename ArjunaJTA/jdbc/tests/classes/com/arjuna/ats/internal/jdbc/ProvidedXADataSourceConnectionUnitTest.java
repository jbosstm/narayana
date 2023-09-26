/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.ats.internal.jdbc;

import org.junit.Before;
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
public class ProvidedXADataSourceConnectionUnitTest {

    @Mock
    private ConnectionImple connectionImple;

    @Mock
    private XADataSource xaDataSource;

    @Mock
    private XAConnection xaConnection;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetConnectionWithNullCredentials() throws SQLException {
        when(xaDataSource.getXAConnection()).thenReturn(xaConnection);
        ProvidedXADataSourceConnection connection = new ProvidedXADataSourceConnection("testDb", null, null, xaDataSource, connectionImple);
        assertEquals(xaConnection, connection.getConnection());
        verify(xaDataSource).getXAConnection();
    }

    @Test
    public void shouldGetConnectionWithEmptyCredentials() throws SQLException {
        when(xaDataSource.getXAConnection()).thenReturn(xaConnection);
        ProvidedXADataSourceConnection connection = new ProvidedXADataSourceConnection("testDb", "", "", xaDataSource, connectionImple);
        assertEquals(xaConnection, connection.getConnection());
        verify(xaDataSource).getXAConnection();
    }

    @Test
    public void shouldGetConnectionWithCredentials() throws SQLException {
        when(xaDataSource.getXAConnection(eq("testName"), eq("testPass"))).thenReturn(xaConnection);
        ProvidedXADataSourceConnection connection = new ProvidedXADataSourceConnection("testDb", "testName", "testPass", xaDataSource, connectionImple);
        assertEquals(xaConnection, connection.getConnection());
        verify(xaDataSource).getXAConnection(eq("testName"), eq("testPass"));
    }

}