/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import jakarta.transaction.Transaction;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.ConnectionModifier;
import com.arjuna.ats.jdbc.logging.jdbcLogger;

/**
 * This class is responsible for maintaining connection information
 * in such a manner that we can recover the connection to the XA
 * database in the event of a failure.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: DirectRecoverableConnection.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class ProvidedXADataSourceConnection extends BaseTransactionalDriverXAConnection implements ConnectionControl, TransactionalDriverXAConnection
{

	public ProvidedXADataSourceConnection (String dbName, String user,
				      String passwd, XADataSource xaDatasource,
				      ConnectionImple conn) throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("DirectRecoverableConnection.DirectRecoverableConnection( " + dbName + ", " + user + ", " + passwd + ", " + xaDatasource + " )");
    }
    _dbName = dbName;
	_user = user;
	_passwd = passwd;
	_theDataSource = xaDatasource;
	_theArjunaConnection = conn;
    }

}