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
