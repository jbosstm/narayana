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
