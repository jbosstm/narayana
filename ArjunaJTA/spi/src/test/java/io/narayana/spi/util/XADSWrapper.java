/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package io.narayana.spi.util;

import com.arjuna.ats.jdbc.TransactionalDriver;

import javax.naming.*;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class XADSWrapper implements XADataSource, Serializable, Referenceable, DataSource {

    private TransactionalDriver arjunaJDBC2Driver = new com.arjuna.ats.jdbc.TransactionalDriver();
    private String binding;
    private String txDriverUrl;
    private Properties properties;

    private XADataSource xaDataSource;
    private String driver;
    private String databaseName;
    private String host;
    private Integer port;
    private String userName;
    private String password;


    public XADSWrapper(String binding, String driver, String databaseName, String host, Integer port, String xaDSClassName, String userName, String password) {
        try {
            xaDataSource = (XADataSource)Class.forName(xaDSClassName).newInstance();
            txDriverUrl = TransactionalDriver.arjunaDriver + binding;

            this.binding = binding;
            this.driver = driver;
            this.databaseName = databaseName;
            this.host = host;
            this.port = port;
            this.userName = userName;
            this.password = password;

            properties = new Properties();
            properties.put(TransactionalDriver.userName, userName);
            properties.put(TransactionalDriver.password, password);
            properties.put(TransactionalDriver.createDb, true);
        } catch(Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public void setProperty(String name, Object value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        name = "set"+name.substring(0,1).toUpperCase()+name.substring(1);

        Class type = value.getClass();
        if(value instanceof Integer) {
            type = Integer.TYPE;
        }
        if(value instanceof Boolean) {
            type = Boolean.TYPE;
        }

        Method method = xaDataSource.getClass().getMethod(name, type);
        method.invoke(xaDataSource, value);
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return getXAConnection(userName, password);
    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        return xaDataSource.getXAConnection(user, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return xaDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        xaDataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        xaDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return xaDataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getParentLogger();
    }

    // DataSource implementation
    public Connection getConnection() throws SQLException {
        return arjunaJDBC2Driver.connect(txDriverUrl, properties);
    }

    public Connection getConnection(String u, String p) throws SQLException {
        Properties dbProperties = new Properties(properties);

        dbProperties.put(TransactionalDriver.userName, userName);
        dbProperties.put(TransactionalDriver.password, password);

        return arjunaJDBC2Driver.connect(txDriverUrl, dbProperties);
    }

    @Override
    public <T> T unwrap(Class<T> tClass) throws SQLException {
        throw new SQLException("Not a wrapper");
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Reference getReference()  throws NamingException {
        return XADSWrapperObjectFactory.getReference(getClass().getName(), binding,
                driver, databaseName,
                host, port,
                userName, password);
    }
}

