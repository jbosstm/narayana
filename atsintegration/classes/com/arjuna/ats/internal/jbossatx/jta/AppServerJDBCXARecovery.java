/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and others contributors as indicated
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
 * (C) 2008,
 * @author Redhat Middleware LLC.
 */
package com.arjuna.ats.internal.jbossatx.jta;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

import javax.transaction.xa.XAResource;
import javax.naming.InitialContext;
import javax.sql.XADataSource;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionEvent;
import javax.sql.XAConnection;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.InstanceNotFoundException;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Properties;
import java.util.Iterator;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import org.jboss.logging.Logger;

/**
 * This provides recovery for compliant JDBC drivers accessed via datasources deployed in JBossAS 4.2
 * It is not meant to be db driver specific.
 *
 * This code is based on JDBCXARecovery, which expects JNDI to contain an XADataSource implementation.
 * In JBossAS, the object created in JNDI when a -ds.xml file containing <xa-datasource> element is used,
 * does not implement the XADataSource interface. We therefore use this modified code to pull the
 * datasource configuration information from the app server's JMX and instantiate an XADataSource from
 * which it can then create an XAConnection.
 *
 * To use this class, add an XAResourceRecovery entry in the jta section of jbossjta-properties.xml
 * for each datasource for which you need recovery, ensuring the value ends with ;<datasource-name>
 * i.e. the same value as is in the -ds.xml jndi-name element.
 * You also need the XARecoveryModule enabled and appropriate values for nodeIdentifier and xaRecoveryNode set.
 * See the JBossTS recovery guide if you are unclear on how the recovery system works.
 *
 * Note: This implementation expects to run inside the app server JVM. It probably won't work
 * if you configure the recovery manager to run as a separate process. (JMX can be accessed remotely,
 * but it would need code changes to the lookup function and some classpath additions).
 *
 *  <properties depends="arjuna" name="jta">
 *  ...
 *    <property name="com.arjuna.ats.jta.recovery.XAResourceRecovery1" value= "com.arjuna.ats.internal.jbossatx.jta.AppServerJDBCXARecovery;MyExampleDbName"/>
 *    <!-- xaRecoveryNode should match value in nodeIdentifier or be * -->
 *    <property name="com.arjuna.ats.jta.xaRecoveryNode" value="1"/>
 *
 */
public class AppServerJDBCXARecovery implements XAResourceRecovery {

// implementation based on com.arjuna.ats.internal.jdbc.recovery.JDBCXARecovery

    public AppServerJDBCXARecovery()
        throws SQLException
    {
        if (log.isDebugEnabled())
		{
            log.debug("AppServerJDBCXARecovery<init>");
        }

        _hasMoreResources        = false;
        _connectionEventListener = new LocalConnectionEventListener();
    }

    /**
     * The recovery module will have chopped off this class name already. The
     * parameter should specify a jndi name for the datasource.
     */

    public boolean initialise(String parameter)
        throws SQLException
    {
        if (log.isDebugEnabled())
		{
            log.debug("AppServerJDBCXARecovery.initialise(" + parameter + ")");
        }

        if (parameter == null)
            return false;

        // don't create the datasource yet, we'll do it lazily. Just keep its id.
        _dataSourceId = parameter;

        return true;
    }

    public synchronized XAResource getXAResource()
        throws SQLException
    {
        createConnection();

        if (_connection == null) {
            throw new SQLException("The data source named [" + _dataSourceId + "] is not deployed.");
        }

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
     * Lookup the XADataSource in JNDI.
     */
    private final void createDataSource()
        throws SQLException
    {
        try
        {
            if (_dataSource == null)
            {
                // This is where we do JBossAS specific magic. Use the JMX to fetch the name of the XADataSource class
                // and its config params so that we don't have to duplicate the information in the -ds.xml file.

                // TODO: can we make this flexible enough to handle remote app server processes, so that
                // we can run the recovery manager out of process? The server addr would need to be on a per
                // jndiname basis, as we may be doing recovery for a whole cluster. We would need AS
                // jmx classes (and the db drivers naturally) on the recovery manager classpath.

                InitialContext context = new InitialContext();
                MBeanServerConnection server = (MBeanServerConnection)context.lookup("jmx/invoker/RMIAdaptor");
                ObjectName objectName = new ObjectName("jboss.jca:name="+_dataSourceId+",service=ManagedConnectionFactory");
                String className = (String)server.invoke(objectName, "getManagedConnectionFactoryAttribute", new Object[] {"XADataSourceClass"}, new String[] {"java.lang.String"});
                log.debug("AppServerJDBCXARecovery datasource classname = "+className);
                String properties = (String)server.invoke(objectName, "getManagedConnectionFactoryAttribute", new Object[] {"XADataSourceProperties"}, new String[] {"java.lang.String"});
                // debug disabled due to security paranoia - it may log datasource password in cleartext.
                // log.debug("AppServerJDBCXARecovery.result="+properties);

                try {
                    _dataSource = getXADataSource(className, properties);
                    _supportsIsValidMethod = true; // assume it does; we'll lazily check the first time we try to connect
                } catch(Exception e) {
                    _dataSource = null;
                    log.error("AppServerJDBCXARecovery.createDataSource got exception during getXADataSource call: "+e.toString(), e);
                    throw new SQLException(e.toString());
                }
            }
        }
        catch (MBeanException mbe)
        {
            if (mbe.getTargetException() instanceof InstanceNotFoundException)
            {
                log.warn("AppServerJDBCXARecovery.createDataSource(name="+_dataSourceId+"): InstanceNotFound. Datasource not deployed, or wrong name?");

                // this is an expected condition when the data source is not yet deployed
                // just ignore this for now, the next time around, we try again to see if its deployed yet
                return;
            } else {
                log.error("AppServerJDBCXARecovery.createDataSource(name="+_dataSourceId+") got exception " + mbe.toString(), mbe);
            }

            throw new SQLException(mbe.toString());
        }
        catch (SQLException ex)
        {
            log.error("AppServerJDBCXARecovery.createDataSource got exception "+ex.toString(), ex);

            throw ex;
        }
        catch (Exception e)
        {
            log.error("AppServerJDBCXARecovery.createDataSource got exception "+e.toString(), e);

            throw new SQLException(e.toString());
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
            {
                createDataSource();
                // if we still don't have it, its because the data source isn't deployed yet
                if (_dataSource == null) {
                    return;
                }
            }

            Boolean isConnectionValid;
            try {
                if (_connection != null && _supportsIsValidMethod) {
                    Connection connection = _connection.getConnection();
                    Method method = connection.getClass().getMethod("isValid",  new Class[] {Integer.class});
                    isConnectionValid = (Boolean) method.invoke(connection, new Object[] {new Integer(5)});
                } else {
                    isConnectionValid = Boolean.FALSE;
                }
            } catch (NoSuchMethodException nsme) {
                isConnectionValid = Boolean.FALSE;
                _supportsIsValidMethod = false;
                log.debug("XA datasource does not support isValid method - connection will always be recreated");
            } catch (Throwable t) {
                isConnectionValid = Boolean.FALSE;
                log.debug("XA connection is invalid - will recreate a new one. Cause: " + t);
            }

            if (!isConnectionValid.booleanValue()) {
                if (_connection != null) {
                    try {
                        _connection.close(); // just attempt to clean up anything that we can
                    } catch (Throwable t) {
                    } finally {
                        _connection = null;
                    }
                }

                _connection = _dataSource.getXAConnection();
                _connection.addConnectionEventListener(_connectionEventListener);
                log.debug("Created new XAConnection");
            }
        }
        catch (SQLException ex)
        {
            log.error("AppServerJDBCXARecovery.createConnection got exception "+ex.toString(), ex);

            throw ex;
        }
        catch (Exception e)
        {
            log.error("AppServerJDBCXARecovery.createConnection got exception "+e.toString(), e);

            throw new SQLException(e.toString());
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

    // borrowed from org.jboss.resource.adapter.jdbc.xa.XAManagedConnectionFactory
    // in which the equivalent functionality is protected not public :-(
    private XADataSource getXADataSource(String xaDataSourceClassname, String propertiesString) throws Exception
    {
        // Map any \ to \\
        propertiesString = propertiesString.replaceAll("\\\\", "\\\\\\\\");

        Properties properties = new Properties();
        InputStream is = new ByteArrayInputStream(propertiesString.getBytes());
        properties.load(is);

        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(xaDataSourceClassname);
        XADataSource xads = (XADataSource) clazz.newInstance();
        Class[] NOCLASSES = new Class[] {};
        for (Iterator i = properties.keySet().iterator(); i.hasNext();)
        {
            String name = (String) i.next();
            String value = properties.getProperty(name);
            //This is a bad solution.  On the other hand the only known example
            // of a setter with no getter is for Oracle with password.
            //Anyway, each xadatasource implementation should get its
            //own subclass of this that explicitly sets the
            //properties individually.
            Class type = null;
            try
            {
                Method getter = clazz.getMethod("get" + name, NOCLASSES);
                type = getter.getReturnType();
            }
            catch (NoSuchMethodException e)
            {
                type = String.class;

                try
                {
                    //HACK for now until we can rethink the XADataSourceProperties variable and pass type information
                    Method getter = clazz.getMethod("is" + name, NOCLASSES);
                    type = getter.getReturnType();

                }catch(NoSuchMethodException nsme)
                {
                    type = String.class;

                }

            }

            Method setter = clazz.getMethod("set" + name, new Class[] { type });
            PropertyEditor editor = PropertyEditorManager.findEditor(type);
            editor.setAsText(value);
            setter.invoke(xads, new Object[] { editor.getValue() });
        }
        return xads;
    }

    private boolean _supportsIsValidMethod;
    private XAConnection _connection;
    private XADataSource                 _dataSource;
    private LocalConnectionEventListener _connectionEventListener;
    private boolean                      _hasMoreResources;

    private String _dataSourceId;
    private Logger log = org.jboss.logging.Logger.getLogger(AppServerJDBCXARecovery.class);
}
