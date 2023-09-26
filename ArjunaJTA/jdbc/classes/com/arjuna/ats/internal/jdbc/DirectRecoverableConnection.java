/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.jdbc.logging.jdbcLogger;
import com.arjuna.ats.jta.xa.RecoverableXAConnection;
import com.arjuna.common.internal.util.ClassloadingUtility;

import javax.sql.XAConnection;
import java.sql.SQLException;

/**
 * This class is responsible for maintaining connection information
 * in such a manner that we can recover the connection to the XA
 * database in the event of a failure.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: DirectRecoverableConnection.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class DirectRecoverableConnection extends BaseTransactionalDriverXAConnection implements RecoverableXAConnection, ConnectionControl, TransactionalDriverXAConnection
{

    public DirectRecoverableConnection () throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("DirectRecoverableConnection.DirectRecoverableConnection()");
    }
    }

    public DirectRecoverableConnection (String dbName, String user,
				      String passwd, String dynamic,
				      ConnectionImple conn) throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("DirectRecoverableConnection.DirectRecoverableConnection( " + dbName + ", " + user + ", " + passwd + ", " + dynamic + " )");
    }

	_dbName = dbName;
	_user = user;
	_passwd = passwd;
	_dynamic = dynamic;
	_theArjunaConnection = conn;
    }

    public boolean packInto (OutputObjectState os)
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("DirectRecoverableConnection.packInto ()");
    }

	try
	{
	    os.packString(_dbName);
	    os.packString(_user);
	    os.packString(_passwd);
	    os.packString(_dynamic);

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
        jdbcLogger.logger.trace("DirectRecoverableConnection.unpackFrom ()");
    }

	try
	{
	    _dbName = os.unpackString();
	    _user = os.unpackString();
	    _passwd = os.unpackString();
	    _dynamic = os.unpackString();

	    return true;
	}
	catch (Exception e)
	{
	    return false;
	}
    }

    /**
     * If there is a connection then return it. Do not create a
     * new connection otherwise.
     */

    public XAConnection getCurrentConnection () throws SQLException
    {
	return _theConnection;
    }

    protected void createConnection() throws SQLException {
        if (jdbcLogger.logger.isTraceEnabled()) {
            jdbcLogger.logger.trace("DirectRecoverableConnection.createConnection");
        }

        if ((_dynamic == null) || (_dynamic.equals(""))) {
            throw new SQLException(jdbcLogger.i18NLogger.get_dynamicerror());
        } else {
            try {
                if (_theDataSource == null) {
                    _dynamicConnection = ClassloadingUtility.loadAndInstantiateClass(DynamicClass.class, _dynamic, null);
                    if (_dynamicConnection == null) {
                        throw new SQLException(jdbcLogger.i18NLogger.get_dynamicerror());
                    }

                    _theDataSource = _dynamicConnection.getDataSource(_dbName);
                }

                super.createConnection();
            } catch (Exception e) {
                e.printStackTrace();

                SQLException sqlException = new SQLException(e.toString());
                sqlException.initCause(e);
                throw sqlException;
            }
        }
    }

    private String		          _dynamic;
    private DynamicClass                  _dynamicConnection;

}