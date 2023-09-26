/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc.drivers;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC store implementation driver-specific code. This version for Maria DB JDBC
 * Drivers.
 */
public class mariadb_driver extends
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