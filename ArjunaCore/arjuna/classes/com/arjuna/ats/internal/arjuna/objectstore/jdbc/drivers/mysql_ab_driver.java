/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc.drivers;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC store implementation driver-specific code. This version for MySQL JDBC
 * Drivers.
 */
public class mysql_ab_driver extends
		com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple_driver {

	@Override
	protected String getObjectStateSQLType() {
		return "BLOB";
	}

	@Override
	protected void checkCreateTableError(SQLException ex) throws SQLException {
		if (!ex.getSQLState().equals("42S01")) {
			throw ex;
		}

	}

	@Override
	protected void checkDropTableException(Connection connection,
			SQLException ex) throws SQLException {
		if (!ex.getSQLState().equals("42S02")) {
			throw ex;
		}
	}
}