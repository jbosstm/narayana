/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: accessor.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class SimplePooledDynamicDataSourceJDBCAccess extends DynamicDataSourceJDBCAccess {
    private List<WrappedConnection> unallocatedConnections = new ArrayList<WrappedConnection>();
    private List<WrappedConnection> allocatedConnections = new ArrayList<WrappedConnection>();

    @Override
    public void finalize() {
        for (WrappedConnection connection : unallocatedConnections) {
            try {
                connection.closeImpl();
            } catch (SQLException e) {
            }
        }
        unallocatedConnections.clear();
    }

    public Connection getConnection() throws SQLException {
        WrappedConnection connection;
        synchronized (unallocatedConnections) {
            if (unallocatedConnections.isEmpty()) {
                Connection connectionImpl = super.getConnection();
                unallocatedConnections.add(new WrappedConnection(connectionImpl));
            }

            connection = unallocatedConnections.remove(0);
            allocatedConnections.add(connection);
        }
        return connection;
    }

    private class WrappedConnection implements Connection {
        private final Connection connectionImpl;

        public WrappedConnection(Connection connectionImpl) {
            this.connectionImpl = connectionImpl;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return connectionImpl.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return connectionImpl.isWrapperFor(iface);
        }

        @Override
        public Statement createStatement() throws SQLException {
            return connectionImpl.createStatement();
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return connectionImpl.prepareStatement(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return connectionImpl.prepareCall(sql);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return connectionImpl.nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            connectionImpl.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return connectionImpl.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            connectionImpl.commit();
        }

        @Override
        public void rollback() throws SQLException {
            connectionImpl.rollback();
        }

        @Override
        public void close() throws SQLException {
            allocatedConnections.remove(this);
            unallocatedConnections.add(this);
        }

        @Override
        public boolean isClosed() throws SQLException {
            return connectionImpl.isClosed();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return connectionImpl.getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            connectionImpl.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return connectionImpl.isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            connectionImpl.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return connectionImpl.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            connectionImpl.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return connectionImpl.getTransactionIsolation();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return connectionImpl.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            connectionImpl.clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return connectionImpl.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return connectionImpl.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return connectionImpl.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return connectionImpl.getTypeMap();
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            connectionImpl.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            connectionImpl.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return connectionImpl.getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return connectionImpl.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return connectionImpl.setSavepoint(name);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            connectionImpl.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            connectionImpl.releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return connectionImpl.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return connectionImpl.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return connectionImpl.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return connectionImpl.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return connectionImpl.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return connectionImpl.prepareStatement(sql, columnNames);
        }

        @Override
        public Clob createClob() throws SQLException {
            return connectionImpl.createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {
            return connectionImpl.createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return connectionImpl.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return connectionImpl.createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return connectionImpl.isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            connectionImpl.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            connectionImpl.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return connectionImpl.getClientInfo(name);
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return connectionImpl.getClientInfo();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return connectionImpl.createArrayOf(typeName, elements);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return connectionImpl.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            connectionImpl.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return connectionImpl.getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException {
            connectionImpl.abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            connectionImpl.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return connectionImpl.getNetworkTimeout();
        }

        public void closeImpl() throws SQLException {
            connectionImpl.close();
        }
    }
}
