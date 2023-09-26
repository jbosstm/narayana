/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.ConnectionModifier;
import com.arjuna.ats.internal.jdbc.drivers.modifiers.ModifierFactory;
import com.arjuna.ats.jdbc.TransactionalDriver;
import com.arjuna.ats.jdbc.common.jdbcPropertyManager;
import com.arjuna.ats.jdbc.logging.jdbcLogger;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.transaction.Transaction;
import com.arjuna.ats.jta.xa.RecoverableXAConnection;
import com.arjuna.ats.jta.xa.XAModifier;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import javax.transaction.xa.XAResource;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A transactional JDBC connection. This wraps the real connection and
 * registers it with the transaction at appropriate times to ensure that all
 * worked performed by it may be committed or rolled back.
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
 * @version $Id: ConnectionImple.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 2.0.
 */

public class ConnectionImple implements Connection
{


    public ConnectionImple(String dbName, Properties info) throws SQLException {
        if (jdbcLogger.logger.isTraceEnabled()) {
            jdbcLogger.logger.trace("ConnectionImple.ConnectionImple ( " + dbName + " )");
        }

        String user = null;
        String passwd = null;
        String dynamic = null;
        Object xaDataSource = null;
        boolean poolingEnabled = false;

        if (info != null) {
            user = info.getProperty(TransactionalDriver.userName, "");
            passwd = info.getProperty(TransactionalDriver.password, "");
            dynamic = info.getProperty(TransactionalDriver.dynamicClass);
            xaDataSource = info.get(TransactionalDriver.XADataSource);
            poolingEnabled = Boolean.valueOf(info.getProperty(TransactionalDriver.poolConnections, "true")).booleanValue();
        }


        init (dbName, user, passwd, dynamic, xaDataSource, poolingEnabled);
    }

    public ConnectionImple(String dbName, String user, String passwd,
                           String dynamic, Object xaDataSource) throws SQLException {
        if (jdbcLogger.logger.isTraceEnabled()) {
            jdbcLogger.logger.trace("ConnectionImple.ConnectionImple ( " + dbName + ", " + user
                    + ", " + passwd + ", " + dynamic + " )");
        }

        init(dbName, user, passwd, dynamic, xaDataSource, false);
    }

    private void init (String dbName, String user, String passwd,
                       String dynamic, Object xaDataSource, boolean poolingEnabled) throws SQLException {
        if (xaDataSource != null) {
            _transactionalDriverXAConnectionConnection = new ProvidedXADataSourceConnection(dbName, user,
                    passwd, (XADataSource) xaDataSource, this);
        } else if ((dynamic == null) || (dynamic.equals(""))) {
            _transactionalDriverXAConnectionConnection = new IndirectRecoverableConnection(dbName,
                    user, passwd, this);
        } else {
            _transactionalDriverXAConnectionConnection = new DirectRecoverableConnection(dbName, user,
                    passwd, dynamic, this);
        }

		/*
		 * Any "modifier" required to work with?
		 */

		_theModifier = null;

        getConnection();

        _poolingEnabled = poolingEnabled;
        incrementUseCount();
    }



	public Statement createStatement() throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().createStatement();
	}

	public Statement createStatement(int rs, int rc) throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().createStatement(rs, rc);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().prepareStatement(sql);
	}

	public PreparedStatement prepareStatement(String sql, int rs, int rc)
			throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().prepareStatement(sql, rs, rc);
	}

	public CallableStatement prepareCall(String sql) throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().prepareCall(sql);
	}

	public CallableStatement prepareCall(String sql, int rs, int rc)
			throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().prepareCall(sql, rs, rc);
	}

	public String nativeSQL(String sql) throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().nativeSQL(sql);
	}

	public Map getTypeMap() throws SQLException
	{
		return getConnection().getTypeMap();
	}

/*
    public void setTypeMap(Map map) throws SQLException
	{
		getConnection().setTypeMap(map);
	}
*/
	/**
	 * Not allowed if within a transaction.
	 */

	public void setAutoCommit(boolean autoCommit) throws SQLException
	{
		if (transactionRunning())
		{
			if (autoCommit)
				throw new SQLException(jdbcLogger.i18NLogger.get_autocommit());
		}
		else
		{
			getConnection().setAutoCommit(autoCommit);
		}
	}

	public boolean getAutoCommit() throws SQLException
	{
		return getConnection().getAutoCommit();
	}

	public void commit() throws SQLException
	{
		/*
		 * If there is a transaction running, then it cannot be terminated via
		 * the driver - the user must go through current.
		 */

		if (transactionRunning())
		{
			throw new SQLException(jdbcLogger.i18NLogger.get_commiterror());
		}
		else
			getConnection().commit();
	}

	public void rollback() throws SQLException
	{
		if (transactionRunning())
		{
			throw new SQLException(jdbcLogger.i18NLogger.get_aborterror());
		}
		else
			getConnection().rollback();
	}

	/*
	 * This needs to be reworked in light of experience and requirements.
	 */
	public void close() throws SQLException {
		jdbcLogger.logger.trace("Connection closed: " + this);

	    try
	    {
	        /*
	         * Delist resource if within a transaction.
	         */

	        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
	        .transactionManager();
	        jakarta.transaction.Transaction tx = tm.getTransaction();

	        /*
	         * Don't delist if transaction not running. Rely on exception for
	         * this. Also only delist if the transaction is the one the
	         * connection is enlisted with!
	         */

	        if (tx != null)
	        {
	            if (_transactionalDriverXAConnectionConnection.validTransaction(tx))
	            {
	                if (tx instanceof com.arjuna.ats.jta.transaction.Transaction &&
	                    _transactionalDriverXAConnectionConnection.getResource() != null &&
	                    ((com.arjuna.ats.jta.transaction.Transaction) tx).getResources().get(_transactionalDriverXAConnectionConnection.getResource()) == null) {
	                    // This connection was not enlisted to transaction
	                    // Calling delistResource on not enlisted resource is illegal thus only closing and returning back
	                    return;
	                }
	                if ((tx.getStatus() == Status.STATUS_ACTIVE && !tx.delistResource(_transactionalDriverXAConnectionConnection.getResource(), XAResource.TMSUCCESS)) ||
                            (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK && !tx.delistResource(_transactionalDriverXAConnectionConnection.getResource(), XAResource.TMFAIL)))
	                    throw new SQLException(
	                            jdbcLogger.i18NLogger.get_delisterror());
	            }
	            else
	            {
	                throw new SQLException(jdbcLogger.i18NLogger.get_closeerrorinvalidtx(tx.toString()));
	            }
	        }
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
	        SQLException sqlException = new SQLException(jdbcLogger.i18NLogger.get_closeerror());
	        sqlException.initCause(e1);
	        throw sqlException;
	    } finally {
            closeImpl();
			}
	}

    public void incrementUseCount() {
        synchronized (this) {
            useCount.incrementAndGet();
        }
    }

    public boolean inUse() {
        return useCount.get() > 0;
    }

    void closeImpl() throws SQLException {
        jdbcLogger.logger.trace("Connection closeImpl: " + this);
        boolean release = false;
        boolean remove = false;
        synchronized (this) {
            if (useCount.decrementAndGet() == 0) {
                if (_poolingEnabled) {
                    if (_transactionalDriverXAConnectionConnection != null) {
                        _transactionalDriverXAConnectionConnection.setTransaction(null);
                    }
                    release = true;
                } else if (_transactionalDriverXAConnectionConnection != null) {
					if (_theConnection != null && !_theConnection.isClosed()) {
						_theConnection.close();
					}
                    _transactionalDriverXAConnectionConnection.closeCloseCurrentConnection();
					remove = true;
                }
            }
        }
        if (remove) {
			ConnectionManager.remove(this);
		} else if (release) {
            ConnectionManager.release(this);
        }
    }



	public boolean isClosed() throws SQLException
	{
		/*
		 * A connection may appear closed to a thread if another thread has
		 * bound it to a different transaction.
		 */

		if (_transactionalDriverXAConnectionConnection == null)
			return true; // closeImpl was explicitly called
		else if (_theConnection == null)
			return false; // not opened yet. // TODO why don't we return true here
		else
			return _theConnection.isClosed();
	}

	public DatabaseMetaData getMetaData() throws SQLException
	{
		return getConnection().getMetaData();
	}

	/**
	 * Can only set readonly before we use the connection in a given
	 * transaction!
	 */

	public void setReadOnly(boolean ro) throws SQLException
	{
		if (!_transactionalDriverXAConnectionConnection.inuse())
		{
			getConnection().setReadOnly(ro);
		}
		else
			throw new SQLException(jdbcLogger.i18NLogger.get_setreadonly());
	}

	public boolean isReadOnly() throws SQLException
	{
		return getConnection().isReadOnly();
	}

	public void setCatalog(String cat) throws SQLException
	{
		checkTransaction();

		registerDatabase();

		getConnection().setCatalog(cat);
	}

	public String getCatalog() throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().getCatalog();
	}

	public void setTransactionIsolation(int iso) throws SQLException
	{
		checkTransaction();

		/*
		 * if (iso != Connection.TRANSACTION_SERIALIZABLE) throw new
		 * SQLException(jdbcLogger.loggerI18N.getString.getString("com.arjuna.ats.internal.jdbc.stateerror")+"Connection.TRANSACTION_SERIALIZABLE");
		 */

		getConnection().setTransactionIsolation(iso);
	}

	public int getTransactionIsolation() throws SQLException
	{
		return getConnection().getTransactionIsolation();
	}

	public SQLWarning getWarnings() throws SQLException
	{
		return getConnection().getWarnings();
	}

	public void clearWarnings() throws SQLException
	{
		getConnection().clearWarnings();
	}

	/**
	 * @return the Arjuna specific recovery connection information. This should
	 *         not be used by anything other than Arjuna.
	 */

	public final RecoverableXAConnection recoveryConnection()
	{
		if (_transactionalDriverXAConnectionConnection instanceof RecoverableXAConnection) {
			return (RecoverableXAConnection) _transactionalDriverXAConnectionConnection;
		} else {
			return null;
		}
	}

	/*
	 * ******************************************************************* *
	 * JDBC 3.0 section
	 */

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException
    {
        getConnection().setTypeMap(map);
    }

    public void setHoldability(int holdability) throws SQLException
    {
        checkTransaction();

        registerDatabase();

        getConnection().setHoldability(holdability);
    }

    public int getHoldability() throws SQLException
    {
        return getConnection().getHoldability();
    }

    public Savepoint setSavepoint() throws SQLException
    {
        if (transactionRunning())
        {
            throw new SQLException(jdbcLogger.i18NLogger.get_setsavepointerror());
        }
        else
        {
            return getConnection().setSavepoint();
        }
    }

    public Savepoint setSavepoint(String name) throws SQLException
    {
        if (transactionRunning())
        {
            throw new SQLException(jdbcLogger.i18NLogger.get_setsavepointerror());
        }
        else
        {
            return getConnection().setSavepoint(name);
        }
    }

    // The JDBC 3.0 spec (section 12.4) prohibits calling setSavepoint indide an XA tx.
    // It does not explicitly disallow calling rollback(savepoint) or releaseSavepoint(savepoint)
    // but allowing them does not make a whole lot of sense, so we don't:

	public void rollback(Savepoint savepoint) throws SQLException
	{
		if (transactionRunning())
		{
			throw new SQLException(jdbcLogger.i18NLogger.get_rollbacksavepointerror());
		}
		else
		{
			getConnection().rollback(savepoint);
		}
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException
	{
		if (transactionRunning())
		{
			throw new SQLException(jdbcLogger.i18NLogger.get_releasesavepointerror());
		}
		else
		{
			getConnection().releaseSavepoint(savepoint);
		}
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency,
									 int resultSetHoldability) throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
											  int resultSetConcurrency, int resultSetHoldability) throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
										 int resultSetConcurrency, int resultSetHoldability) throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int columnIndexes[])
			throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String columnNames[])
			throws SQLException
	{
		checkTransaction();

		registerDatabase();

		return getConnection().prepareStatement(sql, columnNames);
	}

	/*
	 * end of the JDBC 3.0 section
	 * *******************************************************************
	 */


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

    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        if (iface != null) {
            if (iface.isInstance(this)) {
                return (T) this;
            } else {
                Connection conn = getConnection();
                if (conn != null) {
                    if (iface.isInstance(conn)) {
                        return (T) conn;
                    } else if(conn.isWrapperFor(iface)) {
                        return conn.unwrap(iface);
                    }
                }
            }
        }
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        if (iface != null) {
            if (iface.isInstance(this)) {
                return true;
            } else {
                Connection conn = getConnection();
                if (conn != null) {
                    if (iface.isInstance(conn)) {
                        return true;
                    } else {
                        return conn.isWrapperFor(iface);
                    }
                }
            }
        }
        return false;
    }

    /*
	 * end of the JDBC 4.0 section
	 * *******************************************************************
	 */

    /*
     * ******************************************************************* *
     * Java 7 method section.
     */

    //@Override
    public void setSchema(String schema) throws SQLException
    {
        throw new SQLException();
    }

    //@Override
    public String getSchema() throws SQLException
    {
        throw new SQLException();
    }

    //@Override
    public void abort(Executor executor) throws SQLException
    {
        throw new SQLException();
    }

    //@Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
    {
        throw new SQLException();
    }

    //@Override
    public int getNetworkTimeout() throws SQLException
    {
        throw new SQLException();
    }

    /*
	 * end of the Java 7 section
	 * *******************************************************************
	 */


    /**
	 * @return the XAResource associated with the current XAConnection.
	 */

	protected final XAResource getXAResource()
	{
		try
		{
			return _transactionalDriverXAConnectionConnection.getResource();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	final java.sql.Connection getConnection() throws SQLException
	{
		if (_theConnection != null && !_theConnection.isClosed())
			return _theConnection;

		XAConnection xaConn = _transactionalDriverXAConnectionConnection.getConnection();

		if (xaConn != null)
		{
			_theConnection = xaConn.getConnection();

			try
			{
				getModifier();

				if (_theModifier != null)
				{
					((ConnectionModifier) _theModifier).setIsolationLevel(
							_theConnection, defaultIsolationLevel);
				} else {
                    _theConnection.setTransactionIsolation(defaultIsolationLevel);
                }
			}
			catch (SQLException ex)
			{
				throw ex;
			}
			catch (Exception e)
			{
                jdbcLogger.i18NLogger.warn_isolationlevelfailset("ConnectionImple.getConnection", e);

                SQLException sqlException = new SQLException(jdbcLogger.i18NLogger.get_conniniterror());
                sqlException.initCause(e);
                throw sqlException;
			}

			return _theConnection;
		}
		else
			return null;
	}

	final ConnectionControl connectionControl()
	{
		return (ConnectionControl) _transactionalDriverXAConnectionConnection;
	}

	protected final boolean transactionRunning() throws SQLException
	{
		try
		{
			if (com.arjuna.ats.jta.TransactionManager.transactionManager()
					.getTransaction() != null)
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
            SQLException sqlException = new SQLException(e.toString());
            sqlException.initCause(e);
            throw sqlException;
		}
	}

	/**
	 * Whenever a JDBC call is invoked on us we get an XAResource and try to
	 * register it with the transaction. If the same thread causes this to
	 * happen many times within the same transaction then we will currently
	 * attempt to get and register many redundant XAResources for it. The JTA
	 * implementation will detect this and ignore all but the first for each
	 * thread. However, a further optimisation would be to trap such calls here
	 * and not do a registration at all. This would require the connection
	 * object to be informed whenever a transaction completes so that it could
	 * flush its cache of XAResources though.
	 */

	protected final synchronized void registerDatabase() throws SQLException
	{
		if (jdbcLogger.logger.isTraceEnabled()) {
            jdbcLogger.logger.trace("ConnectionImple.registerDatabase ()");
        }

        boolean needsClose = _theConnection == null;
		Connection theConnection = getConnection();

		if (theConnection != null)
		{
			XAResource xares = null;

			try
			{
				jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
						.transactionManager();
				jakarta.transaction.Transaction tx = tm.getTransaction();

				if (tx == null) {
					return;
				}


				/*
				 * Already enlisted with this transaction?
				 */

				if (!_transactionalDriverXAConnectionConnection.setTransaction(tx))
					throw new SQLException( jdbcLogger.i18NLogger.get_alreadyassociated() );

				Object[] params;

				if (_theModifier != null)
					params = new Object[2];
				else
					params = new Object[1];

				params[com.arjuna.ats.jta.transaction.Transaction.XACONNECTION] = _transactionalDriverXAConnectionConnection;

				if (_theModifier != null)
					params[com.arjuna.ats.jta.transaction.Transaction.XAMODIFIER] = (XAModifier) _theModifier;

				/*
				 * Use our extended version of enlistResource.
				 */

				xares = _transactionalDriverXAConnectionConnection.getResource();

				if (!((com.arjuna.ats.jta.transaction.Transaction) tx)
						.enlistResource(xares, params))
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
                        jdbcLogger.i18NLogger.warn_rollbackerror("ConnectionImple.registerDatabase");

                        SQLException sqlException = new SQLException(e.toString());
                        sqlException.initCause(e);
						throw sqlException;
					}

					throw new SQLException(
							"ConnectionImple.registerDatabase - "
									+ jdbcLogger.i18NLogger.get_enlistfailed());
				} else {
					getModifier();

					if (_theModifier == null) {
						jdbcLogger.i18NLogger.info_closingconnectionnull(theConnection.toString());

						// no indication about connections, so assume close immediately

	                    /*
						 * Don't return just yet. Drop through bottom of these clauses and
	                     * close _theConnection and _recoveryConnection.
	                     *
	                     * delayClose is false at this point.
	                     *
	                     * JBTM-789.
	                     */
					} else {
						if (((ConnectionModifier) _theModifier).supportsMultipleConnections()) {
	                        /*
	                         * We can't close the connection until the transaction has
	                         * terminated, so register a Synchronization here.
	                         */

							jdbcLogger.i18NLogger.debug_closingconnection(theConnection.toString());

							jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistry().registerInterposedSynchronization(new ConnectionSynchronization(this));
						}
					}
				}
			}
			catch (RollbackException e1)
			{
                SQLException sqlException = new SQLException("ConnectionImple.registerDatabase - " + e1);
                sqlException.initCause(e1);
				throw sqlException;
			}
			catch (SystemException e2)
			{
                SQLException sqlException = new SQLException("ConnectionImple.registerDatabase - "+ e2);
                sqlException.initCause(e2);
                throw sqlException;
			}
			catch (SQLException e3)
			{
				throw e3;
			}
			catch (Exception e4)
			{
                SQLException sqlException = new SQLException(e4.toString());
                sqlException.initCause(e4);
                throw sqlException;
			}
		}
	}

	protected final void checkTransaction() throws SQLException
	{
		if (jdbcLogger.logger.isTraceEnabled()) {
            jdbcLogger.logger.trace("ConnectionImple.checkTransaction ()");
        }

		try
		{
			jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
					.transactionManager();
			jakarta.transaction.Transaction tx = tm.getTransaction();

			if (tx == null)
				return;

			if (tx.getStatus() != Status.STATUS_ACTIVE)
				throw new SQLException(jdbcLogger.i18NLogger.get_inactivetransaction());

			/*
			 * Now check that we are not already associated with a transaction.
			 */

			if (!_transactionalDriverXAConnectionConnection.validTransaction(tx))
				throw new SQLException(
						jdbcLogger.i18NLogger.get_alreadyassociatedcheck());
		}
		catch (SQLException ex)
		{
			throw ex;
		}
		catch (Exception e3)
		{
            SQLException sqlException = new SQLException(jdbcLogger.i18NLogger.get_infoerror());
            sqlException.initCause(e3);
            throw sqlException;
		}
	}

	private final void getModifier()
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

				((ConnectionControl) _transactionalDriverXAConnectionConnection)
						.setModifier((ConnectionModifier) _theModifier);
			}
			catch (Exception ex)
			{
                jdbcLogger.i18NLogger.warn_getmoderror(ex);
			}
		}
	}

	private TransactionalDriverXAConnection _transactionalDriverXAConnectionConnection;

	private java.lang.Object _theModifier;

	private Connection _theConnection;

    private static final int defaultIsolationLevel = jdbcPropertyManager.getJDBCEnvironmentBean().getIsolationLevel();

    private boolean _poolingEnabled = false;


    private AtomicInteger useCount = new AtomicInteger(0);
}