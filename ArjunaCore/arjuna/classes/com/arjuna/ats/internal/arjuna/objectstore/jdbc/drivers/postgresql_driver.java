/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc.drivers;

import java.sql.Connection;
import java.sql.SQLException;

import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple_driver;

/**
 * JDBC store implementation driver-specific code. This version for PostgreSQL
 * JDBC Drivers.
 */
public class postgresql_driver extends JDBCImple_driver {

	@Override
	protected void checkCreateTableError(SQLException ex) throws SQLException {
		if (!ex.getSQLState().equals("42P07")) {
			throw ex;
		}
	}

	@Override
	protected void checkDropTableException(Connection connection,
			SQLException ex) throws SQLException {
		if (!ex.getSQLState().equals("42P01")) {
			throw ex;
		} else {
			// For some reason PSQL leaves the transaction in a bad state on a
			// failed drop
			connection.commit();
		}
	}
}