/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss Inc.
 */
package com.arjuna.ats.internal.jdbc.drivers;

import com.arjuna.ats.internal.jdbc.DynamicClass;

import javax.sql.XADataSource;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Enumeration;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * A dynamic class that reads from a properties file and uses the information to
 * instantiate and configure an XADataSource.
 *
 * The properties in the file must be as follows:
 *   xaDataSourceClassName : The name of the driver class that implements XADataSource
 * All other properties in the file are read and a matching setter method called
 * on the XADataSource. This allows for implementations that require non standard configuration.
 * e.g.
 *   serverName=foo
 * results in the method call
 *   setServerName("foo");
 * in accordance with JavaBeans naming conventions.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-05
 */
public class PropertyFileDynamicClass implements DynamicClass
{
    private static final String xaDataSourceClassNameProperty = "xaDataSourceClassName";

    public XADataSource getDataSource(String propertyFileName) throws SQLException {
        // read some system properties and use reflection to load and configure the datasource.

        Properties properties = new Properties();

        FileInputStream propertiesFileInputStream = null;
        try {
            propertiesFileInputStream = new FileInputStream(propertyFileName);
            properties.load(propertiesFileInputStream);
            propertiesFileInputStream.close();
        } catch(IOException e) {
            SQLException sqlException = new SQLException("failed to locate properties file");
            sqlException.initCause(e);
            throw sqlException;
        } finally {
            if(propertiesFileInputStream != null) {
                try {
                    propertiesFileInputStream.close();
                } catch(IOException e) {}
            }
        }

        String xaDataSourceClassName = properties.getProperty(xaDataSourceClassNameProperty);

        XADataSourceReflectionWrapper xaDataSourceReflectionWrapper = new XADataSourceReflectionWrapper(xaDataSourceClassName);

        Enumeration enumeration = properties.propertyNames();
        while(enumeration.hasMoreElements()) {
            String propertyName = (String)enumeration.nextElement();
            if(xaDataSourceClassNameProperty.equals(propertyName)) {
                continue;
            }
            String propertyValue = (String)properties.get(propertyName);
            try {
                xaDataSourceReflectionWrapper.setProperty(propertyName, propertyValue);
            } catch(Exception e) {
                SQLException sqlException = new SQLException("failed to configure XADataSource");
                sqlException.initCause(e);
                throw sqlException;
            }
        }

        return xaDataSourceReflectionWrapper.getWrappedXADataSource();
    }

    /**
     * @deprecated
     */
    public XADataSource getDataSource(String dbName, boolean create) throws SQLException {
        return null;
    }

    /**
     * @deprecated
     */
    public void shutdownDataSource(XADataSource ds) throws SQLException {
    }
}
