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
 * $Id: ConnectionImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.ModifierFactory;
import com.arjuna.ats.internal.jdbc.drivers.modifiers.ConnectionModifier;

import com.arjuna.ats.jdbc.TransactionalDriver;
import com.arjuna.ats.jdbc.logging.*;
import com.arjuna.ats.jdbc.common.jdbcPropertyManager;

import com.arjuna.ats.jta.*;
import com.arjuna.ats.jta.recovery.*;
import com.arjuna.ats.jta.xa.XAModifier;
import com.arjuna.ats.jta.xa.RecoverableXAConnection;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.common.util.logging.*;

import javax.transaction.*;
import javax.transaction.xa.*;
import javax.sql.*;
import java.util.*;
import java.sql.*;
import javax.transaction.RollbackException;
import java.sql.SQLException;

/**
 * A transactional JDBC 2.0 connection. This wraps the real connection and
 * registers it with the transaction at appropriate times to ensure that
 * all worked performed by it may be committed or rolled back.
 *
 * Once a connection is used within a transaction, that instance is bound to
 * that transaction for the duration. It can be used by any number of threads,
 * as long as they all have the same notion of the "current" transaction. When
 * the transaction terminates, the connection is freed for use in another
 * transaction.
 *
 * Applications must not use this class directly.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ConnectionImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class ConnectionImple implements java.sql.Connection
{

    public ConnectionImple (String dbName, Properties info) throws SQLException
    {
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				  com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "ConnectionImple.ConnectionImple ( "+dbName+" )");
	}

	String user = null;
	String passwd = null;
	String dynamic = null;

	if (info != null)
	{
	    user = info.getProperty(TransactionalDriver.userName);
	    passwd = info.getProperty(TransactionalDriver.password);
	    dynamic = info.getProperty(TransactionalDriver.dynamicClass);
	}

	if ((dynamic == null) || (dynamic.equals("")))
	{
	    _recoveryConnection = new IndirectRecoverableConnection(dbName, user, passwd, this);
	}
	else
	{
	    _recoveryConnection = new DirectRecoverableConnection(dbName, user, passwd, dynamic, this);
	}

	/*
	 * Is there any "modifier" we are required to work with?
	 */

	_theModifier = null;
	_theConnection = null;
    }

    public ConnectionImple (String dbName, String user, String passwd) throws SQLException
    {
	this(dbName, user, passwd, null);
    }

    public ConnectionImple (String dbName, String user, String passwd, String dynamic) throws SQLException
    {
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				  com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "ConnectionImple.ConnectionImple ( "+dbName+", "+user+", "+passwd+", "+dynamic+" )");
	}

	if ((dynamic == null) || (dynamic.equals("")))
	{
	    _recoveryConnection = new IndirectRecoverableConnection(dbName, user, passwd, this);
	}
	else
	{
	    _recoveryConnection = new DirectRecoverableConnection(dbName, user, passwd, dynamic, this);
	}

	/*
	 * Any "modifier" required to work with?
	 */

	_theModifier = null;
	_theConnection = null;
    }

    public void finalize ()
    {
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				  com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "ConnectionImple.finalize ()");
	}

	_recoveryConnection = null;
	_theConnection = null;
    }

    public Statement createStatement () throws SQLException
    {
	checkTransaction();

	registerDatabase();

	return getConnection().createStatement();
    }

    public Statement createStatement (int rs, int rc) throws SQLException
    {
	checkTransaction();

	registerDatabase();

	return getConnection().createStatement(rs, rc);
    }

    public PreparedStatement prepareStatement (String sql) throws SQLException
    {
	checkTransaction();

	registerDatabase();

	return getConnection().prepareStatement(sql);
    }

    public PreparedStatement prepareStatement (String sql, int rs, int rc) throws SQLException
    {
	checkTransaction();

	registerDatabase();

	return getConnection().prepareStatement(sql, rs, rc);
    }

    public CallableStatement prepareCall (String sql) throws SQLException
    {
	checkTransaction();

	registerDatabase();

	return getConnection().prepareCall(sql);
    }

    public CallableStatement prepareCall (String sql, int rs, int rc) throws SQLException
    {
	checkTransaction();

	registerDatabase();

	return getConnection().prepareCall(sql, rs, rc);
    }

    public String nativeSQL (String sql) throws SQLException
    {
	checkTransaction();

	registerDatabase();

	return getConnection().nativeSQL(sql);
    }

    public Map getTypeMap () throws SQLException
    {
	return getConnection().getTypeMap();
    }

    public void setTypeMap (Map map) throws SQLException
    {
	getConnection().setTypeMap(map);
    }

    /**
     * Not allowed if within a transaction.
     *
     * @message com.arjuna.ats.internal.jdbc.autocommit AutoCommit is not allowed by the transaction service.
     */

    public void setAutoCommit (boolean autoCommit) throws SQLException
    {
	if (transactionRunning())
	{
	    if (autoCommit)
		throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.autocommit"));
	}
	else
	{
	    getConnection().setAutoCommit(autoCommit);
	}
    }

    public boolean getAutoCommit () throws SQLException
    {
	return getConnection().getAutoCommit();
    }

    /**
     * @message com.arjuna.ats.internal.jdbc.commiterror Commit not allowed by transaction service.
     */

    public void commit () throws SQLException
    {
	/*
	 * If there is a transaction running, then it cannot be terminated
	 * via the driver - the user must go through current.
	 */

	if (transactionRunning())
	{
	    throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.commiterror"));
	}
	else
	    getConnection().commit();
    }

    /**
     * @message com.arjuna.ats.internal.jdbc.aborterror Rollback not allowed by transaction service.
     */

    public void rollback () throws SQLException
    {
	if (transactionRunning())
	{
	    throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.aborterror"));
	}
	else
	    getConnection().rollback();
    }

    /*
     * This needs to be reworked in light of experience and requirements.
     */

    /**
     * @message com.arjuna.ats.internal.jdbc.delisterror Delist of resource failed.
     * @message com.arjuna.ats.internal.jdbc.closeerror An error occurred during close:
     */

    public void close () throws SQLException
    {
	try
	{
	    /*
	     * Need to know whether this particular connection has
	     * outstanding resources waiting for it. If not then we
	     * can close, otherwise we can't.
	     */

	    if (!_recoveryConnection.inuse())
	    {
		ConnectionManager.remove(this);  // finalize?
	    }

	    /*
	     * Delist resource if within a transaction.
	     */

	    javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
	    javax.transaction.Transaction tx = tm.getTransaction();

	    /*
	     * Don't delist if transaction not running. Rely on exception
	     * for this. Also only delist if the transaction is the one
	     * the connection is enlisted with!
	     */

	    if (tx != null)
	    {
		if (_recoveryConnection.validTransaction(tx))
		{
		    XAResource xares = _recoveryConnection.getResource();

		    if (!tx.delistResource(xares, XAResource.TMSUCCESS))
			throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.delisterror"));

		    /*
		     * We can't close the connection until the transaction
		     * has terminated, so register a synchronisation here.
		     */
		    getModifier();

		    if (_theModifier != null && ((ConnectionModifier) _theModifier).supportsMultipleConnections())
		    {
		        tx.registerSynchronization(new ConnectionSynchronization(_theConnection, _recoveryConnection));
		        _theConnection = null;
		    }
		}
	    }
	    else
	    {
		_recoveryConnection.close();
		_theConnection = null;
	    }

	    // what about connections without xaCon?
	}
	catch (IllegalStateException ex)
	{
	    // transaction not running, so ignore.
	}
	catch (SQLException sqle)
	{
	    throw sqle;
	}
	catch (Exception e1)
	{
	    e1.printStackTrace();

	    throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.closeerror")+e1);
	}
    }

    public boolean isClosed () throws SQLException
    {
	/*
	 * A connection may appear closed to a thread if another thread
	 * has bound it to a different transaction.
	 */

	checkTransaction();

	if (_theConnection == null)
	    return false;  // not opened yet.
	else
	    return _theConnection.isClosed();
    }

    public DatabaseMetaData getMetaData () throws SQLException
    {
	return getConnection().getMetaData();
    }

    /**
     * Can only set readonly before we use the connection in a
     * given transaction!
     *
     * @message com.arjuna.ats.internal.jdbc.setreadonly Cannot set readonly when within a transaction!
     */

    public void setReadOnly (boolean ro) throws SQLException
    {
	if (!_recoveryConnection.inuse())
	{
	    getConnection().setReadOnly(ro);
	}
	else
	    throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.setreadonly"));
    }

    public boolean isReadOnly () throws SQLException
    {
	return getConnection().isReadOnly();
    }

    public void setCatalog (String cat) throws SQLException
    {
	checkTransaction();

	registerDatabase();

	getConnection().setCatalog(cat);
    }

    public String getCatalog () throws SQLException
    {
	checkTransaction();

	registerDatabase();

	return getConnection().getCatalog();
    }

    /**
     * @message com.arjuna.ats.internal.jdbc.stateerror State must be:
     */

    public void setTransactionIsolation (int iso) throws SQLException
    {
	checkTransaction();

	/*
	if (iso != Connection.TRANSACTION_SERIALIZABLE)
	    throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.stateerror")+"Connection.TRANSACTION_SERIALIZABLE");
	*/

	getConnection().setTransactionIsolation(iso);
    }

    public int getTransactionIsolation () throws SQLException
    {
	return getConnection().getTransactionIsolation();
    }

    public SQLWarning getWarnings () throws SQLException
    {
	return getConnection().getWarnings();
    }

    public void clearWarnings () throws SQLException
    {
	getConnection().clearWarnings();
    }

    /**
     * @return the Arjuna specific recovery connection information. This
     * should not be used by anything other than Arjuna.
     */

    public final RecoverableXAConnection recoveryConnection ()
    {
	return _recoveryConnection;
    }

    /*
     ********************************************************************
     ** JDBC 3.0 section, needed to compile under jdk1.4
     ** We don't support this stuff yet, so we just throw exceptions.
     */

    public void setHoldability(int holdability) throws SQLException {
        throw new SQLException("feature not supported");
    }

    public int getHoldability() throws SQLException {
        throw new SQLException("feature not supported");
    }

    public Savepoint setSavepoint() throws SQLException {
        throw new SQLException("feature not supported");
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLException("feature not supported");
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        throw new SQLException("feature not supported");
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new SQLException("feature not supported");
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                             int resultSetHoldability) throws SQLException {
        throw new SQLException("feature not supported");
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
              int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLException("feature not supported");
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
             int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLException("feature not supported");
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException {
        throw new SQLException("feature not supported");
    }

    public PreparedStatement prepareStatement(String sql, int columnIndexes[])
            throws SQLException {
        throw new SQLException("feature not supported");
    }

    public PreparedStatement prepareStatement(String sql, String columnNames[])
            throws SQLException {
        throw new SQLException("feature not supported");
    }

    /* end of the JDBC 3.0 section
     ********************************************************************
     */

    /**
     * @return the XAResource associated with the current XAConnection.
     */

    protected final XAResource getXAResource ()
    {
	try
	{
	    return _recoveryConnection.getResource();
	}
	catch (Exception e)
	{
	    return null;
	}
    }

    /**
     * Remove this connection so that we have to get another one when
     * asked. Some drivers allow connections to be reused once any
     * transactions have finished with them.
     */

    final void reset ()
    {
	try
	{
	    if (_theConnection != null)
		_theConnection.close();
	}
	catch (Exception ex)
	{
	}
	finally
	{
	    _theConnection = null;
	}
    }

    /**
     * @message com.arjuna.ats.internal.jdbc.isolationlevelfailget {0} - failed to set isolation level: {1}
     * @message com.arjuna.ats.internal.jdbc.isolationlevelfailset {0} - failed to set isolation level: {1}
     * @message com.arjuna.ats.internal.jdbc.conniniterror JDBC2 connection initialisation problem
     */

    final java.sql.Connection getConnection () throws SQLException
    {
	if (_theConnection != null)
	    return _theConnection;

	XAConnection xaConn = _recoveryConnection.getConnection();

	if (xaConn != null)
	{
	    _theConnection =  xaConn.getConnection();

	    try
	    {
		getModifier();

		if (_theModifier != null)
		{
		    ((ConnectionModifier) _theModifier).setIsolationLevel(_theConnection, _currentIsolationLevel);
		}
	    }
	    catch (SQLException ex)
	    {
		throw ex;
	    }
	    catch (Exception e)
	    {
		if (jdbcLogger.loggerI18N.isWarnEnabled())
		{
		    jdbcLogger.loggerI18N.warn("com.arjuna.ats.internal.jdbc.isolationlevelfailset",
					       new Object[] {"ConnectionImple.getConnection", e});
		}

		throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.conniniterror")+":"+e);
	    }

	    return _theConnection;
	}
	else
	    return null;
    }

    final ConnectionControl connectionControl ()
    {
	return (ConnectionControl) _recoveryConnection;
    }

    protected final boolean transactionRunning () throws SQLException
    {
	try
	{
	    if (com.arjuna.ats.jta.TransactionManager.transactionManager().getTransaction() != null)
	    {
		return true;
	    }
	    else
	    {
		return false;
	    }
	}
	catch (Exception e)
	{
	    throw new SQLException(e.toString());
	}
    }

    /**
     * Whenever a JDBC call is invoked on us we get an XAResource and
     * try to register it with the transaction. If the same thread
     * causes this to happen many times within the same transaction then
     * we will currently attempt to get and register many redundant XAResources
     * for it. The JTA implementation will detect this and ignore all but the
     * first for each thread. However, a further optimisation would be to trap
     * such calls here and not do a registration at all. This would require the
     * connection object to be informed whenever a transaction completes so
     * that it could flush its cache of XAResources though.
     *
     * @message com.arjuna.ats.internal.jdbc.rollbackerror {0} - could not mark transaction rollback
     * @message com.arjuna.ats.internal.jdbc.enlistfailed enlist of resource failed
     * @message com.arjuna.ats.internal.jdbc.alreadyassociated Connection is already associated with a different transaction! Obtain a new connection for this transaction.
     */

    protected final synchronized void registerDatabase () throws SQLException
    {
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
				    com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "ConnectionImple.registerDatabase ()");
	}

	Connection theConnection = getConnection();

	if (theConnection != null)
	{
	    XAResource xares = null;

	    try
	    {
		javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
		javax.transaction.Transaction tx = tm.getTransaction();

		if (tx == null)
		    return;

		/*
		 * Already enlisted with this transaction?
		 */

		if (!_recoveryConnection.setTransaction(tx))
		    throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.alreadyassociated"));

		Object[] params;

		if (_theModifier != null)
		    params = new Object[2];
		else
		    params = new Object[1];

		params[com.arjuna.ats.jta.transaction.Transaction.XACONNECTION] = _recoveryConnection;

		if (_theModifier != null)
		    params[com.arjuna.ats.jta.transaction.Transaction.XAMODIFIER] = (XAModifier) _theModifier;

		/*
		 * Use our extended version of enlistResource.
		 */

		xares = _recoveryConnection.getResource();

		if (!((com.arjuna.ats.jta.transaction.Transaction) tx).enlistResource(xares, params))
		{
		    /*
		     * Failed to enlist, so mark transaction as rollback only.
		     */

		    try
		    {
			tx.setRollbackOnly();
		    }
		    catch (Exception e)
		    {
			if (jdbcLogger.loggerI18N.isWarnEnabled())
			{
			    jdbcLogger.loggerI18N.warn("com.arjuna.ats.internal.jdbc.rollbackerror",
						       new Object[] {"ConnectionImple.registerDatabase"});
			}

			throw new SQLException(e.toString());
		    }

		    throw new SQLException("ConnectionImple.registerDatabase - " + jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.enlistfailed"));
		}

		params = null;
		xares = null;
		tx = null;
		tm = null;
	    }
	    catch (RollbackException e1)
	    {
		throw new SQLException("ConnectionImple.registerDatabase - "+e1);
	    }
	    catch (SystemException e2)
	    {
		throw new SQLException("ConnectionImple.registerDatabase - "+e2);
	    }
	    catch (SQLException e3)
	    {
		throw e3;
	    }
	    catch (Exception e4)
	    {
		throw new SQLException(e4.toString());
	    }
	}
    }

    /**
     * @message com.arjuna.ats.internal.jdbc.alreadyassociatedcheck Checking transaction and found that this connection is already associated with a different transaction! Obtain a new connection for this transaction.
     * @message com.arjuna.ats.internal.jdbc.infoerror Could not get transaction information.
     */

    protected final void checkTransaction () throws SQLException
    {
	if (jdbcLogger.logger.isDebugEnabled())
	{
	    jdbcLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
				    com.arjuna.ats.jdbc.logging.FacilityCode.FAC_JDBC, "ConnectionImple.checkTransaction ()");
	}

	try
	{
	    javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
	    javax.transaction.Transaction tx = tm.getTransaction();

	    if (tx == null)
		return;

	    /*
	     * Now check that we are not already associated with a transaction.
	     */

	    if (!_recoveryConnection.validTransaction(tx))
		throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.alreadyassociatedcheck"));
	}
	catch (SQLException ex)
	{
	    throw ex;
	}
	catch (Exception e3)
	{
	    throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.infoerror"));
	}
    }

    /**
     * @message com.arjuna.ats.internal.jdbc.getmoderror Failed to get modifier for driver:
     */

    private final void getModifier ()
    {
	if (_theModifier == null)
	{
	    try
	    {
		DatabaseMetaData md = _theConnection.getMetaData();

		String name = md.getDriverName();
		int major = md.getDriverMajorVersion();
		int minor = md.getDriverMinorVersion();

		_theModifier = ModifierFactory.getModifier(name, major, minor);

		((ConnectionControl) _recoveryConnection).setModifier((ConnectionModifier) _theModifier);
	    }
	    catch (Exception ex)
	    {
		if (jdbcLogger.loggerI18N.isWarnEnabled())
		{
		    jdbcLogger.loggerI18N.warn("com.arjuna.ats.internal.jdbc.getmoderror", ex);
		}
	    }
	}
    }

    private RecoverableXAConnection _recoveryConnection;
    private java.lang.Object        _theModifier;
    private Connection              _theConnection;

    private static final int defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE;

    private static int _currentIsolationLevel = defaultIsolationLevel;

    /**
     * @message com.arjuna.ats.internal.jdbc.isolationerror Unknown isolation level {0}. Will use default of TRANSACTION_SERIALIZABLE.
     */

    static
    {
	String isolationLevel = jdbcPropertyManager.propertyManager.getProperty(com.arjuna.ats.jdbc.common.Environment.ISOLATION_LEVEL);

	if (isolationLevel != null)
	{
	    if (isolationLevel.equals("TRANSACTION_READ_COMMITTED"))
		_currentIsolationLevel = Connection.TRANSACTION_READ_COMMITTED;
	    else if (isolationLevel.equals("TRANSACTION_READ_UNCOMMITTED"))
		    _currentIsolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
	    else if (isolationLevel.equals("TRANSACTION_REPEATABLE_READ"))
			_currentIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
	    else if (isolationLevel.equals("TRANSACTION_SERIALIZABLE"))
			    _currentIsolationLevel = Connection.TRANSACTION_SERIALIZABLE;
	    else
	    {
		if (jdbcLogger.loggerI18N.isWarnEnabled())
		{
		    jdbcLogger.loggerI18N.warn("com.arjuna.ats.internal.jdbc.isolationerror",
							   new Object[] {isolationLevel});
		}

		    _currentIsolationLevel = Connection.TRANSACTION_SERIALIZABLE;
	    }
	}
    }

}

