/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
 * Copyright (C) 2004, 2005,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id$
 */

package com.arjuna.ats.internal.jdbc.recovery;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.jdbc.common.jdbcPropertyManager;
import com.arjuna.ats.jdbc.logging.jdbcLogger;
import com.arjuna.ats.jta.recovery.XAResourceRecovery;
import com.arjuna.common.util.propertyservice.PropertiesFactory;

/**
 * This provides recovery for compliant JDBC drivers. It is not meant to be
 * implementation specific.
 *
 * Users are responsible for deploying an appropriate XADataSource into JNDI and
 * then providing the relevant JNDI lookup information in a property file to an
 * instance of this class. Username and password values may also be provided in
 * the same property file.
 */

public class JDBCXARecovery implements XAResourceRecovery
{
    public static final String DATABASE_JNDI_NAME = "DatabaseJNDIName";
    public static final String USER_NAME          = "UserName";
    public static final String PASSWORD           = "Password";

    /*
     * Some XAResourceRecovery implementations will do their startup work here,
     * and then do little or nothing in setDetails. Since this one needs to know
     * dynamic class name, the constructor does nothing.
     */

    public JDBCXARecovery()
        throws SQLException
    {
        if (jdbcLogger.logger.isDebugEnabled())
            jdbcLogger.logger.debug("JDBCXARecovery()");

        _props                   = null;
        _hasMoreResources        = false;
        _connectionEventListener = new LocalConnectionEventListener();
    }

    /**
     * The recovery module will have chopped off this class name already. The
     * parameter should specify a property file from which the jndi name, user name,
     * password can be read.
     */

    public boolean initialise(String parameter)
        throws SQLException
    {
        if (jdbcLogger.logger.isDebugEnabled())
            jdbcLogger.logger.debug("JDBCXARecovery.initialise(" + parameter + ")");

        if (parameter == null)
            return false;

        try
        {
            _props = PropertiesFactory.getPropertiesFromFile(parameter, JDBCXARecovery.class.getClassLoader());

            _dbName   = _props.getProperty(DATABASE_JNDI_NAME);
            _user     = _props.getProperty(USER_NAME);
            _password = _props.getProperty(PASSWORD);
        }
        catch (Exception e)
        {
            jdbcLogger.i18NLogger.warn_recovery_xa_initexp(e);

            return false;
        }

        return true;
    }

    public synchronized XAResource getXAResource()
        throws SQLException
    {
        createConnection();

        return _connection.getXAResource();
    }

    public boolean hasMoreResources()
    {
        if (_dataSource == null)
            try
            {
                createDataSource();
            }
            catch (SQLException sqlException)
            {
                return false;
            }

        if (_dataSource != null)
        {
            _hasMoreResources = ! _hasMoreResources;

            return _hasMoreResources;
        }
        else
            return false;
    }

    /**
     * Lookup the XADataSource in JNDI. We got the relevant information from the
     * property file provided at input to this instance.
     */

    private final void createDataSource() throws SQLException {
        try
        {
            if (_dataSource == null)
            {
                Hashtable env = jdbcPropertyManager.getJDBCEnvironmentBean().getJndiProperties();
                Context context = new InitialContext(env);
                _dataSource = (XADataSource) context.lookup(_dbName);

                if (_dataSource == null)
                    throw new SQLException(jdbcLogger.i18NLogger.get_cant_resolve_ds_jndi_lookup(_dbName, env));
            }
        }
        catch (SQLException ex)
        {
            jdbcLogger.i18NLogger.error_cannot_create_datasource(_dbName, ex);

            throw ex;
        }
        catch (Exception e)
        {
            jdbcLogger.i18NLogger.error_cannot_create_datasource(_dbName, e);

            SQLException sqlException = new SQLException(e.toString());
            sqlException.initCause(e);
            throw sqlException;
        }
    }

    /**
     * Create the XAConnection from the XADataSource.
     */

    private final void createConnection() throws SQLException
    {
        if (_dataSource == null)
            createDataSource();

        try
        {

            if (_connection == null)
            {
                if ((_user == null) && (_password == null))
                    _connection = _dataSource.getXAConnection();
                else
                    _connection = _dataSource.getXAConnection(_user, _password);

                _connection.addConnectionEventListener(_connectionEventListener);
            }
        }
        catch (SQLException ex)
        {
            jdbcLogger.i18NLogger.error_cannot_create_connection(_dataSource, _user, _password, ex);

            throw ex;
        }
        catch (Exception e)
        {
            jdbcLogger.i18NLogger.error_cannot_create_connection(_dataSource, _user, _password, e);

            SQLException sqlException = new SQLException(e.toString());
            sqlException.initCause(e);
            throw sqlException;
        }
    }

    private class LocalConnectionEventListener implements ConnectionEventListener
    {
        public void connectionErrorOccurred(ConnectionEvent connectionEvent)
        {
            _connection.removeConnectionEventListener(_connectionEventListener);
            _connection = null;
        }

        public void connectionClosed(ConnectionEvent connectionEvent)
        {
            _connection.removeConnectionEventListener(_connectionEventListener);
            _connection = null;
        }
    }

    private XAConnection                 _connection;
    private XADataSource                 _dataSource;
    private LocalConnectionEventListener _connectionEventListener;
    private Properties                   _props;
    private String                       _dbName;
    private String                       _user;
    private String                       _password;
    private boolean                      _hasMoreResources;
}
