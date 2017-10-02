/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JNDIManager.java,v 1.10 2004/07/26 09:49:59 jcoleman Exp $
 */
package org.jboss.jbossts.qa.Utils;

import com.arjuna.ats.internal.jdbc.DynamicClass;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.XADataSource;
import java.util.Hashtable;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Uses reflection to configure the datasources to avoid the need for
 * compile time linking against specific drivers jars
 */
public class JNDIManager
{
	public static void main(String[] args)
	{
		try
		{
			String profileName = args[args.length - 1];
			String driver = JDBCProfileStore.driver(profileName, 0 /*driver number*/);
			String binding = JDBCProfileStore.binding(profileName);
			String databaseName = JDBCProfileStore.databaseName(profileName);
			String host = JDBCProfileStore.host(profileName);
			String dynamicClass = JDBCProfileStore.databaseDynamicClass(profileName);
			String databaseURL = JDBCProfileStore.databaseURL(profileName);
			String port = JDBCProfileStore.port(profileName);

			XADataSource xaDataSourceToBind = null;

			if (driver == null || binding == null)
			{
				throw new Exception("Driver or binding was not specified");
			}

            // We use reflection to configure the data source so as to avoid a compile or runtime
            // dependency on all the drivers. see JBTM-543

			if (driver.equals("com.arjuna.ats.jdbc.TransactionalDriver"))
			{
				if ((dynamicClass == null) || (databaseURL == null))
				{
					throw new Exception("One of dynamicClass/datbaseURL was not specified for: " + profileName);
				}

				Class c = Class.forName(dynamicClass);

				DynamicClass arjunaJDBC2DynamicClass = (DynamicClass) c.getDeclaredConstructor().newInstance();
				javax.sql.XADataSource xaDataSource = arjunaJDBC2DynamicClass.getDataSource(databaseURL);

				xaDataSourceToBind = xaDataSource;
			}
			else if (driver.equals("oracle.jdbc.driver.OracleDriver"))
			{
				if (databaseName == null)
				{
					throw new Exception("DatabaseName was not specified for profile: " + profileName);
				}

                XADataSourceReflectionWrapper wrapper = new XADataSourceReflectionWrapper("oracle.jdbc.xa.client.OracleXADataSource");

                wrapper.setProperty("databaseName", databaseName);
                wrapper.setProperty("serverName", host);
                wrapper.setProperty("portNumber", Integer.valueOf(port));
                wrapper.setProperty("driverType", "thin");

				xaDataSourceToBind = wrapper.getWrappedXADataSource();
			}
			else if( driver.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver")) {

                XADataSourceReflectionWrapper wrapper = new XADataSourceReflectionWrapper("com.microsoft.sqlserver.jdbc.SQLServerXADataSource");

                wrapper.setProperty("databaseName", databaseName);
                wrapper.setProperty("serverName", host);
                wrapper.setProperty("portNumber", Integer.valueOf(port));
                wrapper.setProperty("sendStringParametersAsUnicode", false);

				xaDataSourceToBind = wrapper.getWrappedXADataSource();
			}
			else if( driver.equals("org.postgresql.Driver")) {

                XADataSourceReflectionWrapper wrapper = new XADataSourceReflectionWrapper("org.postgresql.xa.PGXADataSource");

                wrapper.setProperty("databaseName", databaseName);
                wrapper.setProperty("serverName", host);

                xaDataSourceToBind = wrapper.getWrappedXADataSource();
			}
			else if( driver.equals("com.mysql.jdbc.Driver")) {

				// Note: MySQL XA only works on InnoDB tables.
				// set 'default-storage-engine=innodb' in e.g. /etc/my.cnf
				// so that the 'CREATE TABLE ...' statments behave correctly.
				// doing this config on a per connection basis instead is
				// possible but would require lots of code changes :-(

                XADataSourceReflectionWrapper wrapper = new XADataSourceReflectionWrapper("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");

                wrapper.setProperty("databaseName", databaseName);
                wrapper.setProperty("serverName", host);
                wrapper.setProperty("pinGlobalTxToPhysicalConnection", true); // Bad Things happen if you forget this bit.

				xaDataSourceToBind = wrapper.getWrappedXADataSource();
			}
			else if( driver.equals("com.ibm.db2.jcc.DB2Driver")) {

				// for DB2 version 8.2

                XADataSourceReflectionWrapper wrapper = new XADataSourceReflectionWrapper("com.ibm.db2.jcc.DB2XADataSource");

                wrapper.setProperty("databaseName", databaseName);
                wrapper.setProperty("serverName", host);
                wrapper.setProperty("driverType", 4);
                wrapper.setProperty("portNumber", Integer.valueOf(port));

                xaDataSourceToBind = wrapper.getWrappedXADataSource();
			}
			else if( driver.equals("com.sybase.jdbc3.jdbc.SybDriver")) {

                XADataSourceReflectionWrapper wrapper = new XADataSourceReflectionWrapper("com.sybase.jdbc3.jdbc.SybXADataSource");

                wrapper.setProperty("databaseName", databaseName);
                wrapper.setProperty("serverName", host);
                wrapper.setProperty("portNumber", Integer.valueOf(port));

                xaDataSourceToBind = wrapper.getWrappedXADataSource();
			}
			else
			{
				throw new Exception("JDBC2 driver " + driver + " not recognised");
			}

			//
			// bind to JDNI
			//
			try
			{
                // expect suitable java.naming.provider.url and java.naming.factory.initial
                // system properties have been set.
				InitialContext ctx = new InitialContext();

				ctx.rebind(binding, xaDataSourceToBind);

				System.out.println("bound "+binding);
			}
			catch (Exception e)
			{
				System.err.println("JNDIManager.main: Problem binding resource into JNDI");
				e.printStackTrace();
				System.out.println("Failed");
				System.exit(1);
			}

			System.out.println("Passed");
		}
		catch (Exception e)
		{
            e.printStackTrace(System.err);
			System.out.println("Failed");
		}
	}
}

class XADataSourceReflectionWrapper {
    private XADataSource xaDataSource;
    XADataSourceReflectionWrapper(String classname) {
        try {
            xaDataSource = (XADataSource)Class.forName(classname).getDeclaredConstructor().newInstance();
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

    public XADataSource getWrappedXADataSource() {
        return xaDataSource;
    }
}
