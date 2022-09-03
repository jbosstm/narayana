package com.arjuna.ats.internal.jdbc;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.ConnectionModifier;
import com.arjuna.ats.jdbc.logging.jdbcLogger;
import com.arjuna.ats.jta.xa.RecoverableXAConnection;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import jakarta.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.sql.SQLException;

public abstract class BaseTransactionalDriverXAConnection implements ConnectionControl, TransactionalDriverXAConnection {
    protected void createConnection() throws SQLException {
        if (jdbcLogger.logger.isTraceEnabled()) {
            jdbcLogger.logger.trace("BaseTransactionalDriverXAConnection.createConnection");
        }

        try {
            if ((_user == null || _user.isEmpty()) && (_passwd == null || _passwd.isEmpty())) {
                if (jdbcLogger.logger.isTraceEnabled()) {
                    jdbcLogger.logger.trace("BaseTransactionalDriverXAConnection - getting connection with no user");
                }

                _theConnection = _theDataSource.getXAConnection();
            } else {
                if (jdbcLogger.logger.isTraceEnabled()) {
                    jdbcLogger.logger.trace("BaseTransactionalDriverXAConnection - getting connection for user " + _user);
                }

                _theConnection = _theDataSource.getXAConnection(_user, _passwd);
            }
        } catch (Exception e) {
            e.printStackTrace();

            SQLException sqlException = new SQLException(e.toString());
            sqlException.initCause(e);
            throw sqlException;
        }
    }

    public final void finalize() {
        try {
            if (_theConnection != null) {
                _theConnection.close();
                _theConnection = null;
            }
            _theXAResource = null;
        } catch (SQLException e) {
            jdbcLogger.i18NLogger.warn_drcdest(e);
        }
    }

    public final boolean setTransaction(jakarta.transaction.Transaction tx) {
        if (tx == null) {
            _theTransaction = null;
            return true;
        }

        synchronized (this) {
            if (_theTransaction == null) {
                _theTransaction = tx;

                return true;
            }
        }

	/*
     * In case we have already set it for this transaction.
	 */

        return validTransaction(tx);
    }

    public final boolean validTransaction(jakarta.transaction.Transaction tx) {
        boolean valid = true;

        if (_theTransaction != null) {
            valid = _theTransaction.equals(tx);
        }

        return valid;
    }

    /**
     * @return a new XAResource for this connection.
     */

    public final XAResource getResource() throws SQLException {
        if (jdbcLogger.logger.isTraceEnabled()) {
            jdbcLogger.logger.trace("BaseTransactionalDriverXAConnection.getResource ()");
        }

        try {
            if (_theXAResource == null)
                if (_theModifier != null && _theModifier.requiresSameRMOverride()) {
                    _theXAResource = new IsSameRMOverrideXAResource(getConnection().getXAResource());
                } else {
                    _theXAResource = getConnection().getXAResource();
                }

            return _theXAResource;
        } catch (Exception e) {
            e.printStackTrace();

            SQLException sqlException = new SQLException(e.toString());
            sqlException.initCause(e);
            throw sqlException;
        }
    }

    public final void close() {
        reset();
    }

    public final void reset() {
        _theXAResource = null;
        _theTransaction = null;
    }

    public final void closeCloseCurrentConnection() throws SQLException {
        synchronized (this) {
            if (_theConnection != null) {
                _theConnection.close();
                _theConnection = null;
                reset();
            }
        }
    }

    public final XAConnection getConnection() throws SQLException {
        if (jdbcLogger.logger.isTraceEnabled()) {
            jdbcLogger.logger.trace("BaseTransactionalDriverXAConnection.getConnection ()");
        }

        try {
            synchronized (this) {
                if (_theConnection == null) {
                    createConnection();
                }
            }

            return _theConnection;
        } catch (Exception e) {
            e.printStackTrace();

            SQLException sqlException = new SQLException(e.toString());
            sqlException.initCause(e);
            throw sqlException;
        }
    }

    public final boolean inuse() {
        return (boolean) (_theXAResource != null);
    }

    public final String user() {
        return _user;
    }

    public final String password() {
        return _passwd;
    }

    public final String url() {
        return _dbName;
    }

    public final String dynamicClass() {
        return "";
    }

    public final String dataSourceName() {
        return _dbName;
    }

    public final Transaction transaction() {
        return _theTransaction;
    }

    public final void setModifier(ConnectionModifier cm) {
        _theModifier = cm;

        if (_theModifier != null)
            _dbName = _theModifier.initialise(_dbName);
    }

    public final XADataSource xaDataSource() {
        if (jdbcLogger.logger.isTraceEnabled()) {
            jdbcLogger.logger.trace("BaseTransactionalDriverXAConnection.getDataSource ()");
        }
        return _theDataSource;
    }

    public XADataSource getDataSource() throws SQLException {
        if (jdbcLogger.logger.isTraceEnabled()) {
            jdbcLogger.logger.trace("DirectRecoverableConnection.getDataSource ()");
        }

        return _theDataSource;
    }

    protected String _dbName;
    protected String _user;
    protected String _passwd;
    protected XAConnection _theConnection;
    protected XAResource _theXAResource;
    protected volatile jakarta.transaction.Transaction _theTransaction;
    protected ConnectionModifier _theModifier;
    protected ConnectionImple _theArjunaConnection;
    protected XADataSource _theDataSource;
}
