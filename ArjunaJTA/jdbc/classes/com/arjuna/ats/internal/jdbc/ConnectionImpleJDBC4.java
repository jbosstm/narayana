/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 *
 * (C) 2009 @author JBoss Inc
 */
package com.arjuna.ats.internal.jdbc;

import com.arjuna.ats.jdbc.logging.jdbcLogger;

import java.sql.*;
import java.util.Properties;
import java.util.Map;

/**
 * JDBC 4.0 extention to the Connection wrapper.
 * ConnectionImple 'implements' JDBC3, this subclass
 * adds those that use methods only present in the JDK 1.6 standard library.
 */
public class ConnectionImpleJDBC4 extends ConnectionImple implements Connection
{
	public ConnectionImpleJDBC4(String dbName, Properties info) throws SQLException {
		super(dbName, info);
	}

    /*
     * ******************************************************************* *
     * JDBC 4.0 method section.
     */



    public Clob createClob() throws SQLException
    {
        checkTransaction();

        registerDatabase();

        return getConnection().createClob();
    }

    public Blob createBlob() throws SQLException
    {
        checkTransaction();

        registerDatabase();

        return getConnection().createBlob();
    }

    public NClob createNClob() throws SQLException
    {
        checkTransaction();

		registerDatabase();

		return getConnection().createNClob();
    }

    public SQLXML createSQLXML() throws SQLException
    {
        checkTransaction();

		registerDatabase();

		return getConnection().createSQLXML();
    }

    public boolean isValid(int timeout) throws SQLException
    {
        checkTransaction();

        registerDatabase();

        return getConnection().isValid(timeout);
    }

    public String getClientInfo(String name) throws SQLException
    {
        return getConnection().getClientInfo(name);
    }

    public Properties getClientInfo() throws SQLException
    {
        return getConnection().getClientInfo();
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException
    {
        try
        {
    		getConnection().setClientInfo(name, value);
        }
        catch(SQLException e)
        {
            throw new SQLClientInfoException("setClientInfo : getConnection failed", null, e);
        }
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
        try
        {
    		getConnection().setClientInfo(properties);
        }
        catch(SQLException e)
        {
            throw new SQLClientInfoException("setClientInfo : getConnection failed", null, e);
        }
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
        checkTransaction();

        registerDatabase();

        return getConnection().createArrayOf(typeName, elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {
        checkTransaction();

        registerDatabase();

        return getConnection().createStruct(typeName, attributes);
    }

    /**
     * @message com.arjuna.ats.internal.jdbc.nounwrapping Unwrapping is not supported.
     */
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.nounwrapping"));
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.nounwrapping"));
    }

    /*
	 * end of the JDBC 4.0 section
	 * *******************************************************************
	 */
}
