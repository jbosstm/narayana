/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package org.jboss.jbossts.qa.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.StringTokenizer;

public class JDBCAccess implements com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess
{
	private static final String dbProp = "org.jboss.jbossts.qa.Utils.JDBCAccess";

	public Connection getConnection() throws SQLException
	{
		String dbName = System.getProperty(dbProp, "OBJECTSTORE_DB");
		System.err.println("Using JDBC store against profile: " + dbName);
		Properties prop = new Properties();
		try
		{
			prop.setProperty("user", JDBCProfileStore.databaseUser(dbName));
			prop.setProperty("password", JDBCProfileStore.databasePassword(dbName));
			Class driverClass = Class.forName(JDBCProfileStore.driver(dbName, 0));
			DriverManager.registerDriver((java.sql.Driver) driverClass.getDeclaredConstructor().newInstance());
			Connection conn = DriverManager.getConnection(JDBCProfileStore.databaseURL(dbName), prop);
			conn.setAutoCommit(false);
			return conn;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		}
	}

	public void initialise(StringTokenizer objName)
	{
	}
}