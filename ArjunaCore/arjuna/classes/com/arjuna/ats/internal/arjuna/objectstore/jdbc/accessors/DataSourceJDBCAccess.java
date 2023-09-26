/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;

public class DataSourceJDBCAccess implements JDBCAccess {

	private String datasourceName;
	private InitialContext context;

	public Connection getConnection() throws SQLException {
		DataSource dataSource;
		try {
			dataSource = (DataSource) context.lookup(datasourceName);
		} catch (NamingException ex) {
			throw new FatalError(toString() + " : " + ex, ex);
		}
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		return connection;
	}

	public void initialise(StringTokenizer tokenizer) {
		while (tokenizer.hasMoreElements()) {
			try {
				String[] split = tokenizer.nextToken().split("=");
				if (split[0].equalsIgnoreCase("datasourceName")) {
					datasourceName = split[1];
				}
			} catch (Exception ex) {
				throw new FatalError(toString() + " : " + ex, ex);
			}
		}

		if (datasourceName == null) {
			throw new FatalError(
					"The JDBC ObjectStore was not configured with a datasource name");
		}
		
		try {
			context = new InitialContext();
		} catch (NamingException ex) {
			throw new FatalError(toString() + " : " + ex, ex);
		}
	}
}