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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DirectRecoverableConnection.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.Transaction;
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

public class ProvidedXADataSourceConnection implements ConnectionControl, TransactionalDriverXAConnection
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
	_theConnection = null;
	_theDataSource = xaDatasource;
	_theTransaction = null;
	_theArjunaConnection = conn;
	_theModifier = null;
    }

    public void finalize ()
    {
	try
	{
	    if (_theConnection != null)
	    {
		_theConnection.close();
		_theConnection = null;
	    }
	}
	catch (SQLException e)
	{
        jdbcLogger.i18NLogger.warn_drcdest(e);
	}
    }

    public boolean setTransaction (javax.transaction.Transaction tx)
    {
	synchronized (this)
	{
	    if (_theTransaction == null)
	    {
		_theTransaction = tx;

		return true;
	    }
	}

	/*
	 * In case we have already set it for this transaction.
	 */

	return validTransaction(tx);
    }

    public boolean validTransaction (javax.transaction.Transaction tx)
    {
	boolean valid = true;

	if (_theTransaction != null)
	    valid = _theTransaction.equals(tx);

	return valid;
    }

    /**
     * @return a new XAResource for this connection.
     */

    public XAResource getResource () throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("DirectRecoverableConnection.getResource ()");
    }

	try
	{
	    if (_theXAResource == null)
		_theXAResource = getConnection().getXAResource();

	    return _theXAResource;
	}
	catch (Exception e)
	{
	    e.printStackTrace();

        SQLException sqlException = new SQLException(e.toString());
        sqlException.initCause(e);
        throw sqlException;
	}
    }

    public final void close ()
    {
	reset();
    }

    public final void reset ()
    {
	_theXAResource = null;
	_theTransaction = null;
    }

    /**
     * If there is a connection then return it. Do not create a
     * new connection otherwise.
     */

    public XAConnection getCurrentConnection () throws SQLException
    {
	return _theConnection;
    }

    public void closeCloseCurrentConnection() throws SQLException
    {
        synchronized (this)
        {
            if (_theConnection != null)
            {
                _theConnection.close();
                _theConnection = null;
				_theXAResource = null;
				_theTransaction = null;
            }
        }
    }

    public XAConnection getConnection () throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("DirectRecoverableConnection.getConnection ()");
    }

	try
	{
	    synchronized (this)
	    {
		if (_theConnection == null)
		{
		    createConnection();
		}
	    }

	    return _theConnection;
	}
	catch (Exception e)
	{
	    e.printStackTrace();

        SQLException sqlException = new SQLException(e.toString());
        sqlException.initCause(e);
	    throw sqlException;
	}
    }

    public XADataSource getDataSource () throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("DirectRecoverableConnection.getDataSource ()");
    }

	return _theDataSource;
    }

    public boolean inuse ()
    {
	return (boolean) (_theXAResource != null);
    }

    public String user ()
    {
	return _user;
    }

    public String password ()
    {
	return _passwd;
    }

    public String url ()
    {
	return _dbName;
    }

    public String dynamicClass ()
    {
	return "";
    }

    public String dataSourceName ()
    {
	return _dbName;
    }

    public Transaction transaction ()
    {
	return _theTransaction;
    }

    public void setModifier (ConnectionModifier cm)
    {
	_theModifier = cm;

	if (_theModifier != null)
	    _dbName = _theModifier.initialise(_dbName);
    }

    public XADataSource xaDataSource () {
    	return _theDataSource;
	}

    private final void createConnection () throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("DirectRecoverableConnection.createConnection");
    }

	    try
	    {
		if ((_user == null) && (_passwd == null))
		{
		    if (jdbcLogger.logger.isTraceEnabled()) {
                jdbcLogger.logger.trace("DirectRecoverableConnection - getting connection with no user");
            }

		    _theConnection = _theDataSource.getXAConnection();
		}
		else
		{
		    if (jdbcLogger.logger.isTraceEnabled()) {
                jdbcLogger.logger.trace("DirectRecoverableConnection - getting connection for user " + _user);
            }

		    _theConnection = _theDataSource.getXAConnection(_user, _passwd);
		}
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();

            SQLException sqlException = new SQLException(e.toString());
            sqlException.initCause(e);
    		throw sqlException; 
	    }
	}

    private String		          _dbName;
    private String		          _user;
    private String		          _passwd;
    private XAConnection                  _theConnection;
    private XADataSource	          _theDataSource;
    private XAResource                    _theXAResource;
    private javax.transaction.Transaction _theTransaction;
    private ConnectionImple               _theArjunaConnection;
    private ConnectionModifier            _theModifier;

}

