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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SyncRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.resources.mock;

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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class MockConnection implements Connection
{
    @Override
    public void clearWarnings () throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void close () throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void commit () throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Array createArrayOf (String typeName, Object[] elements)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Blob createBlob () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Clob createClob () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NClob createNClob () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SQLXML createSQLXML () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Statement createStatement () throws SQLException
    {
        return new MockStatement();
    }

    @Override
    public Statement createStatement (int resultSetType,
            int resultSetConcurrency) throws SQLException
    {
        return new MockStatement();
    }

    @Override
    public Statement createStatement (int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException
    {
        return new MockStatement();
    }

    @Override
    public Struct createStruct (String typeName, Object[] attributes)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getAutoCommit () throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getCatalog () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Properties getClientInfo () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getClientInfo (String name) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getHoldability () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public DatabaseMetaData getMetaData () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getTransactionIsolation () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Map<String, Class<?>> getTypeMap () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SQLWarning getWarnings () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isClosed () throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isReadOnly () throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isValid (int timeout) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String nativeSQL (String sql) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CallableStatement prepareCall (String sql) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CallableStatement prepareCall (String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CallableStatement prepareCall (String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (String sql) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (String sql, int autoGeneratedKeys)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (String sql, int[] columnIndexes)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (String sql, String[] columnNames)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void releaseSavepoint (Savepoint savepoint) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback () throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback (Savepoint savepoint) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAutoCommit (boolean autoCommit) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCatalog (String catalog) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setClientInfo (Properties properties)
            throws SQLClientInfoException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setClientInfo (String name, String value)
            throws SQLClientInfoException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHoldability (int holdability) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setReadOnly (boolean readOnly) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Savepoint setSavepoint () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Savepoint setSavepoint (String name) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTransactionIsolation (int level) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTypeMap (Map<String, Class<?>> map) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isWrapperFor (Class<?> iface) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T unwrap (Class<T> iface) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }



    //@Override
    public void setSchema(String schema) throws SQLException
    {
    }

    //@Override
    public String getSchema() throws SQLException
    {
        return null;
    }

    //@Override
    public void abort(Executor executor) throws SQLException
    {
    }

    //@Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
    {
    }

    //@Override
    public int getNetworkTimeout() throws SQLException
    {
        return 0;
    }
}

