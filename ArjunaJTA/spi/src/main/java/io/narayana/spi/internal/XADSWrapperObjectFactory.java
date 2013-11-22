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
package io.narayana.spi.internal;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import javax.sql.XADataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class XADSWrapperObjectFactory implements ObjectFactory {

    private static Map<String, String> jdbcDrivers = new HashMap<String, String>()  {{
        put("org.postgresql.Driver", "org.postgresql.xa.PGXADataSource");
        put("org.h2.Driver", "org.h2.jdbcx.JdbcDataSource");
        put("oracle.jdbc.driver.OracleDriver", "oracle.jdbc.xa.client.OracleXADataSource");
        put("com.microsoft.sqlserver.jdbc.SQLServerDriver", "com.microsoft.sqlserver.jdbc.SQLServerXADataSource"); // no setPassword
        put("com.mysql.jdbc.Driver", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        put("com.ibm.db2.jcc.DB2Driver", "com.ibm.db2.jcc.DB2XADataSource"); // for DB2 version 8.2      // no setPassword
        put("com.sybase.jdbc3.jdbc.SybDriver", "com.sybase.jdbc3.jdbc.SybXADataSource");  // no setPassword
    }};

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        Reference ref = (Reference)obj;
        XADataSource xads = getXADataSource(
                getStringProperty(ref, "binding"),
                getStringProperty(ref, "driver"),
                getStringProperty(ref, "databaseName"),
                getStringProperty(ref, "host"),
                getIntegerProperty(ref, "port", 0),
                getStringProperty(ref, "username"),
                getStringProperty(ref, "password")
        );

        return xads;
    }

    public static Reference getReference(String className, String binding,
                                         String driver, String databaseName,
                                         String host, Integer port,
                                         String userName, String password)  throws NamingException {

       Reference ref = new Reference(className, XADSWrapperObjectFactory.class.getName(), null);

       ref.add(new StringRefAddr("binding", binding));
       ref.add(new StringRefAddr("driver", driver));
       ref.add(new StringRefAddr("databaseName", databaseName));
       ref.add(new StringRefAddr("host", host));
       ref.add(new StringRefAddr("port", port.toString()));

       ref.add(new StringRefAddr("username", userName));
       ref.add(new StringRefAddr("password", password));

       return ref;
   }

    public static XADSWrapper getXADataSource(String binding, String driver, String databaseName, String host, Integer port, String userName, String password)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        XADSWrapper wrapper;
        String xaDSClassName = jdbcDrivers.get(driver);

        if (xaDSClassName == null)
            throw new RuntimeException("JDBC2 driver " + driver + " not recognised");

        wrapper = new XADSWrapper(binding, driver, databaseName, host, port, xaDSClassName, userName, password);

        if( driver.equals("org.h2.Driver")) {
            wrapper.setProperty("URL", databaseName);
        } else {
            wrapper.setProperty("databaseName", databaseName);
            wrapper.setProperty("serverName", host);
            wrapper.setProperty("portNumber", port);
        }

        if (driver.equals("oracle.jdbc.driver.OracleDriver")) {
            wrapper.setProperty("driverType", "thin");
        } else if( driver.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver")) {
            wrapper.setProperty("sendStringParametersAsUnicode", false);
        } else if( driver.equals("com.mysql.jdbc.Driver")) {

            // Note: MySQL XA only works on InnoDB tables.
            // set 'default-storage-engine=innodb' in e.g. /etc/my.cnf
            // so that the 'CREATE TABLE ...' statments behave correctly.
            // doing this config on a per connection basis instead is
            // possible but would require lots of code changes :-(

            wrapper.setProperty("pinGlobalTxToPhysicalConnection", true); // Bad Things happen if you forget this bit.
        } else if( driver.equals("com.ibm.db2.jcc.DB2Driver")) {
            wrapper.setProperty("driverType", 4);
        } else if( driver.equals("org.h2.Driver")) {
            wrapper.setProperty("URL", databaseName);
        }

        return wrapper;
    }

    private String getStringProperty(Reference ref, String propName) {

        RefAddr addr = ref.get(propName);

        return (addr == null ? null : (String)addr.getContent());
    }

    private Integer getIntegerProperty(Reference ref, String propName, int defValue) {

        RefAddr addr = ref.get(propName);

        if (addr ==  null)
            return defValue;

        Object content =  addr.getContent();

        return (content == null ? defValue : Integer.parseInt(content.toString()));
    }
}
