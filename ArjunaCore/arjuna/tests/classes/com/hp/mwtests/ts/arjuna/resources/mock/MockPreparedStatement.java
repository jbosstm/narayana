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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;


public class MockPreparedStatement implements PreparedStatement
{
    public MockPreparedStatement ()
    {
        this(true);
    }
    
    public MockPreparedStatement (boolean n)
    {
        _next = n;
    }
    
    @Override
    public void addBatch () throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clearParameters () throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean execute () throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ResultSet executeQuery () throws SQLException
    {
        return new MockResultSet(_next);
    }

    @Override
    public int executeUpdate () throws SQLException
    {
        return 1;
    }

    @Override
    public ResultSetMetaData getMetaData () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ParameterMetaData getParameterMetaData () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setArray (int parameterIndex, Array x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAsciiStream (int parameterIndex, InputStream x)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAsciiStream (int parameterIndex, InputStream x, int length)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAsciiStream (int parameterIndex, InputStream x, long length)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBigDecimal (int parameterIndex, BigDecimal x)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBinaryStream (int parameterIndex, InputStream x)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBinaryStream (int parameterIndex, InputStream x, int length)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBinaryStream (int parameterIndex, InputStream x, long length)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBlob (int parameterIndex, Blob x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBlob (int parameterIndex, InputStream inputStream)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBlob (int parameterIndex, InputStream inputStream,
            long length) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBoolean (int parameterIndex, boolean x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setByte (int parameterIndex, byte x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setBytes (int parameterIndex, byte[] x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCharacterStream (int parameterIndex, Reader reader)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCharacterStream (int parameterIndex, Reader reader,
            int length) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCharacterStream (int parameterIndex, Reader reader,
            long length) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setClob (int parameterIndex, Clob x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setClob (int parameterIndex, Reader reader) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setClob (int parameterIndex, Reader reader, long length)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDate (int parameterIndex, Date x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDate (int parameterIndex, Date x, Calendar cal)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDouble (int parameterIndex, double x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFloat (int parameterIndex, float x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setInt (int parameterIndex, int x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLong (int parameterIndex, long x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNCharacterStream (int parameterIndex, Reader value)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNCharacterStream (int parameterIndex, Reader value,
            long length) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNClob (int parameterIndex, NClob value) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNClob (int parameterIndex, Reader reader)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNClob (int parameterIndex, Reader reader, long length)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNString (int parameterIndex, String value)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNull (int parameterIndex, int sqlType) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNull (int parameterIndex, int sqlType, String typeName)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setObject (int parameterIndex, Object x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setObject (int parameterIndex, Object x, int targetSqlType)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setObject (int parameterIndex, Object x, int targetSqlType,
            int scaleOrLength) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRef (int parameterIndex, Ref x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRowId (int parameterIndex, RowId x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSQLXML (int parameterIndex, SQLXML xmlObject)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setShort (int parameterIndex, short x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setString (int parameterIndex, String x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTime (int parameterIndex, Time x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTime (int parameterIndex, Time x, Calendar cal)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTimestamp (int parameterIndex, Timestamp x)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTimestamp (int parameterIndex, Timestamp x, Calendar cal)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setURL (int parameterIndex, URL x) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setUnicodeStream (int parameterIndex, InputStream x, int length)
            throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addBatch (String sql) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void cancel () throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clearBatch () throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

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
    public boolean execute (String sql) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean execute (String sql, int autoGeneratedKeys)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean execute (String sql, int[] columnIndexes)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean execute (String sql, String[] columnNames)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int[] executeBatch () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet executeQuery (String sql) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int executeUpdate (String sql) throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int executeUpdate (String sql, int autoGeneratedKeys)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int executeUpdate (String sql, int[] columnIndexes)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int executeUpdate (String sql, String[] columnNames)
            throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Connection getConnection () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getFetchDirection () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getFetchSize () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ResultSet getGeneratedKeys () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMaxFieldSize () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMaxRows () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getMoreResults () throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getMoreResults (int current) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getQueryTimeout () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ResultSet getResultSet () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getResultSetConcurrency () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getResultSetHoldability () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getResultSetType () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getUpdateCount () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
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
    public boolean isPoolable () throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCursorName (String name) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setEscapeProcessing (boolean enable) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFetchDirection (int direction) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFetchSize (int rows) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMaxFieldSize (int max) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMaxRows (int max) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPoolable (boolean poolable) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setQueryTimeout (int seconds) throws SQLException
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
    
    private boolean _next;
}

