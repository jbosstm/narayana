/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc.drivers;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC store implementation driver-specific code. This version for IBM DB2
 * Universal JDBC Drivers.
 */
public class ibm_driver extends
		com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple_driver {

	@Override
	protected String getObjectStateSQLType() {
		return "BLOB";
	}

	@Override
	protected void checkCreateTableError(SQLException ex) throws SQLException {
		if (!ex.getSQLState().equals("42710") && ex.getErrorCode() != -601) {
			throw ex;
		}
	}

	@Override
	protected void checkDropTableException(Connection connection,
			SQLException ex) throws SQLException {
		if (!ex.getSQLState().equals("42704") && ex.getErrorCode() != -204) {
			throw ex;
		}

	}
}