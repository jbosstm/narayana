/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.jdbc.common.jdbcPropertyManager;
import com.arjuna.ats.jdbc.logging.jdbcLogger;
import com.arjuna.ats.jta.xa.RecoverableXAConnection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * This class is responsible for maintaining connection information
 * in such a manner that we can recover the connection to the XA
 * database in the event of a failure.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: IndirectRecoverableConnection.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class IndirectRecoverableConnection extends BaseTransactionalDriverXAConnection implements RecoverableXAConnection, ConnectionControl, TransactionalDriverXAConnection
{

    public IndirectRecoverableConnection () throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("IndirectRecoverableConnection.IndirectRecoverableConnection ()");
    }
    }

    public IndirectRecoverableConnection (String dbName, String user,
				     String passwd,
				     ConnectionImple conn) throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("IndirectRecoverableConnection.IndirectRecoverableConnection ( " + dbName + ", " + user + ", " + passwd + " )");
    }

	_dbName = dbName;
	_user = user;
	_passwd = passwd;
	_theArjunaConnection = conn;

	/*
	 * Create a jndi specific modifier first, so that we can then
	 * use this to find out what the end-point datasource really
	 * is.
	 */

	com.arjuna.ats.internal.jdbc.drivers.modifiers.jndi jndiModifier = new com.arjuna.ats.internal.jdbc.drivers.modifiers.jndi();

	_dbName = jndiModifier.initialise(_dbName);
	_theModifier = null;

	createDataSource();
    }

    public boolean packInto (OutputObjectState os)
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("IndirectRecoverableConnection.packInto ()");
    }

	try
	{
	    os.packString(_dbName);
	    os.packString(_user);
	    os.packString(_passwd);

	    return true;
	}
	catch (Exception e)
	{
	    return false;
	}
    }

    public boolean unpackFrom (InputObjectState os)
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("IndirectRecoverableConnection.unpackFrom ()");
    }

	try
	{
	    _dbName = os.unpackString();
	    _user = os.unpackString();
	    _passwd = os.unpackString();

	    return true;
	}
	catch (Exception e)
	{
	    return false;
	}
    }

    public String getDatabaseName ()
    {
	return _dbName;
    }

    /*
     * If there is a connection then return it. Do not create a
     * new connection otherwise.
     */

    public XAConnection getCurrentConnection () throws SQLException
    {
	return _theConnection;
    }

    public XADataSource getDataSource () throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("IndirectRecoverableConnection.getDataSource ()");
    }

	return _theDataSource;
    }

    private final void createDataSource () throws SQLException
    {
	try
	{
	    if (_theDataSource == null)
	    {
    		Hashtable env = jdbcPropertyManager.getJDBCEnvironmentBean().getJndiProperties();
            Context ctx = new InitialContext(env);
            _theDataSource = (XADataSource) ctx.lookup(_dbName);
		}

		if (_theDataSource == null) {
		    throw new SQLException(jdbcLogger.i18NLogger.get_jndierror());
	    }
	}
	catch (SQLException ex)
	{
	    throw ex;
	}
	catch (Exception e)
	{
        jdbcLogger.logger.error(e);

        SQLException sqlException = new SQLException(e.toString());
        sqlException.initCause(e);
	    throw sqlException;	}
    }

    protected void createConnection() throws SQLException {
        try {
            if (_theDataSource == null)
                createDataSource();

            super.createConnection();
        } catch (SQLException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();

            SQLException sqlException = new SQLException(e.toString());
            sqlException.initCause(e);
            throw sqlException;
        }
    }

}