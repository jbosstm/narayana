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

//import com.microsoft.jdbcx.sqlserver.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.*;

import com.sybase.jdbc3.jdbc.SybXADataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;
import org.postgresql.xa.PGXADataSource;
import com.ibm.db2.jcc.DB2XADataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.XADataSource;
import java.util.Hashtable;


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

			if (driver.equals("com.arjuna.ats.jdbc.TransactionalDriver"))
			{
				if ((dynamicClass == null) || (databaseURL == null))
				{
					throw new Exception("One of dynamicClass/datbaseURL was not specified for: " + profileName);
				}

				Class c = Class.forName(dynamicClass);

				DynamicClass arjunaJDBC2DynamicClass = (DynamicClass) c.newInstance();
				javax.sql.XADataSource xaDataSource = arjunaJDBC2DynamicClass.getDataSource(databaseURL);

				xaDataSourceToBind = xaDataSource;
			}
			/*
						else if (driver.equals ("COM.cloudscape.core.JDBCDriver"))
						{
							if (databaseName == null)
								throw new Exception ("DatabaseName was not specified for profile: " + profileName);

							COM.cloudscape.core.XaDataSource specificXaDataSource = (COM.cloudscape.core.XaDataSource )COM.cloudscape.core.DataSourceFactory.getXADataSource();
							specificXaDataSource.setDatabaseName (databaseName);
							specificXaDataSource.setCreateDatabase ("create"); // create db if not present

							xaDataSourceToBind = (XADataSource )specificXaDataSource;
						}
					*/
			else if (driver.equals("oracle.jdbc.driver.OracleDriver"))
			{
				if (databaseName == null)
				{
					throw new Exception("DatabaseName was not specified for profile: " + profileName);
				}

				Class c = Class.forName("oracle.jdbc.xa.client.OracleXADataSource");
				oracle.jdbc.xa.client.OracleXADataSource specificXaDataSource = (oracle.jdbc.xa.client.OracleXADataSource) c.newInstance();
				specificXaDataSource.setDatabaseName(databaseName);
				specificXaDataSource.setServerName(host);
				specificXaDataSource.setPortNumber((new Integer(port)).intValue());
				specificXaDataSource.setDriverType("thin");

				xaDataSourceToBind = specificXaDataSource;
			}
			/*
			else if (driver.equals("com.microsoft.jdbc.sqlserver.SQLServerDriver"))
			{
				// old MS SQL 2005 JDBC driver

				if (databaseName == null)
				{
					throw new Exception("DatabaseName was not specified for profile: " + profileName);
				}

				SQLServerDataSource specificXaDataSource = new SQLServerDataSource();
				specificXaDataSource.setDatabaseName(databaseName);
				specificXaDataSource.setServerName(host);
				specificXaDataSource.setPortNumber((new Integer(port)).intValue());
				specificXaDataSource.setSelectMethod("cursor");
				specificXaDataSource.setSendStringParametersAsUnicode(false);
				xaDataSourceToBind = specificXaDataSource;
			}
			*/
			else if( driver.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver")) {
				// new MS SQL 2005 driver

				SQLServerXADataSource specificXaDataSource = new SQLServerXADataSource();
				specificXaDataSource.setServerName(host);
				specificXaDataSource.setPortNumber(Integer.parseInt(port));
				specificXaDataSource.setDatabaseName(databaseName);
				//ds.setUser("jbossts1");
				//ds.setPassword("jbossts1");
				specificXaDataSource.setSendStringParametersAsUnicode(false);
				xaDataSourceToBind = specificXaDataSource;
			}
			else if( driver.equals("org.postgresql.Driver")) {

				PGXADataSource specificXaDataSource = new PGXADataSource();
				specificXaDataSource.setServerName(host);
				specificXaDataSource.setDatabaseName(databaseName);
				//specificXaDataSource.setUser("test");
				//specificXaDataSource.setPassword("testpass");

				xaDataSourceToBind = specificXaDataSource;
			}
			else if( driver.equals("com.mysql.jdbc.Driver")) {

				// Note: MySQL XA only works on InnoDB tables.
				// set 'default-storage-engine=innodb' in e.g. /etc/my.cnf
				// so that the 'CREATE TABLE ...' statments behave correctly.
				// doing this config on a per connection basis instead is
				// possible but would require lots of code changes :-(

				MysqlXADataSource specificXaDataSource = new MysqlXADataSource();
				specificXaDataSource.setDatabaseName(databaseName);
				specificXaDataSource.setServerName(host);

				specificXaDataSource.setPinGlobalTxToPhysicalConnection(true); // Bad Things happen if you forget this bit.

				xaDataSourceToBind = specificXaDataSource;
			}
			else if( driver.equals("com.ibm.db2.jcc.DB2Driver")) {

				// for DB2 version 8.2

				DB2XADataSource specificXaDataSource = new DB2XADataSource();
				specificXaDataSource.setDriverType(4);
				specificXaDataSource.setDatabaseName(databaseName);
				specificXaDataSource.setServerName(host);
				specificXaDataSource.setPortNumber(Integer.parseInt(port));
				//specificXaDataSource.setUser("jbossts");
    	        //specificXaDataSource.setPassword("jbossts");

				xaDataSourceToBind = specificXaDataSource;
			}
			else if( driver.equals("com.sybase.jdbc3.jdbc.SybDriver")) {

				SybXADataSource specificXaDataSource = new SybXADataSource();

				specificXaDataSource.setServerName(host);
				specificXaDataSource.setPortNumber(Integer.parseInt(port));
				specificXaDataSource.setDatabaseName(databaseName);
				//ds.setUser("jbossts0");
				//ds.setPassword("jbossts0");

				xaDataSourceToBind = specificXaDataSource;
			}
			/*else if (driver.equals("COM.FirstSQL.Dbcp.DbcpXADataSource"))
			{
				COM.FirstSQL.Dbcp.DbcpXADataSource specificXaDataSource = new COM.FirstSQL.Dbcp.DbcpXADataSource();
				specificXaDataSource.setServerName(host);
				specificXaDataSource.setPortNumber((new Integer(port)).intValue());
				xaDataSourceToBind = specificXaDataSource;
			}
			*/
			else
			{
				throw new Exception("JDBC2 driver " + driver + " not recognised");
			}

			//
			// bind to JDNI
			//
			try
			{
				Hashtable env = new Hashtable();
				String initialCtx = System.getProperty("Context.INITIAL_CONTEXT_FACTORY");
				String bindingsLocation = System.getProperty("Context.PROVIDER_URL");
		
				if (bindingsLocation != null)
				{
					env.put(Context.PROVIDER_URL, bindingsLocation);
				}

				env.put(Context.INITIAL_CONTEXT_FACTORY, initialCtx);
				InitialContext ctx = new InitialContext(env);

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
			System.err.println(e);
			System.out.println("Failed");
		}
	}
};
