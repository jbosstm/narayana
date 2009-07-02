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

import com.arjuna.ats.jdbc.common.jdbcPropertyManager;
import com.arjuna.ats.jdbc.logging.*;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.ModifierFactory;
import com.arjuna.ats.internal.jdbc.drivers.modifiers.ConnectionModifier;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.jta.*;
import com.arjuna.ats.jta.xa.RecoverableXAConnection;
import com.arjuna.ats.jta.xa.XAModifier;
import com.arjuna.ats.jta.exceptions.NotImplementedException;

import com.arjuna.common.util.logging.*;

import java.util.*;
import java.sql.*;
import javax.sql.*;
import javax.transaction.*;
import javax.transaction.xa.*;
import javax.naming.*;

/**
 * This class is responsible for maintaining connection information
 * in such a manner that we can recover the connection to the XA
 * database in the event of a failure.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: IndirectRecoverableConnection.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class IndirectRecoverableConnection implements RecoverableXAConnection, ConnectionControl
{

    public IndirectRecoverableConnection () throws SQLException
    {
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				    com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "IndirectRecoverableConnection.IndirectRecoverableConnection ()");
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
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				    com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "IndirectRecoverableConnection.IndirectRecoverableConnection ( "+dbName+", "+user+", "+passwd+" )");
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

    /**
     * @message com.arjuna.ats.internal.jdbc.ircdest Caught exception
     */

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
	    if (jdbcLogger.loggerI18N.isWarnEnabled())
	    {
		jdbcLogger.loggerI18N.warn("com.arjuna.ats.internal.jdbc.drcdest",
					   new Object[] {e});
	    }
	}
    }

    public boolean packInto (OutputObjectState os)
    {
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				    com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "IndirectRecoverableConnection.packInto ()");
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
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				    com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "IndirectRecoverableConnection.unpackFrom ()");
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
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				    com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "IndirectRecoverableConnection.getResource ()");
	}

	try
	{
	    if (_theXAResource == null)
		_theXAResource = getConnection().getXAResource();

	    return _theXAResource;
	}
	catch (Exception e)
	{
        SQLException sqlException = new SQLException(e.toString());
        sqlException.initCause(e);
	    throw sqlException;
	}
    }

    /**
     * @message com.arjuna.ats.internal.jdbc.idrcclose Caught exception
     */

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
            }
        }
    }

    public XAConnection getConnection () throws SQLException
    {
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				    com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "IndirectRecoverableConnection.getConnection ()");
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
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				    com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "IndirectRecoverableConnection.getDataSource ()");
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

    /**
     * @message com.arjuna.ats.internal.jdbc.jndierror Could not resolve JNDI XADataSource
     */

    private final void createDataSource () throws SQLException
    {
	try
	{
	    if (_theDataSource == null)
	    {
		Hashtable env = new Hashtable();
		Enumeration e = jdbcPropertyManager.getPropertyManager().propertyNames();

		/*
		 * Look through the properties for any Context related
		 * stuff.
		 */

		while (e.hasMoreElements())
		{
		    String name = (String) e.nextElement();

		    if (name.startsWith("Context."))
			env.put(translate(name), jdbcPropertyManager.getPropertyManager().getProperty(name));
		}

		if (env.size() > 0)
		{
		    Context ctx = new InitialContext(env);
		    _theDataSource = (XADataSource) ctx.lookup(_dbName);
		}
		else
		    _theDataSource = null;

		if (_theDataSource == null)
		    throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.jndierror"));
	    }
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

    private final void createConnection () throws SQLException
    {
	try
	{
	    if (_theDataSource == null)
		createDataSource();

	    if ((_user == null) && (_passwd == null))
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
