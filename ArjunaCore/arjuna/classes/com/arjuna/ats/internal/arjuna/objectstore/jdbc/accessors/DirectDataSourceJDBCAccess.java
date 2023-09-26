/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors;

import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

/**
 * Access mechanism for supplying a DataSource instance at runtime
 */
public class DirectDataSourceJDBCAccess implements JDBCAccess {
	private final DataSource dataSource;

	public DirectDataSourceJDBCAccess(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Connection getConnection() throws SQLException {
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);

		return connection;
	}

	public void initialise(StringTokenizer tokenizer) {
	}
}