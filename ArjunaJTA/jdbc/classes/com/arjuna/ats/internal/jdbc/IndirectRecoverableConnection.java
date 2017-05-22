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
 * $Id: IndirectRecoverableConnection.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc;

import java.sql.SQLException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jdbc.drivers.modifiers.ConnectionModifier;
import com.arjuna.ats.jdbc.common.jdbcPropertyManager;
import com.arjuna.ats.jdbc.logging.jdbcLogger;
import com.arjuna.ats.jta.xa.RecoverableXAConnection;

/**
 * This class is responsible for maintaining connection information
 * in such a manner that we can recover the connection to the XA
 * database in the event of a failure.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: IndirectRecoverableConnection.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class IndirectRecoverableConnection implements RecoverableXAConnection, ConnectionControl, TransactionalDriverXAConnection
{

    public IndirectRecoverableConnection () throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("IndirectRecoverableConnection.IndirectRecoverableConnection ()");
    }

	_dbName = null;
	_user = null;
	_passwd = null;
	_theConnection = null;
	_theDataSource = null;
	_theXAResource = null;
	_theTransaction = null;
	_theArjunaConnection = null;
	_theModifier = null;
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
	_theConnection = null;
	_theDataSource = null;
	_theXAResource = null;
	_theTransaction = null;
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

    public XAResource getResource () throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("IndirectRecoverableConnection.getResource ()");
    }

	try
	{
		if (_theXAResource == null) {
	    	if (_theModifier != null && _theModifier.requiresSameRMOverride()) {
	    		_theXAResource = new IsSameRMOverrideXAResource(getConnection().getXAResource());
	    	} else {
	    		_theXAResource = getConnection().getXAResource();
	    	}
	    }
	    return _theXAResource;
	}
	catch (Exception e)
	{
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

    /*
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
				reset();
            }
        }
    }

    public XAConnection getConnection () throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("IndirectRecoverableConnection.getConnection ()");
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
	    throw sqlException;	}
    }

    public XADataSource getDataSource () throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("IndirectRecoverableConnection.getDataSource ()");
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
	if (_theDataSource != null)
	    return _theDataSource.toString();
	else
	    return "";
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

    private final void createConnection () throws SQLException
    {
	try
	{
	    if (_theDataSource == null)
		createDataSource();

	    if ((_user == null || _user.isEmpty()) && (_passwd == null || _passwd.isEmpty()))
		_theConnection = _theDataSource.getXAConnection();
	    else
		_theConnection = _theDataSource.getXAConnection(_user, _passwd);
	}
	catch (SQLException ex)
	{
	    throw ex;
	}
	catch (Exception e)
	{
	    e.printStackTrace();

        SQLException sqlException = new SQLException(e.toString());
        sqlException.initCause(e);
	    throw sqlException;	}
    }

    /*
     * Warning; roadworks ahead!!
     *
     * For some reasons JNDI uses different property names internally for
     * specifying things like the initial context to those it expects
     * users to manipulate. Why?! There are some really stupid people
     * at Sun!!
     */

    private final String translate (String name)
    {
	try
	{
	    if (name.equals("Context.APPLET"))
		return Context.APPLET;
	    if (name.equals("Context.AUTHORITATIVE"))
		return Context.AUTHORITATIVE;
	    if (name.equals("Context.BATCHSIZE"))
		return Context.BATCHSIZE;
	    if (name.equals("Context.DNS_URL"))
		return Context.DNS_URL;
	    if (name.equals("Context.INITIAL_CONTEXT_FACTORY"))
		return Context.INITIAL_CONTEXT_FACTORY;
	    if (name.equals("Context.LANGUAGE"))
		return Context.LANGUAGE;
	    if (name.equals("Context.OBJECT_FACTORIES"))
		return Context.OBJECT_FACTORIES;
	    if (name.equals("Context.PROVIDER_URL"))
		return Context.PROVIDER_URL;
	    if (name.equals("Context.REFERRAL"))
		return Context.REFERRAL;
	    if (name.equals("Context.SECURITY_AUTHENTICATION"))
		return Context.SECURITY_AUTHENTICATION;
	    if (name.equals("Context.SECURITY_CREDENTIALS"))
		return Context.SECURITY_CREDENTIALS;
	    if (name.equals("Context.SECURITY_PRINCIPAL"))
		return Context.SECURITY_PRINCIPAL;
	    if (name.equals("Context.SECURITY_PROTOCOL"))
		return Context.SECURITY_PROTOCOL;
	    if (name.equals("Context.STATE_FACTORIES"))
		return Context.STATE_FACTORIES;
	    if (name.equals("Context.URL_PKG_PREFIXES"))
		return Context.URL_PKG_PREFIXES;
	}
	catch (NullPointerException ex)
	{
	    // name is null
	}

	return name;
    }

    private String                        _dbName;
    private String                        _user;
    private String                        _passwd;
    private XAConnection                  _theConnection;
    private XADataSource                  _theDataSource;
    private XAResource                    _theXAResource;
    private javax.transaction.Transaction _theTransaction;
    private ConnectionImple               _theArjunaConnection;
    private ConnectionModifier            _theModifier;

}
