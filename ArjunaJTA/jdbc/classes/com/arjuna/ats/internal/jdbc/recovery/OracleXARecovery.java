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

import java.util.*;
import java.sql.*;
import javax.sql.*;
import javax.transaction.xa.*;
import com.arjuna.common.util.logging.*;
import com.arjuna.common.util.propertyservice.PropertyManagerFactory;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.common.internal.util.propertyservice.plugins.io.XMLFilePlugin;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.ats.jdbc.common.*;
import com.arjuna.ats.jdbc.logging.jdbcLogger;
import com.arjuna.ats.jta.recovery.XAResourceRecovery;
import oracle.jdbc.xa.client.OracleXADataSource;

/**
 * This provides resource recovery for Oracle JDBC drivers.
 *
 * Users are responsible for DataSource creation information in a property file to
 * setup this class. Username and password values may also be provided in
 * the same property file.
 *
 * @deprecated Use BasicXARecovery instead for standalone cases. Use AppServerJDBCRecovery for JBossAS embedded cases.
 */

public class OracleXARecovery implements XAResourceRecovery
{
    public static final String DATABASE_URL = "DatabaseURL";
    public static final String USER_NAME    = "UserName";
    public static final String PASSWORD     = "Password";

    /*
     * Some XAResourceRecovery implementations will do their startup work here.
     */

    public OracleXARecovery()
        throws SQLException
    {
        if (jdbcLogger.logger.isDebugEnabled())
            jdbcLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_CRASH_RECOVERY, "JDBCXARecovery()");

        _props                   = null;
        _hasMoreResources        = false;
        _connectionEventListener = new LocalConnectionEventListener();
    }

    /**
     * The recovery module will have chopped off this class name already. The
     * parameter should specify a property file from which the url, user name,
     * password can be read.
     *
     * @message com.arjuna.ats.internal.jdbc.recovery.oracle.initexp An exception
     *          occurred during initialisation.
     */

    public boolean initialise(String parameter)
        throws SQLException
    {
        if (jdbcLogger.logger.isDebugEnabled())
            jdbcLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_CRASH_RECOVERY, "JDBCXARecovery.initialise(" + parameter + ")");

        if (parameter == null)
            return false;

        try
        {
            PropertyManager propertyManager = PropertyManagerFactory.getPropertyManagerForFile(parameter, false);

            _props = propertyManager.getProperties();

            _dbURL    = _props.getProperty(DATABASE_URL);
            _user     = _props.getProperty(USER_NAME);
            _password = _props.getProperty(PASSWORD);
        }
        catch (Exception e)
        {
            if (jdbcLogger.loggerI18N.isWarnEnabled())
            {
                jdbcLogger.loggerI18N.warn("com.arjuna.ats.internal.jdbc.recovery.oracle.initexp", new Object[] { e }, e);
            }

            return false;
        }

        return true;
    }

    /**
     * @message com.arjuna.ats.internal.jdbc.recovery.oracle.xarec {0} could not find
     *          information for connection!
     */

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
     * Creating and initalising an OracleXADataSource instance. We got the relevant information from the
     * property file provided at input to this instance.
     *
     * @message com.arjuna.ats.internal.jdbc.oracle.recjndierror Could not resolve JNDI
     *          XADataSource
     */

    private final void createDataSource()
        throws SQLException
    {
        try
        {
            if (_dataSource == null)
            {
                OracleXADataSource dataSource = new OracleXADataSource();
                dataSource.setURL(_dbURL);

                _dataSource = dataSource;

                if (_dataSource == null)
                    throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.oracle.recjndierror"));
            }
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();

            throw ex;
        }
        catch (Exception e)
        {
            e.printStackTrace();

            SQLException sqlException = new SQLException(e.toString());
            sqlException.initCause(e);
            throw sqlException;
        }
    }

    /**
     * Create the XAConnection from the XADataSource.
     */

    private final void createConnection()
        throws SQLException
    {
        try
        {
            if (_dataSource == null)
                createDataSource();

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
            ex.printStackTrace();

            throw ex;
        }
        catch (Exception e)
        {
            e.printStackTrace();

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
    private String                       _dbURL;
    private String                       _user;
    private String                       _password;
    private boolean                      _hasMoreResources;
}
